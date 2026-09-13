package cn.com.shopgroup.order.exception;

import cn.com.shopgroup.common.exception.IErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 团长端 / 会员端下单、核销、退款售后(订单模块)业务错误码
 *
 * 集中维护订单模块业务逻辑中出现的错误情况，业务处直接
 * {@code throw new BusinessException(OrderErrorCodeEnum.XXX)}，
 * 避免失败提示语散落在 controller/service 字符串中。
 *
 * code 遵循 JsonResult 协议：300 业务失败 / 400 无权限 / 500 系统异常；
 * 与既有前端 code 判断完全兼容（业务失败默认 300）。
 */
@Getter
@AllArgsConstructor
public enum OrderErrorCodeEnum implements IErrorCode {

    /** 会员未登录或 token 缺失 */
    TOKEN_NOT_EXIST(300301, "token不存在"),

    /** 会员用户不存在 */
    USER_NOT_EXIST(300302, "用户不存在"),

    /** 请求头中解析不到团长id */
    LEADER_NOT_EXIST(300303, "lid不存在"),

    /** memberId 参数错误 */
    MEMBER_ID_PARAM_ERROR(300304, "memberId参数错误"),

    /** 订单不存在 */
    ORDER_NOT_EXIST(300305, "订单不存在"),

    /** 订单下未查询到商品信息 */
    ORDER_GOODS_NOT_FOUND(300306, "该订单未查询到商品信息"),

    /** 下单未选择商品 */
    GOODS_REQUIRED(300307, "请选择商品后再下单"),

    /** 订单正在处理中，重复提交 */
    ORDER_PROCESSING(300308, "订单正在处理中，请勿重复提交"),

    /** 订单未支付，不能收货/核销 */
    ORDER_NOT_PAID(300309, "未支付, 不能收货"),

    /** 订单已退款，不能收货/核销 */
    ORDER_REFUNDED(300310, "已退款, 不能收货"),

    /** 退款审核参数错误 */
    REFUND_PARAM_ERROR(300312, "请查看请求参数，审核失败"),

    /** 申请退货商品数量不合法 */
    REFUND_QTY_ILLEGAL(300313, "申请退货商品数量不合法，审核失败"),

    /** 申请退货数量超过实际可退数量 */
    REFUND_QTY_EXCEEDED(300314, "申请退货商品数量大于实际可退数量，审核失败"),

    /** 二维码生成失败 */
    QRCODE_GEN_FAILED(300315, "二维码生成失败"),

    /** 获取微信 AccessToken 失败 */
    ACCESS_TOKEN_FAILED(300316, "获取AccessToken失败"),

    /** 团购活动id不能为空 */
    GROUP_ID_REQUIRED(300317, "团购活动id不能为空"),

    /** 用户未登录 */
    LOGIN_REQUIRED(300318, "请先登录"),

    /** 订单未支付 */
    PAYMENT_REQUIRED(300319, "订单未支付"),

    /** 微信订单状态查询失败 */
    WX_STATUS_QUERY_FAILED(300320, "查询失败，请稍后再试！"),

    /** 重复发起支付 */
    REPEAT_PAY_REQUEST(300321, "不要重复发起支付"),

    /** openid缺失 */
    OPENID_REQUIRED(300322, "openid不存在"),

    /** 订单超时不能支付 */
    ORDER_TIMEOUT(300323, "订单已超时不能支付"),

    /** 只有待支付订单才能支付 */
    ONLY_UNPAID_PAYABLE(300324, "只有待支付订单才能支付"),

    /** 团长信息有误 */
    LEADER_INFO_ERROR(300325, "团长信息有误"),

    /** 团长没有收款账户 */
    LEADER_NO_ACCOUNT(300326, "该团长没有收款账户"),

    /** 收款账户额度受限 */
    ACCOUNT_LIMIT_EXCEEDED(300327, "该团长下的收款账户收款额度已经全部受限制，暂时不能支付"),

    /** 支付失败 */
    PAY_FAILED(300328, "支付失败"),

    /** 订单未支付, 不能核销 */
    ORDER_UNPAID_NOT_WRITEOFF(300329, "订单未支付, 不能核销"),

    /** 订单已退款, 不能核销 */
    REFUNDED_NOT_WRITEOFF(300330, "订单已退款, 不能核销"),

    /** 核销数量超过订单剩余 */
    VERIFY_NUM_EXCEED(300331, "核销:商品数量大于订单剩余核销数"),

    /** 订单已发货 */
    ORDER_SENT(300332, "订单已发货"),

    /** 小程序码生成失败 */
    ERCODE_GEN_FAILED(300333, "二维码生成失败"),

    /** 订单未支付不能收货 */
    ORDER_UNPAID_NOT_RECEIPT(300334, "未支付, 不能收货"),

    /** 订单已退款不能收货 */
    ORDER_REFUNDED_NOT_RECEIPT(300335, "已退款, 不能收货"),

    /** 不能重复收货 */
    DUPLICATE_RECEIPT(300336, "不能重复收货"),

    /** 收货失败 */
    RECEIPT_FAILED(300337, "收货失败"),

    /** 未支付不能申请退款 */
    ORDER_UNPAID_NOT_REFUND(300338, "未支付, 不能申请退"),

    /** 已退款不能申请 */
    ORDER_REFUNDED_NOT_REFUND(300339, "已退款, 不能申请"),

    /** 退款类型不正确 */
    REFUND_TYPE_INVALID(300340, "退款类型不正确"),

    /** 申请退款的商品与订单不一致 */
    REFUND_GOODS_NOT_MATCH(300341, "申请退款的商品信息与订单不一致，请重新提交"),

    /** 申请退款金额超额 */
    REFUND_AMOUNT_EXCEED(300342, "申请退款总金额大于订单实付金额, 请重新申请"),

    /** 申请退款失败 */
    REFUND_APPLY_FAILED(300343, "申请退款失败"),

    /** 店铺信息不可用 */
    SHOP_INFO_UNAVAILABLE(300344, "店铺信息不可用"),
    //当前订单还有待审核数据，请联系团长处理后再申请
    ORDER_GOODS_NEED_APPROVE(300345, "当前订单还有待审核数据，请联系团长处理后再申请"),
    ;

    /** 返回给前端的业务状态码 */
    private final int code;

    /** 返回给前端的提示信息 */
    private final String message;
}
