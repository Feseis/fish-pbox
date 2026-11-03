package me.feseis.pbox.server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateSpecialSenderRequest {

    @NotBlank(message = "phone 不能为空")
    @Size(max = 20, message = "phone 长度不能超过 20")
    private String phone;

    @NotBlank(message = "senderName 不能为空")
    @Size(max = 64, message = "senderName 长度不能超过 64")
    private String senderName;
}
