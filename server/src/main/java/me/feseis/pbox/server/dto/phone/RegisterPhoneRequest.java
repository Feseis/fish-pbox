package me.feseis.pbox.server.dto.phone;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 登记沙盒手机号入参。只有登记过的号码才能连接 App。
 */
public record RegisterPhoneRequest(
        @NotBlank(message = "phone 不能为空")
        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "phone 格式不正确")
        String phone
) {
}
