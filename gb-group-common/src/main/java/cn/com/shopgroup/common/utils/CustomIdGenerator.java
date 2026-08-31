package cn.com.shopgroup.common.utils;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;

public class CustomIdGenerator {

    private static final int MAX_WORKER_ID = 31;  // 5位workerId，范围0-31
    private static final int MAX_DATA_CENTER_ID = 31;  // 5位dataCenterId，范围0-31
    private static final Pattern IPV4_PATTERN =
            Pattern.compile("^(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})$");
    private static final Pattern IPV6_PATTERN =
            Pattern.compile("^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$|^::([0-9a-fA-F]{1,4}:){0,6}[0-9a-fA-F]{1,4}$|" +
                    "^([0-9a-fA-F]{1,4}:){1,7}:|^([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}$");

    private CustomIdGenerator() {
    }

    private static final Snowflake idWorker = IdUtil.getSnowflake(generateWorkerIdWithProcessInfo(), generateDataCenterId());

    /**
     * 生成WorkerId（自动支持IPv4和IPv6）
     *
     * @return
     */
    public static Long generateUUID() {
        return idWorker.nextId();
    }

    public static void main(String[] args) {
        int machineId = Integer.parseInt(
                System.getenv().getOrDefault("MACHINE_ID", "0")
        );
        System.out.println(machineId);
        for (int i = 0; i < 10; i++) {
            System.out.println(generateUUID());
        }
    }

    /**
     * 生成WorkerId（自动支持IPv4和IPv6）
     * 策略：
     * 1. 尝试获取合适的网卡IP
     * 2. 对IP地址进行哈希计算
     * 3. 确保workerId在0-31范围内
     */
    private static long generateWorkerId() {
        try {
            String ip = getSuitableIpAddress();
            if (ip == null) {
                throw new IllegalStateException("无法获取合适的IP地址");
            }

            return calculateWorkerIdFromIp(ip);
        } catch (Exception e) {
            // 失败时使用随机数，但记录警告
            System.err.println("生成workerId失败，使用随机workerId: " + e.getMessage());
            return ThreadLocalRandom.current().nextLong(0, MAX_WORKER_ID + 1);
        }
    }

    /**
     * 生成DataCenterId
     */
    private static long generateDataCenterId() {
        long dataCenterId = 0;
        try {
            String ip = getSuitableIpAddress();
            if (ip == null) {
                throw new IllegalStateException("无法获取合适的IP地址");
            }

            dataCenterId = calculateDataCenterIdFromIp(ip);
        } catch (Exception e) {
        }
        System.out.println("DataCenterId: " + dataCenterId);
        return dataCenterId;
    }

    /**
     * 获取合适的IP地址
     * 优先选择非回环、非本地的IPv4地址
     * 如果没有IPv4，则使用IPv6
     */
    private static String getSuitableIpAddress() throws SocketException {
        List<String> ipv4Addresses = new ArrayList<>();
        List<String> ipv6Addresses = new ArrayList<>();

        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();

            // 跳过回环接口、未启用的接口、虚拟接口
            if (networkInterface.isLoopback() ||
                    !networkInterface.isUp() ||
                    networkInterface.isVirtual()) {
                continue;
            }

            Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress address = addresses.nextElement();

                // 跳过回环地址、链路本地地址
                if (address.isLoopbackAddress() ||
                        address.isLinkLocalAddress() ||
                        address.isAnyLocalAddress()) {
                    continue;
                }

                if (address instanceof Inet4Address) {
                    ipv4Addresses.add(address.getHostAddress());
                } else if (address instanceof Inet6Address) {
                    // 跳过IPv6的链路本地地址和站点本地地址
                    if (!address.isLinkLocalAddress() && !address.isSiteLocalAddress()) {
                        ipv6Addresses.add(address.getHostAddress());
                    }
                }
            }
        }

        // 优先返回IPv4地址
        if (!ipv4Addresses.isEmpty()) {
            // 选择第一个IPv4地址，或者可以按特定策略选择
            return ipv4Addresses.get(0);
        }

        // 如果没有IPv4，则使用IPv6
        if (!ipv6Addresses.isEmpty()) {
            return ipv6Addresses.get(0);
        }

        // 如果都没有，尝试获取本地主机地址
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            if (!localHost.isLoopbackAddress() && !localHost.isAnyLocalAddress()) {
                return localHost.getHostAddress();
            }
        } catch (UnknownHostException e) {
            // 忽略异常
        }

        return null;
    }

    /**
     * 从IP地址计算WorkerId
     */
    private static long calculateWorkerIdFromIp(String ip) {
        int hash = hashIpAddress(ip);
        return Math.abs(hash) % (MAX_WORKER_ID + 1);
    }

    /**
     * 从IP地址计算DataCenterId
     */
    private static long calculateDataCenterIdFromIp(String ip) {
        // 对IP进行不同的哈希计算，确保与workerId不同
        int hash = hashIpAddress(ip + "_dc");
        return Math.abs(hash) % (MAX_DATA_CENTER_ID + 1);
    }

    /**
     * 统一的IP地址哈希计算（支持IPv4和IPv6）
     */
    private static int hashIpAddress(String ip) {
        if (isIPv4Address(ip)) {
            return hashIPv4(ip);
        } else if (isIPv6Address(ip)) {
            return hashIPv6(ip);
        } else {
            // 如果不是有效的IP地址，使用字符串哈希
            return ip.hashCode();
        }
    }

    /**
     * 判断是否为IPv4地址
     */
    private static boolean isIPv4Address(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }

        if (!IPV4_PATTERN.matcher(ip).matches()) {
            return false;
        }

        // 验证每个数字是否在0-255之间
        String[] parts = ip.split("\\.");
        for (String part : parts) {
            int num = Integer.parseInt(part);
            if (num < 0 || num > 255) {
                return false;
            }
        }

        return true;
    }

    /**
     * 判断是否为IPv6地址
     */
    private static boolean isIPv6Address(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }

        // 简化验证，实际IPv6地址可能更复杂
        return ip.contains(":");
    }

    /**
     * IPv4哈希计算
     */
    private static int hashIPv4(String ip) {
        try {
            String[] segments = ip.split("\\.");

            // 使用IP的最后一段进行计算
            int lastSegment = Integer.parseInt(segments[3]);

            // 也可以结合更多段
            int thirdSegment = Integer.parseInt(segments[2]);

            // 使用简单的哈希组合
            return (thirdSegment << 8) | lastSegment;
        } catch (Exception e) {
            return ip.hashCode();
        }
    }

    /**
     * IPv6哈希计算
     */
    private static int hashIPv6(String ip) {
        try {
            // 标准化IPv6地址
            InetAddress inetAddress = InetAddress.getByName(ip);
            byte[] addressBytes = inetAddress.getAddress();

            if (addressBytes.length == 16) { // IPv6地址是16字节
                // 将IPv6地址转换为哈希值
                int hash = 0;

                // 方法1：使用IPv6地址的最后4个字节
                for (int i = 12; i < 16; i++) { // 取最后4个字节
                    hash = 31 * hash + (addressBytes[i] & 0xFF);
                }

                // 方法2：使用完整的IPv6地址计算哈希
                // int fullHash = 0;
                // for (byte b : addressBytes) {
                //     fullHash = 31 * fullHash + (b & 0xFF);
                // }

                return hash;
            } else {
                // 如果不是16字节，回退到字符串哈希
                return ip.hashCode();
            }
        } catch (UnknownHostException e) {
            return ip.hashCode();
        }
    }

    /**
     * 高级版本：结合IP地址和进程ID
     */
    private static long generateWorkerIdWithProcessInfo() {
        long workId;
        try {
            String ip = getSuitableIpAddress();
            if (ip == null) {
                ip = "unknown";
            }

            // 获取进程ID
            long pid = getProcessId();

            // 结合IP和PID生成哈希
            String combined = ip + "_" + pid;
            int hash = combined.hashCode();

            workId = Math.abs(hash) % (MAX_WORKER_ID + 1);
        } catch (Exception e) {
            workId = ThreadLocalRandom.current().nextLong(0, MAX_WORKER_ID + 1);
        }
        System.out.println("Generated Worker ID: " + workId);
        return workId;
    }

    /**
     * 高级版本：结合IP地址、主机名和进程ID
     */
    private static long generateWorkerIdWithFullInfo() {
        try {
            String ip = getSuitableIpAddress();
            if (ip == null) {
                ip = "unknown";
            }

            String hostname = InetAddress.getLocalHost().getHostName();
            long pid = getProcessId();

            // 结合多个因素生成更稳定的workerId
            String combined = ip + "_" + hostname + "_" + pid;

            // 使用更复杂的哈希算法
            int hash = murmurHash32(combined.getBytes());

            return Math.abs(hash) % (MAX_WORKER_ID + 1);
        } catch (Exception e) {
            return ThreadLocalRandom.current().nextLong(0, MAX_WORKER_ID + 1);
        }
    }

    /**
     * 获取进程ID Java 9+ 的方式
     */
   /* private static long getProcessId() {
        try {
            return ProcessHandle.current().pid();
        } catch (Exception e) {
            try {
                // Java 8 的方式
                String processName = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
                if (processName.contains("@")) {
                    return Long.parseLong(processName.split("@")[0]);
                }
            } catch (Exception ex) {
                // 如果都失败，使用线程ID
                return Thread.currentThread().getId();
            }
        }
        return Thread.currentThread().getId();
    }*/

    /**
     * 获取进程ID - JDK 8 兼容
     */
    private static long getProcessId() {
        try {
            RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
            String name = runtime.getName();
            // name 格式: "pid@hostname"
            int atIndex = name.indexOf('@');
            if (atIndex > 0) {
                return Long.parseLong(name.substring(0, atIndex));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1; // 获取失败返回 -1
    }

    /**
     * MurmurHash3 32位哈希算法
     */
    private static int murmurHash32(byte[] data) {
        int c1 = 0xcc9e2d51;
        int c2 = 0x1b873593;
        int r1 = 15;
        int r2 = 13;
        int m = 5;
        int n = 0xe6546b64;

        int hash = 0x9747b28c; // 种子

        int length = data.length;
        int blocks = length / 4;

        // 处理4字节块
        for (int i = 0; i < blocks; i++) {
            int k = (data[i * 4] & 0xFF) |
                    ((data[i * 4 + 1] & 0xFF) << 8) |
                    ((data[i * 4 + 2] & 0xFF) << 16) |
                    ((data[i * 4 + 3] & 0xFF) << 24);

            k *= c1;
            k = (k << r1) | (k >>> (32 - r1));
            k *= c2;

            hash ^= k;
            hash = (hash << r2) | (hash >>> (32 - r2));
            hash = hash * m + n;
        }

        // 处理剩余字节
        int remaining = length % 4;
        if (remaining != 0) {
            int k = 0;
            for (int i = 0; i < remaining; i++) {
                k ^= (data[length - remaining + i] & 0xFF) << (i * 8);
            }
            k *= c1;
            k = (k << r1) | (k >>> (32 - r1));
            k *= c2;
            hash ^= k;
        }

        hash ^= length;
        hash ^= (hash >>> 16);
        hash *= 0x85ebca6b;
        hash ^= (hash >>> 13);
        hash *= 0xc2b2ae35;
        hash ^= (hash >>> 16);

        return hash;
    }

    /**
     * 获取IPv6的缩写形式
     */
    private static String getShortIPv6(String ipv6) {
        try {
            InetAddress address = InetAddress.getByName(ipv6);
            if (address instanceof Inet6Address) {
                // 规范化IPv6地址
                byte[] bytes = address.getAddress();
                if (bytes.length == 16) {
                    // 将字节数组转换为16进制字符串
                    BigInteger bigInt = new BigInteger(1, bytes);
                    String hex = bigInt.toString(16);

                    // 确保长度为32
                    while (hex.length() < 32) {
                        hex = "0" + hex;
                    }

                    // 压缩IPv6地址（去除前导零）
                    StringBuilder compressed = new StringBuilder();
                    String[] groups = hex.split("(?<=\\G.{4})");

                    boolean zeroCompressed = false;
                    for (int i = 0; i < groups.length; i++) {
                        // 去除前导零
                        String group = groups[i].replaceFirst("^0+(?!$)", "");
                        if (group.equals("0") && !zeroCompressed) {
                            // 找到连续的0块
                            int j = i;
                            while (j < groups.length && groups[j].equals("0000")) {
                                j++;
                            }
                            if (j - i > 1) { // 至少有两个连续的0块
                                compressed.append(":");
                                zeroCompressed = true;
                                i = j - 1;
                                continue;
                            }
                        }
                        compressed.append(group);
                        if (i < groups.length - 1) {
                            compressed.append(":");
                        }
                    }

                    return compressed.toString();
                }
            }
        } catch (UnknownHostException e) {
            // 忽略异常
        }
        return ipv6;
    }

}