package cn.com.shopgroup.common.cache;

/**
 * 各微服务模块 Redis 数据库编号分配:
 * - 用户模块(gb-group-user)使用 db2
 * - 商品模块(gb-group-goods)使用 db3
 * - 订单模块(gb-group-order)使用 db4
 * - 任务模块(gb-group-task)使用 db4(消费订单模块写入的支付延迟队列与分账游标, 必须与订单模块同库)
 * - 后台管理模块(gb-group-admin)使用 db5(仅使用自有的管理员Token/验证码缓存)
 *
 * 各模块分库后, 跨模块的缓存失效联动(如商品模块上下线团购后清订单模块的团购详情缓存,
 * 用户模块保存店铺后清订单模块的店铺信息缓存)通过 RedisHelper.deleteObjectInDb(key, 目标库) 跨库删除,
 * 保证拆库后业务行为与拆库前完全一致
 */
public class RedisDbConstant {

    /** 用户模块(gb-group-user) */
    public static final int DB_USER = 2;
    /** 商品模块(gb-group-goods) */
    public static final int DB_GOODS = 3;
    /** 订单模块(gb-group-order)与任务模块(gb-group-task) */
    public static final int DB_ORDER = 4;
    /** 后台管理模块(gb-group-admin) */
    public static final int DB_ADMIN = 5;

    private RedisDbConstant() {
    }
}
