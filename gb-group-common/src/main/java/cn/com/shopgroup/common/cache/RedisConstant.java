package cn.com.shopgroup.common.cache;

public class RedisConstant {


    public final static long WxMiniAccessTokenExpired = 6900;
    public final static String WxMiniAccessTokenKey = "WxMiniAccessToken";


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



    public final static long RedisOrderAddExpired = 10;
    public final static String RedisOrderAddKey = "OrderAdd:";


    public final static long RedisOrderPayExpired = 10;
    public final static String RedisOrderPayKey = "OrderPay:";



    public final static long RedisOrderCodeExpired = 1*24*60*60;
    public final static String RedisOrderCodeKey = "OrderCode:";



    public final static long RedisLeaderOrderDividePageExpired = 5*60*60;
    public final static String RedisLeaderOrderDividePageKey = "LeaderOrderDividePage";
    public final static String RedisLeaderOrderDivideTotalKey = "LeaderOrderDivideTotal";


    public final static long RedisSysAdminKaptchaExpired = 5*60;
    public final static String RedisSysAdminKaptchaKey = "SysAdminKaptcha:";


    public final static long RedisSysAdminTokenExpired = 24*60*60;
    public final static String RedisSysAdminTokenKey = "SysAdminToken:";


}