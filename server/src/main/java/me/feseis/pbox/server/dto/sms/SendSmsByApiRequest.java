package me.feseis.pbox.server.dto.sms;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * 特殊发送方投递短信入参。渠道由接口固定为 API，发送方手机号与名称由 API 代码查出。
 */
public record SendSmsByApiRequest(
        @NotBlank(message = "apiCode 不能为空")
        @Size(max = 64, message = "apiCode 长度不能超过 64")
        String apiCode,

        @NotBlank(message = "receiverPhone 不能为空")
        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "receiverPhone 格式不正确")
        String receiverPhone,

        @NotBlank(message = "content 不能为空")
        @Size(max = 1024, message = "content 长度不能超过 1024")
        String content
) {

    @Override
    public String toString() {
        return "SendSmsByApiRequest[apiCode=***, receiverPhone=" + receiverPhone + ", content=***]";
    }
}
