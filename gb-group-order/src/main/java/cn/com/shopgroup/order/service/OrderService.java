package cn.com.shopgroup.order.service;

import cn.com.shopgroup.common.cache.RedisConstant;
import cn.com.shopgroup.common.cache.RedisHelper;
import cn.com.shopgroup.common.utils.MoneyUtil;
import cn.com.shopgroup.common.utils.TimeUtils;
import cn.com.shopgroup.goods.model.GbGoodsInfo;
import cn.com.shopgroup.goods.model.GbGoodsPackageInfo;
import cn.com.shopgroup.goods.model.GbGoodsSkuInfo;
import cn.com.shopgroup.goods.model.GbGroupActivityInfo;
import cn.com.shopgroup.goods.service.GbGoodsInfoService;
import cn.com.shopgroup.goods.service.GbGoodsPackageInfoService;
import cn.com.shopgroup.goods.service.GbGoodsSkuInfoService;
import cn.com.shopgroup.goods.service.GbGroupActivityInfoService;
import cn.com.shopgroup.order.http.request.OrderGoodsRequest;
import cn.com.shopgroup.order.http.request.OrderRequest;
import cn.com.shopgroup.order.model.GbOrderGoodsInfo;
import cn.com.shopgroup.order.model.GbOrderInfo;
import cn.com.shopgroup.order.utils.OrderNoGeneratorUtils;
import cn.com.shopgroup.user.model.GbMemberInfo;
import cn.com.shopgroup.user.model.GbOrgPointInfo;
import cn.com.shopgroup.user.model.GbOrgShopInfo;
import cn.com.shopgroup.user.service.GbMemberAddressInfoService;
import cn.com.shopgroup.user.service.GbMemberInfoService;
import cn.com.shopgroup.user.service.GbOrgPointInfoService;
import cn.com.shopgroup.user.service.GbOrgShopInfoService;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class OrderService {

    @Resource
    private GbOrderInfoService orderInfoService;

    @Resource
    private GbGroupActivityInfoService groupService;

    @Resource
    private GbOrgPointInfoService pointService;

    @Resource
    private GbGoodsInfoService goodsService;

    @Resource
    private GbGoodsPackageInfoService packService;

    @Resource
    private GbGoodsSkuInfoService skuService;

    @Resource
    private GbMemberInfoService memberService;

    @Resource
    private GbMemberAddressInfoService addressService;

    @Resource
    private GbOrgShopInfoService shopService;

    @Resource
    private RedisHelper redisHelper;
    /**
     * Redis锁前缀用于生成订单号
     */
    private static final String LOCK_PREFIX = "order:lock:";

    /**
     * 锁过期时间（秒）
     */
    private static final long LOCK_EXPIRE = 10;


    // 小程序下单
    public Map<String, String> addOrder(Long memberId, OrderRequest request) {

        // 返回结果
        Map<String, String> result = new HashMap();
        result.put("success", "0");
        result.put("msg", "");

        GbMemberInfo memberInfo = memberService.getMemberInfo(memberId);
        log.info("下单时查询用户信息,memberId:{}，info:{}", memberId, JSON.toJSONString(memberInfo));
        if (ObjectUtils.isEmpty(memberInfo)) {
            result.put("msg", "用户不存在");
            return result;
        }
        Long memberLeaderId = memberInfo.getLeaderId();
        //绑定关系判断
        if (memberLeaderId == null || memberLeaderId.intValue() <= 0) {
            //绑定这个用户的团长
            handleBindLeaderId(memberInfo, request.getGroupId());
        }

        Long groupId = request.getGroupId();
        GbGroupActivityInfo groupInfo = groupService.getGroupInfo(groupId);
        if (ObjectUtils.isEmpty(groupInfo)) {
            result.put("msg", "团购不存在");
            return result;
        }
        if (groupInfo.getIsClose().intValue() == 1) {
            result.put("msg", "团购已结束");
            return result;
        }
        // 团长id
        Long leaderId = groupInfo.getLeaderId();
        Long pointId = request.getPointId();
        GbOrgPointInfo pointInfo = pointService.getPointInfo(pointId);
        if (ObjectUtils.isEmpty(pointInfo)) {
            result.put("msg", "提货点不存在");
            return result;
        }

        // 计算订单费用, 构建订单商品数据
        BigDecimal orderPrice = BigDecimal.ZERO;
        List<GbOrderGoodsInfo> orderGoodsInfoList = new ArrayList<>();
        // 本次下单各商品的基础数量合计(数量×包装数; 同一商品多规格行会累计), 用于限购校验
        Map<Long, Integer> orderGoodsNumMap = new HashMap<>();
        // 该用户在当前团购活动内各商品的已购数量缓存, 避免同一商品多行时重复查库
        Map<Long, Integer> goodsLimitNumMap = new HashMap<>();
        //订单号生成
        String orderNo = createOrderNo(leaderId);
        for (OrderGoodsRequest item : request.getGoods()) {
            // 去掉商品数量为零的情况
            if (item.getNum() <= 0) continue;
            Long goodsId = item.getId();
            GbGoodsInfo goodsInfo = goodsService.getGoodsInfo(goodsId);
            log.info("[addOrder-> getGoodsInfo] goodsId=:{},goodsInfo:{}", goodsId, JSON.toJSONString(goodsInfo));
            if (ObjectUtils.isEmpty(goodsInfo)) {
                result.put("msg", "商品不存在");
                return result;
            }

            // 商品价格, 先使用商品表里面的销售价格, 后面被包装和sku价格覆盖
            Double goodsPrice = goodsInfo.getSalesPrice();

            // 商品包装信息(获取包装里面的价格)
            Integer packNum = 1; // 扣减库存
            long packId = Optional.of(item.getPackId()).orElse(0L).intValue();
            if (packId > 0) {
                GbGoodsPackageInfo packInfo = packService.getGoodsPackageInfo(packId);
                if (ObjectUtils.isEmpty(packInfo)) {
                    result.put("msg", "商品包装信息不存在");
                    return result;
                }
                if (packInfo.getSalesPrice() > 0) {
                    goodsPrice = packInfo.getSalesPrice();
                }
                if (packInfo.getPackNum() > 0) {
                    packNum = packInfo.getPackNum();
                }
            }

            // 商品SKU信息(获取sku里面的价格)
            Long skuId = item.getSkuId();
            GbGoodsSkuInfo skuInfo = new GbGoodsSkuInfo();
            if (skuId > 0) {
                skuInfo = skuService.getGoodsSkuInfo(skuId);
                if (ObjectUtils.isEmpty(skuInfo)) {
                    result.put("msg", "商品sku信息不存在");
                    return result;
                }
                if (skuInfo.getSalesPrice() > 0) goodsPrice = skuInfo.getSalesPrice();
                if (skuInfo.getPackNum() > 0) packNum = skuInfo.getPackNum();
            }

            // 计算商品小计
            BigDecimal goodsTotal = new BigDecimal(goodsPrice).multiply(new BigDecimal(item.getNum()));

            // 商品小计累加到订单总价里面
            orderPrice = orderPrice.add(goodsTotal);
            // 商品库存检查: 优先校验SKU库存(用户选择规格后), 再校验商品总库存兜底
            if (goodsInfo.getIsStock() == 1) {
                int num = item.getNum() * packNum;
                // SKU库存校验(用户选择了具体规格)
                if (skuId > 0 && !ObjectUtils.isEmpty(skuInfo) && skuInfo.getGoodsNum() != null
                        && num > skuInfo.getGoodsNum()) {
                    result.put("msg", (skuInfo.getSkuNames() == null ? "商品SKU" : skuInfo.getSkuNames()) + "库存不足");
                    return result;
                }
                // 商品总库存校验
                if (num > goodsInfo.getGoodsNum()) {
                    result.put("msg", goodsInfo.getGoodsName() + "库存不足");
                    return result;
                }
            }

            // 商品限购检查(仅限购商品): 限购维度为「用户 + 当前团购活动 + 商品」,
            // 该团购活动内该商品已购买数量 + 本次购买数量(数量×包装数, 同一商品多规格行累计) 不能超过限购数
            if (goodsInfo.getIsLimit() != null && goodsInfo.getIsLimit().intValue() == 1
                    && goodsInfo.getLimitNum() != null && goodsInfo.getLimitNum() > 0) {
                int num = item.getNum() * packNum;
                int buyNum = orderGoodsNumMap.merge(goodsId, num, Integer::sum);
                int alreadyBuyNum = goodsLimitNumMap.computeIfAbsent(goodsId,
                        key -> orderInfoService.getGroupOrderGoodsNum(memberId, groupId, goodsId));
                if (buyNum + alreadyBuyNum > goodsInfo.getLimitNum()) {
                    result.put("msg", goodsInfo.getGoodsName() + "限购" + goodsInfo.getLimitNum() + goodsInfo.getGoodsUnit()
                            + ", 本团购活动内您已购买" + alreadyBuyNum + goodsInfo.getGoodsUnit());
                    return result;
                }
            }

            // 构建订单商品表
            GbOrderGoodsInfo orderGoodsInfo = new GbOrderGoodsInfo();
            // 订单号
            orderGoodsInfo.setOrderNo(orderNo);
            // 序号id
           /* itemId++;
            orderGoodsInfo.setItemId(itemId);*/
            // 商品id
            orderGoodsInfo.setGoodsId(goodsInfo.getGoodsId());
            // 商品名称
            orderGoodsInfo.setGoodsName(goodsInfo.getGoodsName());
            // 商品价格
            orderGoodsInfo.setGoodsPrice(goodsPrice);
            // 商品数量
            orderGoodsInfo.setGoodsNum(item.getNum());
            // 商品单位
            orderGoodsInfo.setGoodsUnit(goodsInfo.getGoodsUnit());
            // 商品图片
            orderGoodsInfo.setGoodsImg(goodsInfo.getGoodsImg());
            // 商品类型:1普通商品2称重商品
            orderGoodsInfo.setGoodsType(goodsInfo.getGoodsType());
            // 包装信息
            orderGoodsInfo.setPackId(item.getPackId());
            if (packNum == 0) packNum = 1;
            orderGoodsInfo.setPackNum(packNum); // 不使用用户提交的
            orderGoodsInfo.setPackName(item.getPackName());
            // 规格和sku信息
            orderGoodsInfo.setSkuId(item.getSkuId());
            orderGoodsInfo.setSkuIds(item.getSkuids());
            orderGoodsInfo.setSkuNames(item.getSkunames());
            // 添加订单商品列表中
            orderGoodsInfoList.add(orderGoodsInfo);
        }
        // 订单商品不能为空
        if (CollectionUtils.isEmpty(orderGoodsInfoList)) {
            result.put("msg", "订单商品不能为空");
            return result;
        }

        // 订单金额不能为空
        if (orderPrice.compareTo(BigDecimal.ZERO) == 0) {
            result.put("msg", "订单金额不能为空");
            return result;
        }

        // 团长门店信息
        GbOrgShopInfo shopInfo = shopService.getMiniLeaderShop(leaderId);
        if (ObjectUtils.isEmpty(shopInfo)) {
            result.put("msg", "查询不到团长门店信息");
            return result;
        }
        String shopName = shopInfo.getShopName();

        // 构建订单数据
        GbOrderInfo orderInfo = new GbOrderInfo();
        orderInfo.setOrderNo(orderNo);
        // 用户信息
        orderInfo.setMemberId(memberInfo.getMemberId());
        orderInfo.setMobile(memberInfo.getMobile());
        orderInfo.setNickname(memberInfo.getNickname());
        orderInfo.setAvatar(memberInfo.getAvatar());
        orderInfo.setOpenid(memberInfo.getOpenid());
        // 团购id
        orderInfo.setGroupId(groupInfo.getGroupId());
        // 团长id
        orderInfo.setLeaderId(groupInfo.getLeaderId());
        // 隔离id
        orderInfo.setIsolationId(groupInfo.getIsolationId());
        // 门店名称
        orderInfo.setShopName(shopName);
        // 团购名称
        orderInfo.setGroupName(groupInfo.getGroupName());
        // 团购价格(暂时不需要了)
        orderInfo.setGroupPrice(0D);
        // 订单价格
        orderInfo.setOrderPrice(orderPrice.doubleValue());
        // 生待支付状态
        orderInfo.setStatus(0);
        // 支付金额,单位：分
        int payFee = MoneyUtil.yuanToCent(orderPrice.doubleValue());
        orderInfo.setPayFee(payFee);
        // 支付时间
        orderInfo.setPayTime(0);
        // 支付流水号
        orderInfo.setPayNo("");
        // 退款金额,单位：分
        orderInfo.setRefundFee(0);
        // 退款时间
        orderInfo.setRefundTime(0);
        // 退款流水号
        orderInfo.setRefundNo("");

        // 订单收货方式：1=自提,2=邮寄
        // 团购商品提货方式：1=自提,2=邮递
        Byte receiptType = groupInfo.getPickupStyle();
        orderInfo.setReceiptType(receiptType);
        // 收货姓名,自提和邮寄都需要
        orderInfo.setTrueName(request.getName());
        // 收货电话,自提和邮寄都需要
        orderInfo.setTelephone(request.getMobile());
        // 收货时间
        orderInfo.setReceiptTime(0);
        // 自提点id
        orderInfo.setPointId(pointInfo.getPointId());
        // 自提点名称
        orderInfo.setPointName(pointInfo.getPointName());
        // 详细地址
        orderInfo.setPointAddress(pointInfo.getPointAddress());
        // 自提核销码(订单编号)
        String receiptCode = this.createOrderCode(leaderId);
        orderInfo.setReceiptCode(receiptCode);
        // 自提点id,实际领取自提点
        orderInfo.setPointId2(0L);
        // 自提点名称,实际领取自提点
        orderInfo.setPointName2("");
        // 订单备注
        orderInfo.setRemark("");
        // 下单时间
        orderInfo.setAddTime(TimeUtils.getTimeStamp());
        orderInfo.setUpdateTime(TimeUtils.getTimeStamp());
        // 微信发货调用标记,新订单默认未调用
        orderInfo.setWxShipment(0);
        // 确认收货操作标记,新订单默认未操作
        orderInfo.setClickConfirmFlag(0);
        // 先扣减库存(商品总库存 + SKU库存), 全部成功后再写订单, 防止并发超卖产生"幽灵订单"
        List<GbOrderGoodsInfo> reducedGoodsList = new ArrayList<>();
        for (GbOrderGoodsInfo item : orderGoodsInfoList) {
            int reduceNum = item.getPackNum() * item.getGoodsNum();
            boolean goodsFlag = goodsService.reduceGoodsStock(item.getGoodsId(), reduceNum);
            boolean skuFlag = true;
            if (item.getSkuId() != null && item.getSkuId() > 0) {
                skuFlag = skuService.reduceGoodsStock(item.getSkuId(), reduceNum);
            }
            // 任一库存扣减失败(库存不足): 回补该商品已扣部分 + 之前已扣减的商品, 并返回
            if (!goodsFlag || !skuFlag) {
                if (goodsFlag) goodsService.increaseGoodsStock(item.getGoodsId(), reduceNum);
                if (skuFlag && item.getSkuId() != null && item.getSkuId() > 0) {
                    skuService.increaseGoodsStock(item.getSkuId(), reduceNum);
                }
                restoreOrderStock(reducedGoodsList);
                result.put("msg", item.getGoodsName() + "库存不足");
                return result;
            }
            reducedGoodsList.add(item);
        }
        // 写入数据库
        Long orderId = orderInfoService.addMiniOrder(orderInfo, orderGoodsInfoList);
        if (orderId.intValue() == 0) {
            // 订单写入失败, 回补全部已扣库存
            restoreOrderStock(reducedGoodsList);
            result.put("msg", "下单失败");
            return result;
        }
        // 返回订单Id
        result.put("success", "1");
        result.put("msg", orderNo);
        return result;
    }

    //给新用户绑定团长
    private void handleBindLeaderId(GbMemberInfo memberInfo, Long groupId) {
        GbGroupActivityInfo groupActivityInfo = groupService.getMiniGroupActivityInfo(groupId);
        if (ObjectUtils.isEmpty(groupActivityInfo)) {
            return;
        }
        memberService.updateMemberBindLeader(memberInfo.getMemberId(), groupActivityInfo.getLeaderId());
    }

    // 回补订单商品库存(商品总库存 + SKU库存), 用于下单扣减失败或订单写入失败时的补偿
    private void restoreOrderStock(List<GbOrderGoodsInfo> goodsList) {
        for (GbOrderGoodsInfo item : goodsList) {
            int num = item.getPackNum() * item.getGoodsNum();
            goodsService.increaseGoodsStock(item.getGoodsId(), num);
            if (item.getSkuId() != null && item.getSkuId() > 0) {
                skuService.increaseGoodsStock(item.getSkuId(), num);
            }
        }
    }


    /**
     * 生成订单号（高并发安全）
     */
    public String createOrderNo(Long memberId) {
        String orderNo = "";
        int retryCount = 0;
        final int MAX_RETRY = 3;

        // 重试机制：防止极端情况下的重复
        while (retryCount < MAX_RETRY) {
            orderNo = OrderNoGeneratorUtils.generateGroup(memberId);
            // 1. 先查缓存（布隆过滤器或Set去重）
            String lockKey = LOCK_PREFIX + orderNo;
            Boolean locked = redisHelper.getLock(LOCK_PREFIX, LOCK_EXPIRE);
            if (!locked) {
                // 锁已存在，说明可能重复，重新生成
                retryCount++;
                continue;
            }
            try {
                // 2. 再查数据库（唯一索引兜底）
                if (orderInfoService.existsByOrderNo(orderNo)) {
                    retryCount++;
                    continue;
                }
                log.info("订单号生成成功: orderNo={}, memberId={}", orderNo, memberId);
                return orderNo;

            } finally {
                // 释放锁
                redisHelper.releaseLock(LOCK_PREFIX);
            }
        }
        throw new RuntimeException("订单号生成失败，请重试");
    }


    // 生成核销码 只考虑每个团长下能够"唯一性"就行了
    // 避免相同时间不同用户下单生成相同订单编号
    public String createOrderCode(Long leaderId) {

        // 每个团长每天都有一个订单计数器
        String key = RedisConstant.RedisOrderCodeKey + leaderId + ":" + TimeUtils.getTodayStr();
        if (redisHelper.hasKey(key) == false) {
            redisHelper.increment(key, 1);
            redisHelper.expire(key, RedisConstant.RedisOrderCodeExpired, TimeUnit.SECONDS);
        }

        // 获取当前的时间"yyMMddHHmmss"格式
        // 相同时间不同用户下单的时间是一样的
        Long nowTime = Long.parseLong(TimeUtils.getNowTimeStr());

        // 获取计数器, 每次获取每次累加 1
        // 避免相同时间不同用户下单生成相同订单编号
        long num = redisHelper.increment(key, 1);

        // 累加当前时间, 团长ID和计数器
        return String.valueOf(nowTime + leaderId + num);
    }


}
