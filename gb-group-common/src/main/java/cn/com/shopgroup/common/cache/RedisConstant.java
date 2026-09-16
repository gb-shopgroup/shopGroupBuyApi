package cn.com.shopgroup.common.cache;

public class RedisConstant {


    public final static long WxMiniAccessTokenExpired = 6900;
    public final static String WxMiniAccessTokenKey = "WxMiniAccessToken";
    // access_token刷新分布式锁, 保证同一时间只有一个实例向微信刷新
    public final static String WxMiniAccessTokenLockKey = "WxMiniAccessToken:lock";


    public final static long RedisShopInfoExpired = 30*24*60*60;
    public final static String RedisShopInfoKey = "ShopInfo:";


    public final static long RedisPointListExpired = 30*24*60*60;
    public final static String RedisPointListKey = "PointList:";


    public final static long RedisOrderTotalExpired = 30*24*60*60;
    public final static String RedisOrderTotalKey = "OrderTotal:";


    public final static long RedisGroupInfoExpired = 30*24*60*60;
    public final static String RedisGroupInfoKey = "GroupInfo:";


    public final static long RedisGroupGoodsListExpired = 30*24*60*60;
    public final static String RedisGroupGoodsListKey = "GroupGoodsList:";


    public final static long RedisGroupLogsExpired = 1*60*60;
    public final static String RedisGroupLogsKey = "GroupLogs:";
    public final static String RedisGroupLogsKey2 = "GroupLogs2:";



    public final static long RedisOrderAddExpired = 3;
    public final static String RedisOrderAddKey = "OrderAdd:";


    public final static long RedisOrderPayExpired = 10;
    public final static String RedisOrderPayKey = "OrderPay:";


    // 退款结果回调幂等键(按易宝退款单号), 防止易宝重复通知导致重复落库
    public final static long RedisRefundNotifyExpired = 7*24*60*60;
    public final static String RedisRefundNotifyKey = "RefundNotify:";



    public final static long RedisOrderCodeExpired = 1*24*60*60;
    public final static String RedisOrderCodeKey = "OrderCode:";

    // 用户订单核销小程序码(base64): 小程序码永久有效, 生成一次后永久缓存复用, 后续请求不再依赖微信接口(access_token)
    public final static String WxMiniOrderErCodeKey = "WxMiniOrderErCode:";



    public final static long RedisLeaderOrderDividePageExpired = 5*60*60;
    public final static String RedisLeaderOrderDividePageKey = "LeaderOrderDividePage";
    public final static String RedisLeaderOrderDivideTotalKey = "LeaderOrderDivideTotal";


    public final static long RedisSysAdminKaptchaExpired = 5*60;
    public final static String RedisSysAdminKaptchaKey = "SysAdminKaptcha:";


    public final static long RedisSysAdminTokenExpired = 24*60*60;
    public final static String RedisSysAdminTokenKey = "SysAdminToken:";


    public final static long RedisMemberTokenExpired = 30*24*60*60;
    public final static String RedisMemberTokenKey = "MemberToken:";


    // 订单支付超时延迟队列(ZSet结构, member=订单号, score=订单超时可取消的时间戳=下单时间+15分钟):
    // 下单时入队, 延迟15分钟仍未支付(pay_time=0且status=0)的订单由定时任务自动取消并回补库存
    public final static String RedisOrderPayDelayQueueKey = "OrderPayDelayQueue";

}