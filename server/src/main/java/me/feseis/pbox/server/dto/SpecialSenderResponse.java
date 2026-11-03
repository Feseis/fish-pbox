package me.feseis.pbox.server.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SpecialSenderResponse {

    private final String phone;
    private final String senderName;
    private final String apiCode;
}
