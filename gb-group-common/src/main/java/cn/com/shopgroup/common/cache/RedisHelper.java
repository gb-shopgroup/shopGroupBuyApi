package cn.com.shopgroup.common.cache;

import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
public class RedisHelper {

    @Resource
    public RedisTemplate redisTemplate;

    // 缓存基本的对象，Integer、String、实体类等等
    public <T> void setCacheObject(String key, T value) {
        redisTemplate.opsForValue().set(key, value);
    }

    // 缓存基本的对象，Integer、String、实体类等等
    // 支持设置缓存时间及其时间单位
    public <T> void setCacheObject(String key, T value, Long timeout, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
    }

    // 判断缓存 key 是否存在
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    // 获得缓存的基本对象，先判断是否存在哦
    public <T> T getCacheObject(String key) {
        ValueOperations<String, T> operation = redisTemplate.opsForValue();
        return operation.get(key);
    }

    // 删除单个对象
    public boolean deleteObject(String key) {
        return redisTemplate.delete(key);
    }

    // 设置有效时间及其时间单位
    public boolean expire(String key, long timeout, TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }

    // 通用设置有效时间方法（时间单位：秒）
    public boolean expire(String key, long timeout) {
        return expire(key, timeout, TimeUnit.SECONDS);
    }

    // 通用设置有效时间方法（时间单位：天）
    public boolean expireByDay(String key, long timeout) {
        return redisTemplate.expire(key, timeout, TimeUnit.DAYS);
    }

    // 获取缓存剩余时间，单位为秒，返回0代表为永久有效
    // 如果key没有设置过期时间或键不存在，将返回 null 或 -1
    public long getExpire(String key) {
        return redisTemplate.getExpire(key);
    }


    /***********************************************************************/


    // 向List的左端缓存数据
    public <T> long setCacheListLeft(String key, T value) {
        Long count = redisTemplate.opsForList().leftPush(key, value);
        return count == null ? 0 : count;
    }

    // 批量向List的左端缓存数据
    public <T> long setCacheListLeft(String key, List<T> values) {
        Long count = redisTemplate.opsForList().leftPushAll(key, values);
        return count == null ? 0 : count;
    }

    // 向List的右端缓存数据
    public <T> long setCacheListRight(String key, T value) {
        Long count = redisTemplate.opsForList().rightPush(key, value);
        return count == null ? 0 : count;
    }

    // 批量向List的右端缓存数据
    public <T> long setCacheListRight(String key, List<T> values) {
        Long count = redisTemplate.opsForList().rightPushAll(key, values);
        return count == null ? 0 : count;
    }

    // 获取List中指定索引 index 的数据
    public <T> T getCacheListItemIndex(String key, int index) {
        ListOperations<String, T> operations = redisTemplate.opsForList();
        return operations.index(key, index);
    }

    // 获得List中的缓存数据
    public <T> List<T> getCacheList(String key) {
        return redisTemplate.opsForList().range(key, 0, -1);
    }

    // 获得List中指定长度的缓存数据
    public <T> List<T> getCacheList(String key, int length) {
        return redisTemplate.opsForList().range(key, 0, length);
    }

    // 获取List的长度
    public long getCacheListSize(String key) {
        return redisTemplate.opsForList().size(key);
    }

    // 获取List中左端一个数据，这个数据会被移除
    public <T> T getCacheListItemLeft(String key) {
        ListOperations<String, T> operations = redisTemplate.opsForList();
        return operations.leftPop(key);
    }

    // 获取List中右端一个数据，这个数据会被移除
    public <T> T getCacheListItemRight(String key) {
        ListOperations<String, T> operations = redisTemplate.opsForList();
        return operations.rightPop(key);
    }

    // 删除List中的缓存数据
    public <T> boolean removeCacheListItem(String key, T value) {
        return redisTemplate.opsForList().remove(key, 0, value) > 0;
    }


    /***********************************************************************/


    // 获取锁
    public boolean getLock(String lock) {
        return redisTemplate.opsForValue().setIfAbsent(lock, "1");
    }

    // 获取指定时间的锁(单位是秒)
    public boolean getLock(String lock, long timeout) {
        return redisTemplate.opsForValue().setIfAbsent(lock, "1", timeout, TimeUnit.SECONDS);
    }

    // 释放锁
    public boolean releaseLock(String lock) {
        return redisTemplate.delete(lock);
    }


    /***********************************************************************/


    // 计数器累加指定数值
    public long increment(String key, long value) {
        return redisTemplate.opsForValue().increment(key, value);
    }

    // 计数器累加 1
    public long increment(String key) {
        return redisTemplate.opsForValue().increment(key);
    }

    // 计数器减少指定数值
    public long decrement(String key, long value) {
        return redisTemplate.opsForValue().decrement(key, value);
    }

    // 计数器减少 1
    public long decrement(String key) {
        return redisTemplate.opsForValue().decrement(key);
    }


    /***********************************************************************/


    // 添加Hash内容
    public void putHashMap(String hashKey, String key, String val) {

        redisTemplate.opsForHash().put(hashKey, key, val);
    }

    // 获取Hash内容
    public Object getHashMap(String hashKey, String key) {

        return redisTemplate.opsForHash().get(hashKey, key);
    }

    // 查看ZSet内容是否存在
    public boolean isZSetItem(String zSetKey, Integer id) {

        Double score = redisTemplate.opsForZSet().score(zSetKey, id);
        return score != null;
    }

    // 添加ZSet内容
    public boolean addZSetItem(String zSetKey, Integer id, Integer val) {

        return redisTemplate.opsForZSet().add(zSetKey, id, val);
    }

    // 去掉ZSet内容
    public long removeZSetItem(String zSetKey, Integer id) {

        return redisTemplate.opsForZSet().remove(zSetKey, id);
    }

    // 增加Zset分值
    public double incrementZSetItemScore(String zSetKey, Integer id, Integer val) {

        return redisTemplate.opsForZSet().incrementScore(zSetKey, id, val);
    }

    // 获取ZSet分值
    public double getZSetItemScore(String zSetKey, Integer id) {

        return redisTemplate.opsForZSet().score(zSetKey, id);
    }

    // 获取Zset列表分值最小的id
    public Integer getMiniIdFromZset(String zSetKey) {

        // 获取列表
        ZSetOperations<String, Object> zSetOps = redisTemplate.opsForZSet();

        // 升序取第0位，团长内收款金额最小的商户
        Set<Object> minMerchant = zSetOps.range(zSetKey, 0, 0);
        if (minMerchant == null || minMerchant.isEmpty()) {
            return null;
        }

        // 分值最小的id
        return Integer.valueOf(minMerchant.iterator().next().toString());
    }


}