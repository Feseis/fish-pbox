package me.feseis.pbox.server.dto.sender;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 注册特殊发送方入参。手机号登记后唯一，名称仅用于展示。
 */
public record CreateSpecialSenderRequest(
        @NotBlank(message = "phone 不能为空")
        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "phone 格式不正确")
        String phone,

        @NotBlank(message = "senderName 不能为空")
        @Size(max = 64, message = "senderName 长度不能超过 64")
        String senderName
) {
}
