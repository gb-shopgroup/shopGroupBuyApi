package cn.com.shopgroup.common.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PhoneGeneratorUtils {

    // 手机号前缀
    private static final String[] PREFIX = {"130", "131", "132", "133", "134", "135", "136", "137", "138", "139",
            "144", "147", "149",
            "150", "151", "152", "153", "155", "156", "157", "158", "159",
            "170", "171", "173", "175", "176", "177", "178",
            "180", "181", "182", "183", "184", "185", "186", "187", "188", "189",
            "191", "192", "193", "199"};

    private static final Random random = new Random();

    /**
     * 生成指定数量脱敏手机号
     * @param count 数量
     * @return 脱敏手机号集合
     */
    public static List<String> generateDesensitizePhone(int count) {

        List<String> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            String fullPhone = getRandomPhone();
            String desPhone = desensitizePhone(fullPhone);
            list.add(desPhone);
        }
        return list;
    }

    /**
     * 随机生成完整11位手机号
     */
    public static String getRandomPhone() {

        // 随机前缀
        String prefix = PREFIX[random.nextInt(PREFIX.length)];
        // 后8位随机数字
        StringBuilder suffix = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            suffix.append(random.nextInt(10));
        }
        return prefix + suffix;
    }

    /**
     * 手机号脱敏：前3 + **** + 后4
     */
    public static String desensitizePhone(String phone) {

        if (phone == null || phone.length() != 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }


}
