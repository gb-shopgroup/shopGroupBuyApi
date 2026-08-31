package cn.com.shopgroup.common.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;


public class MoneyUtil {


    private static final BigDecimal HUNDRED = new BigDecimal("100");

    /**
     * 分(Integer) → 元(Double)，保留2位小数
     * @param cent 单位：分
     * @return 元 double，精确两位小数
     */
    public static Double centToYuan(Integer cent) {

        if (cent == null) { return 0.00; }
        BigDecimal yuan = new BigDecimal(cent).divide(HUNDRED, 2, RoundingMode.HALF_UP);
        return yuan.doubleValue();
    }

    /**
     * 元(Double) → 分(Integer)，四舍五入
     * @param yuan 单位：元
     * @return 分 Integer
     */
    public static Integer yuanToCent(Double yuan) {

        if (yuan == null) { return 0; }
        BigDecimal bd = new BigDecimal(String.valueOf(yuan));
        BigDecimal centBd = bd.multiply(HUNDRED).setScale(0, RoundingMode.HALF_UP);
        return centBd.intValue();
    }


    public static Integer calcRateByCent(Integer cent, Integer rate) {

        if (cent == null || cent <= 0) { return 0; }
        BigDecimal centBd = new BigDecimal(cent);
        String RATE_VAL = "0.006";
        switch (rate){
            case 3: RATE_VAL = "0.003"; break;
            case 4: RATE_VAL = "0.004"; break;
            case 5: RATE_VAL = "0.005"; break;
            case 6: RATE_VAL = "0.006"; break;
            case 7: RATE_VAL = "0.007"; break;
            case 8: RATE_VAL = "0.008"; break;
            case 9: RATE_VAL = "0.009"; break;
            case 10: RATE_VAL = "0.01"; break;
        }
        BigDecimal newRate = new BigDecimal(RATE_VAL);
        BigDecimal resultBd = centBd.multiply(newRate).setScale(0, RoundingMode.HALF_UP);
        return resultBd.intValue();
    }


}
