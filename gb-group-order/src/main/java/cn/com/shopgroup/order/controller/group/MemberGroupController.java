package cn.com.shopgroup.order.controller.group;

import cn.com.shopgroup.common.cache.RedisConstant;
import cn.com.shopgroup.common.cache.RedisHelper;
import cn.com.shopgroup.common.exception.BusinessException;
import cn.com.shopgroup.common.utils.JsonResult;
import cn.com.shopgroup.common.utils.PhoneGeneratorUtils;
import cn.com.shopgroup.common.utils.TokenUtils;
import cn.com.shopgroup.goods.http.response.group.GroupActGoodsResponse;
import cn.com.shopgroup.goods.http.response.group.GroupActivityResponse;
import cn.com.shopgroup.goods.http.response.group.GroupCategoryResponse;
import cn.com.shopgroup.goods.http.response.group.GroupSpecResponse;
import cn.com.shopgroup.goods.model.GbGoodsSpecInfo;
import cn.com.shopgroup.goods.model.GbGroupActivityGoods;
import cn.com.shopgroup.goods.model.GbGroupActivityInfo;
import cn.com.shopgroup.goods.model.GbGroupCategoryInfo;
import cn.com.shopgroup.goods.service.GbGoodsSpecInfoService;
import cn.com.shopgroup.goods.service.GbGroupActivityInfoService;
import cn.com.shopgroup.goods.service.GbGroupCategoryInfoService;
import cn.com.shopgroup.order.exception.OrderErrorCodeEnum;
import cn.com.shopgroup.order.http.request.MemberGroupActListRequest;
import cn.com.shopgroup.order.http.request.MemberGroupListRequest;
import cn.com.shopgroup.order.http.request.MemberGroupViewRequest;
import cn.com.shopgroup.order.http.response.GroupLogs;
import cn.com.shopgroup.order.http.response.GroupOrderRecordResponse;
import cn.com.shopgroup.order.http.response.MemberHomeGroupActResponse;
import cn.com.shopgroup.order.service.GbGroupViewLogService;
import cn.com.shopgroup.order.service.GbOrderInfoService;
import cn.com.shopgroup.user.http.response.ShopResponse;
import cn.com.shopgroup.user.model.GbOrgShopInfo;
import cn.com.shopgroup.user.service.GbOrgShopInfoService;
import com.alibaba.fastjson2.JSON;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@RestController
@Slf4j
@Api(value = "用户首页-团购活动相关")
@RequestMapping("/order")
public class MemberGroupController {

    @Resource
    private GbGroupActivityInfoService groupActivityInfoService;

    @Resource
    private GbGroupCategoryInfoService categoryService;

    @Resource
    private GbOrderInfoService orderInfoService;

    @Resource
    private GbOrgShopInfoService shopService;

    @Resource
    private RedisHelper redisHelper;

    @Resource
    private GbGroupViewLogService viewLogService;

    @Resource
    private GbGoodsSpecInfoService specService;


    // 团购分类列表（首页）
    @GetMapping("/group/groupActivity/cat")
    public JsonResult groupCat() {
        log.info("【团购分类列表:/order/group/groupActivity/cat】");
        List<GbGroupCategoryInfo> lists = categoryService.getMiniGroupCategoryList();
        List<GroupCategoryResponse> data = GroupCategoryResponse.getGroupCategoryResponseList(lists);
        log.info("【团购分类列表返回】size:{},data:{}", data == null ? 0 : data.size(), JSON.toJSONString(data));
        return JsonResult.success(data);
    }


    // 用户查询所有在线的团购活动列表[新用户未绑定团长时leaderId=0]: 仅返回未下线(isClose=0)且当前时间处于开团时间窗内(已开团未结束)的在线活动; 支持团购名称模糊搜索(groupName, 非空时生效)与团购分类过滤(catId, 非空且大于0时生效); leaderId>0按团长过滤(排序值sortOrder升序置顶优先, 同级按活动id倒序, 分页在SQL层完成); leaderId=0时需传经纬度(longitude/latitude, 缺失返回空列表), 按活动所属团长过滤: 团长的所有未禁用自提点中, 只要有任意一个自提点到用户的距离小于等于该自提点设置的自提范围(point_scope, 单位公里), 该团长下的所有团购活动均对用户可见, 按活动id倒序(先按名称/分类过滤, 再距离过滤, 最后分页); page默认1, pageSize默认10最大20; 每个团购活动一并返回其商品列表(goods: 团购价取团购商品表冗余价, 库存/单位取自商品表, 批量查询, 无商品的团购返回空数组)
    @PostMapping("/member/groupActivity/list")
    public JsonResult getGroupActiveList(@Validated @RequestBody MemberGroupActListRequest request) {
        log.info("用户首页-查询所有在线的团购活动列表,request:{}", JSON.toJSONString(request));
        // 从请求获取团长id
        Long leaderId = request.getLeaderId();
        Double longitude = request.getLongitude();
        Double latitude = request.getLatitude();
        String groupName = request.getGroupName();
        Long catId = request.getCatId();
        // 请求参数矫正
        int page = Optional.ofNullable(request.getPage()).orElse(1);
        int pageSize = Optional.ofNullable(request.getPageSize())
                .map(size -> Math.min(size, 20))
                .orElse(10);
        List<GbGroupActivityInfo> result = groupActivityInfoService.getMemberGroupActivityList(leaderId, longitude, latitude, groupName, catId, page, pageSize);
        List<MemberHomeGroupActResponse> data = MemberHomeGroupActResponse.getGroupActResponseList(result);
        // 填充每个团购活动的商品列表(列表页需要展示团购里的商品)
        fillGroupGoodsList(data);
        // 填充每个团购活动中商品的规格(含规格值), 一次批量查询避免 N+1
        fillGoodsSpecList(extractAllGoods(data));
        for (MemberHomeGroupActResponse item : data) {
            item.setOrder(this.getRedisOrderTotal(item.getId(), item.getVirtual()));
        }
        fillGroupLogList(data);
        log.info("用户首页获取团购活动数据条数size:{},data:{}", data.size(), JSON.toJSONString(data));
        return JsonResult.success(data);
    }

    // 用户首页-查询所有团购活动列表----旧-废弃
    //@PostMapping("/group/get/groupActivity/list")
    public JsonResult getGroupActiveList(@RequestBody MemberGroupListRequest request) {
        log.info("用户首页-查询所有团购活动列表,order/group/get/groupActivity/list req:{}", JSON.toJSONString(request));
        // 请求参数矫正
        int page = Optional.ofNullable(request.getPage()).orElse(1);
        int pageSize = Optional.ofNullable(request.getPageSize())
                .map(size -> Math.min(size, 20))
                .orElse(10);
        String activityName = request.getName();
        Long leaderId = request.getLeaderId();
        // 查询列表(flag 1、团长团查询 2 用户端查询) -7 不做任何处理，填充参数
        List<GbGroupActivityInfo> result = groupActivityInfoService.getMiniLeaderGroupList(2, leaderId, request.getCatId(), activityName, -7, page, pageSize);
        List<MemberHomeGroupActResponse> data = MemberHomeGroupActResponse.getGroupActResponseList(result);
        fillGroupLogList(data);
        for (MemberHomeGroupActResponse item : data) {
            item.setOrder(this.getRedisOrderTotal(item.getId(), item.getVirtual()));
        }
        log.info("获取团购活动数据条数：size:{}", data.size());
        return JsonResult.success(data);
    }

    /**
     * 填充团购活动的滚动跟团记录(groupLogs, 走Redis缓存)
     */
    private void fillGroupLogList(List<MemberHomeGroupActResponse> data) {
        if (CollectionUtils.isEmpty(data)) {
            return;
        }
        for (MemberHomeGroupActResponse item : data) {
            List<GroupLogs> logList = getGroupList(item.getId(), item.getOrder());
            if (!CollectionUtils.isEmpty(logList)) {
                item.setGroupLogs(logList);
            }
        }
    }

    /**
     * 批量填充团购活动的商品列表(团购价取团购商品表冗余价, 库存/单位取自商品表);
     * 一次批量查询完成, 避免列表循环内 N+1 查询; 没有商品的团购返回空列表
     */
    private void fillGroupGoodsList(List<MemberHomeGroupActResponse> data) {
        if (CollectionUtils.isEmpty(data)) {
            return;
        }
        // 收集本页团购id
        List<Long> groupIds = new ArrayList<>();
        for (MemberHomeGroupActResponse item : data) {
            if (item.getId() != null) {
                groupIds.add(item.getId());
            }
        }
        if (groupIds.isEmpty()) {
            return;
        }
        // 批量查询商品后按团购id回填(C端仅展示在线商品, 已下线商品不返回)
        Map<Long, List<GroupActGoodsResponse>> goodsMap = groupActivityInfoService.getGroupGoodsResponseMap(groupIds, true);
        for (MemberHomeGroupActResponse item : data) {
            List<GroupActGoodsResponse> goodsList = goodsMap.get(item.getId());
            item.setGoods(goodsList == null ? new ArrayList<>() : goodsList);
        }
    }

    /**
     * 提取团购活动列表里所有商品的扁平列表(用于后续批量填充规格)
     */
    private List<GroupActGoodsResponse> extractAllGoods(List<MemberHomeGroupActResponse> data) {
        List<GroupActGoodsResponse> all = new ArrayList<>();
        if (CollectionUtils.isEmpty(data)) {
            return all;
        }
        for (MemberHomeGroupActResponse item : data) {
            if (item.getGoods() != null) {
                all.addAll(item.getGoods());
            }
        }
        return all;
    }

    /**
     * 为商品列表填充规格(含规格值)
     * 一次批量查询多个商品id, 避免循环内 N+1
     */
    private void fillGoodsSpecList(List<GroupActGoodsResponse> goodsList) {
        if (CollectionUtils.isEmpty(goodsList)) {
            return;
        }
        // 1. 收集去重的商品id(保持插入顺序便于排查)
        Set<Long> goodsIdSet = new LinkedHashSet<>();
        for (GroupActGoodsResponse item : goodsList) {
            if (item.getGid() != null) {
                goodsIdSet.add(item.getGid());
            }
        }
        if (goodsIdSet.isEmpty()) {
            return;
        }
        // 2. 一次批量查询所有商品的规格(含规格值)
        Map<Long, List<GbGoodsSpecInfo>> specMap = specService.getGoodsSpecListByGoodsIds(new ArrayList<>(goodsIdSet));
        // 3. 回填到每个商品对象
        for (GroupActGoodsResponse item : goodsList) {
            List<GbGoodsSpecInfo> specs = specMap.get(item.getGid());
            if (specs != null) {
                item.setSpecList(GroupSpecResponse.getSpecResponseList(specs));
            }
        }
    }

    // 团购数量（首页）
/*    @GetMapping("/group/groupActivity/count")
    public JsonResult groupCount(@RequestParam("leaderId") Long leaderId, @RequestParam("catId") Long catId) {

        long total = groupActivityInfoService.getMiniGroupActivityCount(leaderId, catId);
        return JsonResult.success(total);
    }*/

    // 团购详情(团长分享页面)---用户首页：团长更多好货也用
    @GetMapping("/group/groupActivity/info")
    public JsonResult groupInfo(@RequestParam("groupId") Long groupId) {
        log.info("【团购详情:/order/group/groupActivity/info】params->groupId:{}", groupId);
        // 先查看是否存在Redis缓存
        String key = RedisConstant.RedisGroupInfoKey + groupId;
        if (redisHelper.hasKey(key) == false) {

            // 根据id查询团购详情
            GbGroupActivityInfo item = groupActivityInfoService.getMiniGroupActivityInfo(groupId);
            if (ObjectUtils.isEmpty(item)) {
                return JsonResult.success();
            }
            GroupActivityResponse data = new GroupActivityResponse(item);
            // 放入Redis中缓存
            redisHelper.setCacheObject(key, data, RedisConstant.RedisGroupInfoExpired, TimeUnit.SECONDS);
        }

        // 使用缓存
        GroupActivityResponse cacheData = redisHelper.getCacheObject(key);

        // 订单销售数量（跟团人次 = 实际支付订单数 + 虚拟订单数）
        // 注意: 不能使用缓存里的 num2(虚拟数量是 30 天详情缓存里的旧快照)。
        // 计数器(OrderTotal:{groupId})被删除重建时(下单/退款/task/过期), 基数 = DB真实订单数 + 虚拟数,
        // 若此处传入旧 num2 而列表接口 getGroupActiveList 传实时 DB virtual, 会导致两接口订单数不一致;
        // 统一走 getOrderNumByGroupId(实时查 DB virtual + Redis 计数器) 保证口径一致
        cacheData.setNum(this.getOrderNumByGroupId(groupId));
        // 当前团购查看人数(按用户去重, 实时统计; 统计异常不影响主流程, 兜底为0)
        cacheData.setViewCount(this.getGroupViewCountQuietly(groupId));
        // 服务端自动埋点: 记录用户"查看"团购(未登录/防抖命中会忽略, 不影响主流程)
        this.recordViewQuietly(groupId);

        // 返回数据
        log.info("团购详情:/order/group/groupActivity/info info:{}", JSON.toJSONString(cacheData));
        return JsonResult.success(cacheData);
    }

    // 用户查看团购详情-显式埋点上报(分享等场景前端调用; 首页进入详情会自动埋点, 可不上报)
    @PostMapping("/group/groupActivity/view")
    public JsonResult groupView(@RequestBody MemberGroupViewRequest request) {
        log.info("order/group/groupActivity/view req:{}", JSON.toJSONString(request));
        if (request == null || request.getGroupId() == null || request.getGroupId() <= 0) {
            throw new BusinessException(OrderErrorCodeEnum.GROUP_ID_REQUIRED);
        }
        Long memberId = this.getCurrentMemberId();
        if (memberId == null || memberId <= 0) {
            throw new BusinessException(OrderErrorCodeEnum.LOGIN_REQUIRED);
        }
        // 防抖在服务内部处理
        Boolean flag = viewLogService.recordView(memberId, request.getGroupId());
        return JsonResult.success(flag == null ? false : flag);
    }

    // 从请求头Token中解析当前登录用户id, 未登录返回0
    private Long getCurrentMemberId() {
        String token = TokenUtils.getToken();
        if (token == null || token.trim().length() == 0) {
            return 0L;
        }
        String userId = TokenUtils.parseToken(token);
        if (userId == null || userId.length() == 0 || userId.matches("^[0-9]+$") == false) {
            return 0L;
        }
        try {
            return Long.parseLong(userId);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    // 静默记录查看(异常不影响主流程)
    private void recordViewQuietly(Long groupId) {
        try {
            Long memberId = this.getCurrentMemberId();
            if (memberId != null && memberId > 0) {
                viewLogService.recordView(memberId, groupId);
            }
        } catch (Exception e) {
            log.error("查看团购自动埋点异常 groupId:{}", groupId, e);
        }
    }

    // 静默统计团购查看人数(异常不影响主流程, 兜底返回0)
    private Integer getGroupViewCountQuietly(Long groupId) {
        try {
            return viewLogService.getGroupViewCount(groupId);
        } catch (Exception e) {
            log.error("统计团购查看人数异常 groupId:{}", groupId, e);
            return 0;
        }
    }

    // 团长店铺详情
    @GetMapping("/group/groupActivity/shop")
    public JsonResult groupShop(@RequestParam("leaderId") Long leaderId) {
        log.info("order/group/groupActivity/shop req:{}", leaderId);
        String key = RedisConstant.RedisShopInfoKey + leaderId;
        if (redisHelper.hasKey(key) == false) {

            // 查询数据库
            GbOrgShopInfo info = shopService.getMiniLeaderShop(leaderId);
            if (ObjectUtils.isEmpty(info)) {
                return JsonResult.success();
            }
            ShopResponse data = new ShopResponse(info);

            // 缓存起来
            redisHelper.setCacheObject(key, data, RedisConstant.RedisShopInfoExpired, TimeUnit.SECONDS);
        }

        // 读取缓存数据
        ShopResponse data = redisHelper.getCacheObject(key);
        log.info("order/group/groupActivity/shop data:{}", JSON.toJSONString(data));
        return JsonResult.success(data);
    }

    //获取某个活动的真实订单+虚拟订单
    public int getOrderNumByGroupId(Long groupId) {
        // 根据id查询团购详情
        GbGroupActivityInfo activityInfo = groupActivityInfoService.getMiniGroupActivityInfo(groupId);
        if (ObjectUtils.isEmpty(activityInfo)) {
            return 0;
        }

        return getRedisOrderTotal(groupId, activityInfo.getVirtualOrder());
    }

    // 团购记录(跟团记录)
    // 早上6点到晚上8点之间，每个小时生成一批跟团记录（固定部分+滚动部分），
    // 固定部分打开页面就显示，滚动部分每隔几秒显示一条；
    // 晚上时间只生成固定跟团记录（锁定在19:00-20:00生成的订单）。
    @GetMapping("/group/groupActivity/logs")
    public JsonResult groupLogs(@RequestParam("groupId") Long groupId) {
        log.info("团购记录 /group/groupActivity/logs, groupId:{}", groupId);
        List<GroupLogs> data = getGroupList(groupId, getOrderNumByGroupId(groupId));
        log.info("团购记录 /group/groupActivity/logs 返回:{}", JSON.toJSONString(data));
        return JsonResult.success(data);
    }

    //orderNum 实际+虚拟 订单数
    private List<GroupLogs> getGroupList(Long groupId, int orderNum) {
        // 放入缓存
        String key = RedisConstant.RedisGroupLogsKey + groupId;
        if (redisHelper.hasKey(key) == false) {
            int total = 0;
            if (orderNum < 10) {
                // orderNum 为 0 时(如缓存异常兜底返回0), total 钳位为 0, 避免负数传入 ArrayList 构造抛 Illegal Capacity
                total = Math.max(orderNum - 1, 0);
            } else {
                // 随机 [10,20] 之间的数字
                total = ThreadLocalRandom.current().nextInt(10, 21);
            }

            // 生成记录
            List<GroupLogs> data = new ArrayList<>();
            if (total < 1) {
                // 无记录可生成, 直接返回空列表(不缓存, 待订单数恢复后重新生成)
                return data;
            }
            if (this.isBetween6And20()) {
                // 白天的数据, 早上6点到晚上8点之间
                data = this.getDayGroupLogs(total, groupId);
                // 放入redis缓存中(1个小时)
                redisHelper.setCacheObject(key, data, RedisConstant.RedisGroupLogsExpired, TimeUnit.SECONDS);
            } else {
                // 晚上的数据, 早上6点之前, 到晚上8点之后, 共计10个小时
                data = this.getNightGroupLogs(total, groupId);
                // 放入redis缓存中(10个小时)
                long timeout = 10 * 60 * 60;
                redisHelper.setCacheObject(key, data, timeout, TimeUnit.SECONDS);
            }
            return data;
        }

        // 读取缓存
        List<GroupLogs> data = redisHelper.getCacheObject(key);
        return data;
    }

    // 团购记录(跟团记录), 滚动部分
    @GetMapping("/group/groupActivity/logs2")
    public JsonResult groupLogs2(@RequestParam("id") Long groupId) {
        log.info("【团购滚动跟团记录:/order/group/groupActivity/logs2】params->groupId:{}", groupId);
        // 放入缓存
        String key = RedisConstant.RedisGroupLogsKey2 + groupId;
        if (redisHelper.hasKey(key) == false) {
            int total = 0;
            int orderTotal = getOrderNumByGroupId(groupId);
            if (orderTotal < 10) {
                total = orderTotal;
            } else {
                // 随机 [10,20] 之间的数字
                total = ThreadLocalRandom.current().nextInt(10, 21);
            }

            // 生成记录
            List<GroupLogs> data = this.getDayGroupLogs2(total, groupId);

            // 放入redis缓存中(1个小时)
            redisHelper.setCacheObject(key, data, RedisConstant.RedisGroupLogsExpired, TimeUnit.SECONDS);
        }

        // 读取缓存
        List<GroupLogs> data = redisHelper.getCacheObject(key);
        log.info("【团购滚动跟团记录返回】size:{},data:{}", data == null ? 0 : data.size(), JSON.toJSONString(data));
        return JsonResult.success(data);
    }


    // 订单数量 = 实际订单数量 + 虚拟数量
    // 使用Redis计数器来解决这个问题
    private Integer getRedisOrderTotal(Long groupId, long virtual) {

        // 订单销售数量缓存
        String key = RedisConstant.RedisOrderTotalKey + groupId;
        if (redisHelper.hasKey(key) == false) {

            // 查询订单销售数量, 再累加上虚拟数量
            // 不使用团购表里面的 order_total 字段嘛？
            Long total = orderInfoService.getMiniOrderSalesCount(groupId) + virtual;

            // 首次初始化基线: 用 SET 设置基数 + 30 天 TTL, 而非 INCRBY, 避免与"支付回调 +1"语义混淆
            // 一致性约定:
            // 1) key 不存在 = 首次加载, 此处从 DB 读取真实订单数 + 虚拟基数, SET 设一次性基线
            // 2) 后续仅由 OrderPaymentController 中 redisHelper.increment(key, 1L) 做增量累加, 不再覆写基数
            // 3) Redis 6.x INCR 不会重置 TTL; 若 key 30 天后过期失效, 下次访问重新走本分支初始化, 自愈
            redisHelper.setCacheObject(key, total, RedisConstant.RedisOrderTotalExpired, TimeUnit.SECONDS);
        }

        // 读取缓存: FastJson2 反序列化数字时按数值大小动态选择类型,
        // 小数值(<= Integer.MAX_VALUE)反序列化为 Integer, 大数值为 Long;
        // 支付回调走 Redis 原生 INCR 写入的也是数字字符串。
        // 因此此处不能按 Long 强转(否则 Integer cannot be cast to Long), 必须用 Number 接口统一取数值
        Object cached = redisHelper.getCacheObject(key);
        if (cached instanceof Number) {
            return ((Number) cached).intValue();
        }
        if (cached instanceof String) {
            // 兜底: 若 value 以字符串形式存储(如人工 set / 其他工具写入), 尝试解析
            try {
                return Integer.parseInt(((String) cached).trim());
            } catch (NumberFormatException e) {
                log.warn("Redis订单数量缓存值无法解析: key={}, value={}", key, cached);
            }
        }
        log.warn("Redis订单数量缓存值类型异常: key={}, type={}, value={}", key,
                cached == null ? "null" : cached.getClass().getName(), cached);
        return 0;
    }

    // 白天生成跟团记录, 固定部分
    private List<GroupLogs> getDayGroupLogs(int total, Long groupId) {

        // 要返回的数据
        List<GroupLogs> data = new ArrayList<>();

        // 查询团购商品列表(仅在线is_close=0商品, 已关闭商品不出现在跟团记录中)
        List<GbGroupActivityGoods> goodsList = groupActivityInfoService.getGroupActivityOnlineGoodsList(groupId);
        int size = goodsList.size();
        // 无在线商品时不生成跟团记录, 避免随机取商品下标越界
        if (size < 1) {
            return data;
        }

        // 生成 total 个脱敏手机号
        List<String> phoneList = PhoneGeneratorUtils.generateDesensitizePhone(total);

        // 生成total个 0 - 60之间的数字, 从大到小排序
        List<Integer> timeList = this.getSortedNums(total);

        // 循环添加数据
        for (int i = 0; i < total; i++) {

            String avatar = "/static/image/man.png";
            if (ThreadLocalRandom.current().nextInt(1, 6) % 2 == 0) avatar = "/static/image/woman.png";
            String mobile = phoneList.get(i);
            String time = timeList.get(i) + "分钟前";
            int temp = ThreadLocalRandom.current().nextInt(size);
            String name = goodsList.get(temp).getGoodsName();
            String num = String.valueOf(ThreadLocalRandom.current().nextInt(1, 3));

            data.add(new GroupLogs(avatar, mobile, time, name, num));
        }

        // 返回
        return data;
    }

    // 白天生成跟团记录, 滚动部分
    private List<GroupLogs> getDayGroupLogs2(int total, Long groupId) {

        // 要返回的数据
        List<GroupLogs> data = new ArrayList<>();

        // 查询团购商品列表(仅在线is_close=0商品, 已关闭商品不出现在跟团记录中)
        List<GbGroupActivityGoods> goodsList = groupActivityInfoService.getGroupActivityOnlineGoodsList(groupId);
        int size = goodsList.size();
        // 无在线商品时不生成跟团记录, 避免随机取商品下标越界
        if (size < 1) {
            return data;
        }

        // 生成 total 个脱敏手机号
        List<String> phoneList = PhoneGeneratorUtils.generateDesensitizePhone(total);

        // 循环添加数据
        for (int i = 0; i < total; i++) {

            String avatar = "/static/image/man.png";
            if (ThreadLocalRandom.current().nextInt(1, 6) % 2 == 0) avatar = "/static/image/woman.png";
            String mobile = phoneList.get(i);
            int temp = ThreadLocalRandom.current().nextInt(size);
            String name = goodsList.get(temp).getGoodsName();
            String num = String.valueOf(ThreadLocalRandom.current().nextInt(1, 3));
            data.add(new GroupLogs(avatar, mobile, "刚刚", name, num));
        }

        // 返回
        return data;
    }

    // 晚上生成跟团记录
    private List<GroupLogs> getNightGroupLogs(int total, Long groupId) {

        // 数量减半
        total = total / 2;
        if (total <= 0) total = 1;

        // 要返回的数据
        List<GroupLogs> data = new ArrayList<>();

        // 查询团购商品列表(仅在线is_close=0商品, 已关闭商品不出现在跟团记录中)
        List<GbGroupActivityGoods> goodsList = groupActivityInfoService.getGroupActivityOnlineGoodsList(groupId);
        int size = goodsList.size();

        // 生成 total 个脱敏手机号
        List<String> phoneList = PhoneGeneratorUtils.generateDesensitizePhone(total);

        // 循环添加数据
        for (int i = 0; i < total; i++) {

            String avatar = "/static/image/man.png";
            if (ThreadLocalRandom.current().nextInt(1, 6) % 2 == 0) avatar = "/static/image/woman.png";
            String mobile = phoneList.get(i);
            String name = "";
            if (size < 1) {
                //预防bug，但无这个情况
                name = "毛巾";
            } else {
                int temp = ThreadLocalRandom.current().nextInt(size);
                name = goodsList.get(temp).getGoodsName();
            }
            String num = String.valueOf(ThreadLocalRandom.current().nextInt(1, 3));
            data.add(new GroupLogs(avatar, mobile, "10分钟前", name, num));
        }

        // 返回
        return data;
    }

    // 判断当前时间是否早上6点到晚上20点之间
    private boolean isBetween6And20() {

        Date nowDate = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(nowDate);

        // 24小时制
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        // 总分钟数：06:00=360，20:00=1200
        int totalMin = hour * 60 + minute;
        return totalMin >= 6 * 60 && totalMin <= 20 * 60;
    }

    // 生成total个 5 - 50 之间的数字, 从大到小排序
    private List<Integer> getSortedNums(int total) {

        Set<Integer> set = new HashSet<>();
        while (set.size() < total) {
            int num = ThreadLocalRandom.current().nextInt(5, 51);
            set.add(num);
        }
        List<Integer> list = new ArrayList<>(set);
        Collections.sort(list, Collections.reverseOrder());
        return list;
    }

    /**
     * 真实跟团记录：基于支付成功订单数据
     */
    @GetMapping("/group/order/records")
    public JsonResult groupOrderRecords(@RequestParam(value = "groupId", required = false) Long groupId,
                                        @RequestParam(value = "limit", required = false) Integer limit) {
        log.info("真实跟团记录/group/order/records groupId:{}", groupId);
        if (groupId == null || groupId <= 0) {
            throw new BusinessException(OrderErrorCodeEnum.GROUP_ID_REQUIRED);
        }
        List<GroupOrderRecordResponse> data = orderInfoService.getGroupOrderRecordList(groupId, limit);
        log.info("返回信息/group/order/records:{}", JSON.toJSONString(data));
        return JsonResult.success(data);
    }

}
