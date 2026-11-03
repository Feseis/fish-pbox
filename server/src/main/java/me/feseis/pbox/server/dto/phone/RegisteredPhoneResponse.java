package me.feseis.pbox.server.dto.phone;

/**
 * 登记沙盒手机号出参。重复登记返回已存在的号码。
 */
public record RegisteredPhoneResponse(String phone) {
}
