package cn.com.shopgroup.exception;

import cn.com.shopgroup.common.exception.IErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 管理后台(admin 模块)业务错误码
 *
 * 集中维护管理后台业务逻辑中出现的错误情况，业务处直接
 * {@code throw new BusinessException(AdminErrorCodeEnum.XXX)}，
 * 避免失败提示语散落在 controller/service 字符串中。
 *
 * code 遵循 JsonResult 协议：300 业务失败 / 400 无权限 / 500 系统异常；
 * 与既有前端 code 判断完全兼容（业务失败默认 300）。
 */
@Getter
@AllArgsConstructor
public enum AdminErrorCodeEnum implements IErrorCode {

    /** 管理员/后台用户不存在 */
    USER_NOT_EXIST(300501, "用户不存在"),

    /** 后台账号已停用 */
    USER_DISABLED(300502, "用户已关闭"),

    /** 小程序端用户不存在 */
    WX_USER_NOT_EXIST(300503, "小程序用户不存在"),

    /** 登录验证码不正确 */
    CAPTCHA_ERROR(300504, "验证码不对"),

    /** 登录密码不正确 */
    PASSWORD_ERROR(300505, "密码不正确"),

    /** 手机号已存在 */
    PHONE_EXISTED(300506, "手机号已存在"),

    /** 商户编号已存在 */
    MERCHANT_NO_EXISTED(300507, "商户编号已存在"),

    /** 团购标签不存在 */
    TAG_NOT_FOUND(300508, "未查询到标签信息"),

    /** 团购标签名称不能为空 */
    TAG_NAME_EMPTY(300509, "标签名称不能为空"),

    /** 团购标签名称已存在 */
    TAG_NAME_EXISTED(300510, "标签名称已存在"),

    /** 标签已被团购活动使用，不能删除 */
    TAG_IN_USE(300511, "该标签已被团购活动使用, 不可删除, 可改为停用"),

    /** 新增失败 */
    ADD_FAILED(300512, "添加失败"),

    /** 修改失败 */
    UPDATE_FAILED(300513, "修改失败"),

    /** 删除失败 */
    DELETE_FAILED(300514, "删除失败"),

    /** 标签id不能为空 */
    TAG_ID_EMPTY(300515, "标签id不能为空"),

    /** 生成验证码图片失败 */
    CAPTCHA_IMAGE_FAILED(300516, "生成验证码图片失败");

    /** 返回给前端的业务状态码 */
    private final int code;

    /** 返回给前端的提示信息 */
    private final String message;
}
