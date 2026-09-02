package cn.com.shopgroup.order.controller.group;

import cn.com.shopgroup.common.cache.RedisConstant;
import cn.com.shopgroup.common.cache.RedisHelper;
import cn.com.shopgroup.common.utils.JsonResult;
import cn.com.shopgroup.common.utils.PhoneGeneratorUtils;
import cn.com.shopgroup.goods.http.response.group.GroupActivityResponse;
import cn.com.shopgroup.goods.http.response.group.GroupCategoryResponse;
import cn.com.shopgroup.goods.model.GbGroupActivityGoods;
import cn.com.shopgroup.goods.model.GbGroupActivityInfo;
import cn.com.shopgroup.goods.model.GbGroupCategoryInfo;
import cn.com.shopgroup.goods.service.GbGroupActivityInfoService;
import cn.com.shopgroup.goods.service.GbGroupCategoryInfoService;
import cn.com.shopgroup.order.http.request.MemberGroupListRequest;
import cn.com.shopgroup.order.http.response.GroupLogs;
import cn.com.shopgroup.order.http.response.MemberHomeGroupActResponse;
import cn.com.shopgroup.order.service.GbOrderInfoService;
import cn.com.shopgroup.user.http.response.ShopResponse;
import cn.com.shopgroup.user.model.GbOrgShopInfo;
import cn.com.shopgroup.user.service.GbOrgShopInfoService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
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
import java.util.List;
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


    // 团购分类列表（首页）
    @GetMapping("/group/groupActivity/cat")
    public JsonResult groupCat() {

        List<GbGroupCategoryInfo> lists = categoryService.getMiniGroupCategoryList();
        List<GroupCategoryResponse> data = GroupCategoryResponse.getGroupCategoryResponseList(lists);
        return JsonResult.success(data);
    }

    /*// 团购列表（首页）
    @GetMapping("/group/groupActivity/list")
    public JsonResult groupList(@RequestParam("leaderId") Long leaderId, @RequestParam("catId") Long catId,
                                @RequestParam("page") int page, @RequestParam("pageSize") int pageSize) {

        // 请求参数矫正
        if (page == 0) page = 1;
        if (pageSize == 0) pageSize = 10;
        if (pageSize > 100) pageSize = 100;

        // 查询数据库
        List<GbGroupActivityInfo> lists = groupActivityInfoService.getMiniGroupActivityList(leaderId, catId, page, pageSize);
        List<GroupActivityResponse> data = GroupActivityResponse.getGroupActivityResponseList(lists);

        // 实时获取团购销售数量
        for (GroupActivityResponse item : data) {
            item.setNum(this.getRedisOrderTotal(item.getId(), item.getNum2()));
        }

        // 暂时直接返回结果，如果缓存的话，会员头像如何更新？
        return JsonResult.success(data);
    }
*/
    // 用户首页-查询所有团购活动列表
    @PostMapping("/group/get/groupActivity/list")
    public JsonResult getGroupActiveList(@RequestBody MemberGroupListRequest request) {

        // 请求参数矫正
        int page = Optional.ofNullable(request.getPage()).orElse(1);
        int pageSize = Optional.ofNullable(request.getPageSize())
                .map(size -> Math.min(size, 100))
                .orElse(10);
        String activityName = request.getName();
        Long leaderId = request.getLeaderId();
        // 查询列表(flag 1、团长团查询 2 用户端查询) -7 不做任何处理，填充参数
        List<GbGroupActivityInfo> result = groupActivityInfoService.getMiniLeaderGroupList(2, leaderId, request.getCatId(), activityName, -7, page, pageSize);
        List<MemberHomeGroupActResponse> data = MemberHomeGroupActResponse.getGroupActResponseList(result);
        // g
        fillGroupLogList(data);
        for (MemberHomeGroupActResponse item : data) {
            item.setOrder(this.getRedisOrderTotal(item.getId(), item.getVirtual()));
        }
        return JsonResult.success(data);
    }

    /**
     * 批量填充商品列表的规格信息(包含规格值), 避免循环内 N+1 查询
     */
    private void fillGroupLogList(List<MemberHomeGroupActResponse> data) {
        if (CollectionUtils.isEmpty(data)) {
            return;
        }
        for (MemberHomeGroupActResponse item : data) {
            List<GroupLogs> logList = getGroupList(item.getId());
            if (!CollectionUtils.isEmpty(logList)) {
                item.setGroupLogs(logList);
            }
        }
    }

    // 团购数量（首页）
    @GetMapping("/group/groupActivity/count")
    public JsonResult groupCount(@RequestParam("leaderId") Long leaderId, @RequestParam("catId") Long catId) {

        long total = groupActivityInfoService.getMiniGroupActivityCount(leaderId, catId);
        return JsonResult.success(total);
    }

    // 团购详情(团长分享页面)---用户首页：团长更多好货也用
    @GetMapping("/group/groupActivity/info")
    public JsonResult groupInfo(@RequestParam("groupId") Long groupId) {

        // 先查看是否存在Redis缓存
        String key = RedisConstant.RedisGroupInfoKey + groupId;
        if (redisHelper.hasKey(key) == false) {

            // 根据id查询团购详情
            GbGroupActivityInfo item = groupActivityInfoService.getMiniGroupActivityInfo(groupId);
            GroupActivityResponse data = new GroupActivityResponse(item);

            // 放入Redis中缓存
            redisHelper.setCacheObject(key, data, RedisConstant.RedisGroupInfoExpired, TimeUnit.SECONDS);
        }

        // 使用缓存
        GroupActivityResponse cacheData = redisHelper.getCacheObject(key);

        // 订单销售数量
        cacheData.setNum(this.getRedisOrderTotal(groupId, cacheData.getNum2()));

        // 返回数据
        return JsonResult.success(cacheData);
    }

    // 团长店铺详情
    @GetMapping("/group/groupActivity/shop")
    public JsonResult groupShop(@RequestParam("leaderId") Long leaderId) {

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
        return JsonResult.success(data);
    }

    // 团购记录(跟团记录)
    // 早上6点到晚上8点之间，每个小时生成一批跟团记录（固定部分+滚动部分），
    // 固定部分打开页面就显示，滚动部分每隔几秒显示一条；
    // 晚上时间只生成固定跟团记录（锁定在19:00-20:00生成的订单）。
    @GetMapping("/group/groupActivity/logs")
    public JsonResult groupLogs(@RequestParam("groupId") Long groupId) {
        List<GroupLogs> data = getGroupList(groupId);
        return JsonResult.success(data);
    }

    private List<GroupLogs> getGroupList(Long groupId) {
        // 放入缓存
        String key = RedisConstant.RedisGroupLogsKey + groupId;
        if (redisHelper.hasKey(key) == false) {

            // 随机 [10,20] 之间的数字
            int total = ThreadLocalRandom.current().nextInt(10, 21);

            // 生成记录
            List<GroupLogs> data = new ArrayList<>();
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

        // 放入缓存
        String key = RedisConstant.RedisGroupLogsKey2 + groupId;
        if (redisHelper.hasKey(key) == false) {

            // 随机 [10,20] 之间的数字
            int total = ThreadLocalRandom.current().nextInt(10, 21);

            // 生成记录
            List<GroupLogs> data = this.getDayGroupLogs2(total, groupId);

            // 放入redis缓存中(1个小时)
            redisHelper.setCacheObject(key, data, RedisConstant.RedisGroupLogsExpired, TimeUnit.SECONDS);
        }

        // 读取缓存
        List<GroupLogs> data = redisHelper.getCacheObject(key);
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
            long total = orderInfoService.getMiniOrderSalesCount(groupId) + virtual;

            // 放入Redis中缓存
            redisHelper.increment(key, total); // 下单的时候累加这个数字
            redisHelper.expire(key, RedisConstant.RedisOrderTotalExpired, TimeUnit.SECONDS);
        }

        // 使用缓存(放入的时候是long类型,读取的时候却是int类型)
        Integer total = redisHelper.getCacheObject(key);
        return total;
    }

    // 白天生成跟团记录, 固定部分
    private List<GroupLogs> getDayGroupLogs(int total, Long groupId) {

        // 要返回的数据
        List<GroupLogs> data = new ArrayList<>();

        // 查询团购商品列表
        List<GbGroupActivityGoods> goodsList = groupActivityInfoService.getGroupActivityGoodsList(groupId);
        int size = goodsList.size();

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

        // 查询团购商品列表
        List<GbGroupActivityGoods> goodsList = groupActivityInfoService.getGroupActivityGoodsList(groupId);
        int size = goodsList.size();

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

        // 查询团购商品列表
        List<GbGroupActivityGoods> goodsList = groupActivityInfoService.getGroupActivityGoodsList(groupId);
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

}
