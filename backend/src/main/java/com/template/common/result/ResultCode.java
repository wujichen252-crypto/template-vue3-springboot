package com.template.common.result;

public enum ResultCode {

    SUCCESS(200, "ok"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未认证"),
    TOKEN_EXPIRED(401, "Token已过期"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),
    RATE_LIMIT(429, "请求过于频繁"),
    INTERNAL_ERROR(500, "服务器内部错误"),
    USER_EXISTS(400, "用户已存在"),
    USERNAME_OR_PASSWORD_ERROR(401, "用户名或密码错误");

    private final Integer code;
    private final String msg;

    ResultCode(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
