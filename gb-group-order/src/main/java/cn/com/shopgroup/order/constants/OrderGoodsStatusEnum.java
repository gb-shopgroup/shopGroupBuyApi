package cn.com.shopgroup.order.constants;

/**
 * 订单----》（商品售售-退款-后状态）常量维护
 *
 * @author lijing
 */
public enum OrderGoodsStatusEnum {

    NO_APPLY(0, "默认不显示，判断需大于0展示具体"),

    APPLY_AGREE(1, "待审核"),

    AGREE(2, "同意"),

    DISAGREE(3, "不同意");

    private int code;

    private String desc;

    OrderGoodsStatusEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static OrderGoodsStatusEnum fromCode(int code) {
        for (OrderGoodsStatusEnum modeEnum : OrderGoodsStatusEnum.values()) {
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
