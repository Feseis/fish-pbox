package me.feseis.pbox.server.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MarkReadRequest {

    @NotBlank(message = "receiverPhone 不能为空")
    @Size(max = 20, message = "receiverPhone 长度不能超过 20")
    private String receiverPhone;

    @NotNull(message = "lastId 不能为空")
    @Min(value = 0, message = "lastId 不能为负数")
    private Long lastId;
}
