package me.feseis.pbox.server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterPhoneRequest {

    @NotBlank(message = "phone 不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "phone 格式不正确")
    private String phone;
}
