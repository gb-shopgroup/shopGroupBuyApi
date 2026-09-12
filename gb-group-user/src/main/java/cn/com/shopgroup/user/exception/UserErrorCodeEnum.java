package cn.com.shopgroup.user.exception;

import cn.com.shopgroup.common.exception.IErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 团长端 / 员工端 / 会员端(用户模块)业务错误码
 * <p>
 * 集中维护用户模块业务逻辑中出现的错误情况，业务处直接
 * {@code throw new BusinessException(UserErrorCodeEnum.XXX)}，
 * 避免失败提示语散落在 controller/service 字符串中。
 * <p>
 * code 遵循 JsonResult 协议：300 业务失败 / 400 无权限 / 500 系统异常；
 * 与既有前端 code 判断完全兼容（业务失败默认 300）。
 */
@Getter
@AllArgsConstructor
public enum UserErrorCodeEnum implements IErrorCode {

    /**
     * 请求头中解析不到团长id
     */
    LEADER_NOT_EXIST(300101, "lid不存在"),
    //token 不存在
    TOKEN_NOT_EXIST(300101, "token不存在"),

    //团长/会员用户不存在
    USER_NOT_EXIST(300102, "用户不存在"),

    /**
     * 员工不存在(sid 查不到)
     */
    STAFF_NOT_EXIST(300103, "sid查不到员工信息"),

    /**
     * 员工信息查询失败
     */
    STAFF_QUERY_ERROR(300104, "查询员工信息有误"),

    /**
     * 员工已存在
     */
    STAFF_EXISTED(300105, "员工已存在"),

    /**
     * 证件号码重复
     */
    CERT_NO_EXISTED(300106, "证件号码已经存在"),

    /**
     * 记录已审核，不允许再次修改
     */
    ALREADY_AUDITED(300107, "已审核, 不允许修改"),

    /**
     * 已加入黑名单
     */
    BLACKLIST_EXISTED(300108, "已经添加黑名单了"),

    /**
     * 黑名单记录不存在
     */
    BLACKLIST_NOT_EXIST(300109, "黑名单不存在"),

    /**
     * 店铺二维码已存在
     */
    QRCODE_EXISTED(300110, "二维码已经存在，二维码生成失败"),

    /**
     * 二维码生成失败
     */
    QRCODE_GEN_FAILED(300111, "二维码生成失败"),

    /**
     * 二维码上传失败
     */
    QRCODE_UPLOAD_FAILED(300112, "二维码上传失败"),

    /**
     * 获取微信 AccessToken 失败
     */
    ACCESS_TOKEN_FAILED(300113, "获取AccessToken失败"),

    /**
     * 未查询到相关数据
     */
    DATA_NOT_FOUND(300114, "未查到到相关信息"),
    //自己不能操作自己
    OPERATION_NOT_SELF(300114, "自己不能操作自己"),

    /**
     * 数据保存/更新失败(通用)
     */
    UPDATE_FAILED(300115, "操作失败"),

    REQUEST_PARAM_ILLEGAL(300116, "请求参数非法"),

    /**
     * 文件大小超限
     */
    FILE_SIZE_EXCEEDED(300117, "文件大小不能超过5MB"),

    /**
     * 上传文件为空
     */
    FILE_EMPTY(300118, "图片不能为空"),

    /**
     * 图片格式不支持
     */
    IMAGE_FORMAT_NOT_SUPPORTED(300119, "仅支持 jpg/png/gif/bmp 格式图片"),

    /**
     * 图片上传目录创建失败
     */
    DIRECTORY_CREATE_FAILED(300120, "图片上传目录创建失败"),

    /**
     * 图片文件保存失败
     */
    FILE_SAVE_FAILED(300121, "图片保存失败"),

    /**
     * 图片上传失败
     */
    IMAGE_UPLOAD_FAILED(300122, "上传失败"),

    /**
     * 图片不存在或读取失败
     */
    IMAGE_READ_FAILED(300123, "图片不存在或读取失败"),

    /**
     * 获取图片尺寸失败
     */
    IMAGE_SIZE_INVALID(300124, "获取图片尺寸失败"),

    STAFF_MUST_BU_MEMBER(300125, "添加的员工必须先注册用户信息"),

    LEADER_INFO_NOT_FIND(300126, "查询不到团长信息"),

    NOT_SUPPORT_DELETE_ACTION(300127, "暂不支持删除操作"),
    ;

    /**
     * 返回给前端的业务状态码
     */
    private final int code;

    /**
     * 返回给前端的提示信息
     */
    private final String message;
}
