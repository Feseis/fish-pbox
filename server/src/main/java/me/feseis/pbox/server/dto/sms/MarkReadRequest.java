package me.feseis.pbox.server.dto.sms;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 标记已读入参：把接收方 id <= lastId 的未读短信置为已读。
 */
public record MarkReadRequest(
        @NotBlank(message = "receiverPhone 不能为空")
        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "receiverPhone 格式不正确")
        String receiverPhone,

        @Min(value = 0, message = "lastId 不能为负数")
        long lastId
) {
}
