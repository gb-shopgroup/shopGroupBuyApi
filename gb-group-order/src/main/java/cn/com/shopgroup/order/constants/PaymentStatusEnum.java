package cn.com.shopgroup.order.constants;

/**
 * 支付状态-常量维护
 *
 * @author lijing
 */
public enum PaymentStatusEnum {

    PENDING("pending", "支付中"),

    SUCCESS("success", "支付成功"),

    FAILED("failed", "支付失败"),

    REFUNDED("refunded", "退款");

    private String code;

    private String desc;

    PaymentStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static PaymentStatusEnum fromCode(String code) {
        for (PaymentStatusEnum modeEnum : PaymentStatusEnum.values()) {
            if (modeEnum.code == code) {
                return modeEnum;
            }
        }
        return null;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

}
