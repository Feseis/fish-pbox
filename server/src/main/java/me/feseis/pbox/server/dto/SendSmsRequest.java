package me.feseis.pbox.server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendSmsRequest {

    @NotBlank(message = "senderPhone 不能为空")
    @Size(max = 20, message = "senderPhone 长度不能超过 20")
    private String senderPhone;

    @Size(max = 64, message = "apiCode 长度不能超过 64")
    private String apiCode;

    @NotBlank(message = "receiverPhone 不能为空")
    @Size(max = 20, message = "receiverPhone 长度不能超过 20")
    private String receiverPhone;

    @NotBlank(message = "content 不能为空")
    @Size(max = 1024, message = "content 长度不能超过 1024")
    private String content;

    @NotBlank(message = "messageType 不能为空")
    @Size(max = 16, message = "messageType 长度不能超过 16")
    private String messageType;
}
