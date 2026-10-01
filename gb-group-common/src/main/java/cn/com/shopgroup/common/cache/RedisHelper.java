package cn.com.shopgroup.common.cache;

import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class RedisHelper {

    @Resource
    public RedisTemplate redisTemplate;

    // 本模块的Redis连接配置(spring.redis), 构建跨库连接时复用host/port/password
    @Resource
    private RedisProperties redisProperties;

    // 跨库RedisTemplate缓存: key=database编号, 跨模块缓存失效联动时懒加载构建, 一次构建长期复用
    private final Map<Integer, RedisTemplate<Object, Object>> crossDbTemplates = new ConcurrentHashMap<>();

    // 缓存基本的对象，Integer、String、实体类等等
    public <T> void setCacheObject(String key, T value) {
        redisTemplate.opsForValue().set(key, value);
    }

    // 缓存基本的对象，Integer、String、实体类等等
    // 支持设置缓存时间及其时间单位
    public <T> void setCacheObject(String key, T value, Long timeout, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
    }

    // 判断缓存 key 是否存在(null安全: 连接异常等场景返回false)
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    // 获得缓存的基本对象，先判断是否存在哦
    public <T> T getCacheObject(String key) {
        ValueOperations<String, T> operation = redisTemplate.opsForValue();
        return operation.get(key);
    }

    // 删除单个对象(null安全)
    public boolean deleteObject(String key) {
        return Boolean.TRUE.equals(redisTemplate.delete(key));
    }

    /**
     * 跨库删除缓存key: 各模块分库使用Redis后, 跨模块的缓存失效联动通过本方法删除对方库中的缓存;
     * 与本模块连接同一Redis实例(连接参数复用spring.redis配置), 仅database不同;
     * 目标库恰为本模块当前库时, 退化为普通删除
     *
     * @param key      缓存key
     * @param database 目标库编号(见 RedisDbConstant)
     */
    public boolean deleteObjectInDb(String key, int database) {
        if (redisProperties.getDatabase() == database) {
            return deleteObject(key);
        }
        RedisTemplate<Object, Object> template = crossDbTemplates.computeIfAbsent(database, this::buildCrossDbTemplate);
        return Boolean.TRUE.equals(template.delete(key));
    }

    // 基于本模块Redis连接配置, 构建指向指定database的独立Lettuce连接与模板(懒加载构建一次, 之后缓存复用)
    private RedisTemplate<Object, Object> buildCrossDbTemplate(int database) {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisProperties.getHost());
        config.setPort(redisProperties.getPort());
        config.setDatabase(database);
        String password = redisProperties.getPassword();
        if (password != null && password.length() > 0) {
            config.setPassword(RedisPassword.of(password));
        }
        LettuceConnectionFactory factory = new LettuceConnectionFactory(config);
        factory.afterPropertiesSet();

        // 序列化方式与主模板保持一致(key为String, value走FastJson2)
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        StringRedisSerializer keySerializer = new StringRedisSerializer();
        FastJson2JsonRedisSerializer valueSerializer = new FastJson2JsonRedisSerializer(Object.class);
        template.setKeySerializer(keySerializer);
        template.setValueSerializer(valueSerializer);
        template.setHashKeySerializer(keySerializer);
        template.setHashValueSerializer(valueSerializer);
        template.afterPropertiesSet();
        return template;
    }

    // 应用关闭时释放跨库连接工厂
    @PreDestroy
    public void destroyCrossDbTemplates() {
        for (RedisTemplate<Object, Object> template : crossDbTemplates.values()) {
            LettuceConnectionFactory factory = (LettuceConnectionFactory) template.getConnectionFactory();
            if (factory != null) {
                factory.destroy();
            }
        }
        crossDbTemplates.clear();
    }

    // 设置有效时间及其时间单位(null安全)
    public boolean expire(String key, long timeout, TimeUnit unit) {
        return Boolean.TRUE.equals(redisTemplate.expire(key, timeout, unit));
    }

    // 通用设置有效时间方法（时间单位：秒）
    public boolean expire(String key, long timeout) {
        return expire(key, timeout, TimeUnit.SECONDS);
    }

    // 通用设置有效时间方法（时间单位：天）(null安全)
    public boolean expireByDay(String key, long timeout) {
        return Boolean.TRUE.equals(redisTemplate.expire(key, timeout, TimeUnit.DAYS));
    }

    // 获取缓存剩余时间，单位为秒：-1=永久有效, -2=键不存在(null安全, 取不到时返回-1)
    public long getExpire(String key) {
        Long expire = redisTemplate.getExpire(key);
        return expire == null ? -1 : expire;
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

    // 获取List的长度(null安全: key不存在或取不到时返回0)
    public long getCacheListSize(String key) {
        Long size = redisTemplate.opsForList().size(key);
        return size == null ? 0 : size;
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

    // 删除List中的缓存数据(null安全)
    public <T> boolean removeCacheListItem(String key, T value) {
        Long removed = redisTemplate.opsForList().remove(key, 0, value);
        return removed != null && removed > 0;
    }


    /***********************************************************************/


    // 获取锁(null安全)
    public boolean getLock(String lock) {
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(lock, "1"));
    }

    // 获取指定时间的锁(单位是秒)(null安全)
    public boolean getLock(String lock, long timeout) {
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(lock, "1", timeout, TimeUnit.SECONDS));
    }

    // 释放锁(null安全)
    public boolean releaseLock(String lock) {
        return Boolean.TRUE.equals(redisTemplate.delete(lock));
    }


    /***********************************************************************/


    // 计数器累加指定数值(null安全: 取不到返回值时返回0)
    public long increment(String key, long value) {
        Long result = redisTemplate.opsForValue().increment(key, value);
        return result == null ? 0 : result;
    }

    // 计数器累加 1(null安全: 取不到返回值时返回0)
    public long increment(String key) {
        Long result = redisTemplate.opsForValue().increment(key);
        return result == null ? 0 : result;
    }

    // 计数器减少指定数值(null安全: 取不到返回值时返回0)
    public long decrement(String key, long value) {
        Long result = redisTemplate.opsForValue().decrement(key, value);
        return result == null ? 0 : result;
    }

    // 计数器减少 1(null安全: 取不到返回值时返回0)
    public long decrement(String key) {
        Long result = redisTemplate.opsForValue().decrement(key);
        return result == null ? 0 : result;
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

    // 添加ZSet内容(null安全)
    public boolean addZSetItem(String zSetKey, Integer id, Integer val) {

        return Boolean.TRUE.equals(redisTemplate.opsForZSet().add(zSetKey, id, val));
    }

    // 去掉ZSet内容(null安全: 取不到返回值时返回0)
    public long removeZSetItem(String zSetKey, Integer id) {

        Long removed = redisTemplate.opsForZSet().remove(zSetKey, id);
        return removed == null ? 0 : removed;
    }

    // 增加Zset分值(null安全: 取不到返回值时返回0)
    public double incrementZSetItemScore(String zSetKey, Integer id, Integer val) {

        Double score = redisTemplate.opsForZSet().incrementScore(zSetKey, id, val);
        return score == null ? 0 : score;
    }

    // 获取ZSet分值(member不存在时返回null, 避免拆箱NPE)
    public Double getZSetItemScore(String zSetKey, Integer id) {

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


    /***********************************************************************/


    // 添加延迟队列成员(ZSet, score=到期时间戳), 用于延迟消息队列
    public boolean addDelayQueueItem(String zSetKey, Object member, double score) {
        Boolean flag = redisTemplate.opsForZSet().add(zSetKey, member, score);
        return flag != null && flag;
    }

    // 取出并移除到期的延迟队列成员(score <= maxScore, 最多count个):
    // 对每个成员先remove成功(CAS)才算取出成功, 多实例并发消费时同一成员只会被一个实例取到
    @SuppressWarnings("unchecked")
    public List<Object> popDelayQueueItems(String zSetKey, double maxScore, int count) {
        Set<Object> members = redisTemplate.opsForZSet().rangeByScore(zSetKey, 0, maxScore, 0, count - 1);
        if (members == null || members.isEmpty()) {
            return new ArrayList<>();
        }
        List<Object> result = new ArrayList<>();
        for (Object member : members) {
            Long removed = redisTemplate.opsForZSet().remove(zSetKey, member);
            if (removed != null && removed > 0) {
                result.add(member);
            }
        }
        return result;
    }


}