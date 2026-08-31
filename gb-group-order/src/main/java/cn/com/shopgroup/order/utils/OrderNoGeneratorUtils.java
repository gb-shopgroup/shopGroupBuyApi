package cn.com.shopgroup.order.utils;

import java.security.SecureRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 订单号生成器（18位，高并发不重复）
 * 格式：1位业务 + 6位随机 + 5位用户ID + 6位序列号
 */
public class OrderNoGeneratorUtils {

    private static final SecureRandom RANDOM = new SecureRandom();

    // 全局序列号（0-999999）
    private static final AtomicInteger SEQUENCE = new AtomicInteger(0);

    // 业务类型
    public static final int BIZ_NORMAL = 1;   // 普通订单
    public static final int BIZ_GROUP = 2;    // 团购订单
    public static final int BIZ_FLASH = 3;    // 秒杀订单

    /**
     * 生成18位订单号
     * @param bizType 业务类型 (1-9)
     * @param userId 用户ID
     * @return 18位订单号
     */
    public static String generate(int bizType, Long userId) {
        // 各部分数据
        String[] parts = new String[4];
        parts[0] = String.valueOf(bizType);  // 1位
        parts[1] = String.format("%06d", RANDOM.nextInt(1000000));     // 6位随机
        parts[2] = String.format("%05d", Math.abs(userId % 100000)); // 5位用户
        parts[3] = String.format("%06d", SEQUENCE.incrementAndGet() % 1000000); // 6位序列

        // 打乱顺序（固定打乱规则，保证可解析）
        // 顺序：随机(6) + 用户(5) + 业务(1) + 序列(6) = 18位
        return parts[0] + parts[2] + parts[1] + parts[3];
    }

    /**
     * 生成普通订单号
     * @param userId 用户ID
     * @return 18位订单号
     */
    public static String generate(Long userId) {
        return generate(BIZ_NORMAL, userId);
    }

    /**
     * 生成团购订单号
     */
    public static String generateGroup(Long userId) {
        return generate(BIZ_GROUP, userId);
    }

    /**
     * 生成秒杀订单号
     */
    public static String generateFlash(Long userId) {
        return generate(BIZ_FLASH, userId);
    }

    // ============ 测试 ============
    public static void main(String[] args) {
        Long userId = 12345678L;

        System.out.println("普通订单: " + generate(userId));
        System.out.println("团购订单: " + generateGroup(userId));
        System.out.println("秒杀订单: " + generateFlash(userId));

        System.out.println("\n连续生成10个（看不出时间关系）：");
        for (int i = 0; i < 10; i++) {
            System.out.println(generate(userId));
        }

        System.out.println("\n不同用户：");
        for (int i = 0; i < 5; i++) {
            System.out.println("用户" + (10000 + i) + ": " + generateFlash(10000L + i));
        }
    }
}
