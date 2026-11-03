package me.feseis.pbox.server.dto.sender;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 修改特殊发送方名称入参。只更新 senderName，apiCode 不变。
 */
public record UpdateSpecialSenderRequest(
        @NotBlank(message = "phone 不能为空")
        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "phone 格式不正确")
        String phone,

        @NotBlank(message = "senderName 不能为空")
        @Size(max = 64, message = "senderName 长度不能超过 64")
        String senderName
) {
}
