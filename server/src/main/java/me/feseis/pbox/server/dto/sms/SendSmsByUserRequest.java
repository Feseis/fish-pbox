package me.feseis.pbox.server.dto.sms;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 普通用户投递短信入参。渠道由接口固定为 USER，请求体不再携带消息类型与 API 代码。
 */
public record SendSmsByUserRequest(
        @NotBlank(message = "senderPhone 不能为空")
        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "senderPhone 格式不正确")
        String senderPhone,

        @NotBlank(message = "receiverPhone 不能为空")
        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "receiverPhone 格式不正确")
        String receiverPhone,

        @NotBlank(message = "content 不能为空")
        @Size(max = 1024, message = "content 长度不能超过 1024")
        String content
) {
}
