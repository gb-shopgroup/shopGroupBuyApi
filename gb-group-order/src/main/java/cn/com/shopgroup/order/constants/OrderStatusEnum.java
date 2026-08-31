package cn.com.shopgroup.order.constants;

/**
 * 订单状态常量维护
 *
 * @author lijing
 */
public enum OrderStatusEnum {

    UNPAID(0, "待支付"),

    PREPAID(1, "待收货"),

    PART_RECEIVED(2, "部分收货"),

    RECEIVED(3, "已提货"),

    REFUNDED(4, "已退款"),

    APPLY_REFUND(5, "售后"),

    CANCELED(6, "已取消");

    private int code;

    private String desc;

    OrderStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static OrderStatusEnum fromCode(int code) {
        for (OrderStatusEnum modeEnum : OrderStatusEnum.values()) {
            if (modeEnum.code == code) {
                return modeEnum;
            }
        }
        return null;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

}
