package cn.com.shopgroup.service;

/**
 * 定时任务-团购活动相关服务
 */
public interface TaskGoodsService {

    /**
     * 到期团购活动自动下线:
     * 查询在线(is_close=0)且结束时间(end_time)早于等于当前时间的团购活动(每次最多limit条),
     * 将其置为下线(is_close=1); 逐条按"is_close=0"条件抢占式更新, 防并发/防重复下线
     *
     * @param nowTime 当前时间戳(秒)
     * @param limit   本次最多处理的活动数量
     * @return 实际下线的活动数量
     */
    int closeExpiredGroupActivity(int nowTime, int limit);

}
