package cn.com.shopgroup.goods.exception;

import cn.com.shopgroup.common.exception.IErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 团长端商品 / 团购活动管理(商品模块)业务错误码
 *
 * 集中维护商品模块业务逻辑中出现的错误情况，业务处直接
 * {@code throw new BusinessException(GoodsErrorCodeEnum.XXX)}，
 * 避免失败提示语散落在 controller/service 字符串中。
 *
 * code 遵循 JsonResult 协议：300 业务失败 / 400 无权限 / 500 系统异常；
 * 与既有前端 code 判断完全兼容（业务失败默认 300）。
 */
@Getter
@AllArgsConstructor
public enum GoodsErrorCodeEnum implements IErrorCode {

    /** 请求头中解析不到团长id */
    LEADER_NOT_EXIST(300201, "lid不存在"),

    /** 员工不存在 */
    STAFF_NOT_EXIST(300202, "未查询到员工信息"),

    /** 团购活动不存在 */
    GROUP_NOT_EXIST(300203, "未查询到团购活动信息"),

    /** 团购商品不存在 */
    GROUP_GOODS_NOT_EXIST(300204, "团购商品不存在"),

    /** 商品不存在 */
    GOODS_NOT_EXIST(300205, "商品不存在"),

    /** 团购进行中，不允许修改 */
    GROUP_ONGOING(300206, "团购进行中, 不允许修改"),

    /** 商品正在团购中，不允许修改 */
    GOODS_GROUPING(300207, "该商品正在团购中, 不允许修改"),

    /** 活动起止时间不合法 */
    TIME_RANGE_INVALID(300208, "结束时间必须晚于开始时间"),

    /** 新增团购活动缺少自提点 */
    PICKUP_POINT_REQUIRED(300209, "新增团购活动时请添加自提点"),

    /** SKU 数据不能为空 */
    SKU_REQUIRED(3002210, "SKU数据不能为空"),

    /** 平台未审核通过，不允许上线 */
    NOT_AUDITED(300212, "平台未审核, 不允许打开上线"),

    /** 获取微信 AccessToken 失败 */
    ACCESS_TOKEN_FAILED(300213, "获取AccessToken失败"),

    /** 修改失败 */
    UPDATE_FAILED(300214, "修改失败"),

    /** 分享海报生成失败 */
    POSTER_GEN_FAILED(300215, "分享海报生成失败"),
   // 请求参数id必须大于0
   REQUEST_ID_THAN_ZERO(300216, "请求参数id必须大于0"),
    ;

    /** 返回给前端的业务状态码 */
    private final int code;

    /** 返回给前端的提示信息 */
    private final String message;
}
