package me.feseis.pbox.server.exp;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

    INVALID_ARGUMENT(HttpStatus.BAD_REQUEST, "参数不合法"),
    SENDER_ALREADY_EXISTS(HttpStatus.CONFLICT, "该手机号已登记为特殊发送方"),
    SENDER_NOT_FOUND(HttpStatus.NOT_FOUND, "特殊发送方不存在"),
    PHONE_NOT_FOUND(HttpStatus.NOT_FOUND, "手机号未登记"),
    INVALID_API_CODE(HttpStatus.FORBIDDEN, "API 代码无效"),
    MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "短信不存在"),
    DB_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "数据库不可用"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "服务内部错误");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
