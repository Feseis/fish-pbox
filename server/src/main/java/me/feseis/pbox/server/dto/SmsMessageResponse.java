package me.feseis.pbox.server.dto;

import java.time.OffsetDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SmsMessageResponse {

    private final Long id;
    private final String senderPhone;
    private final String senderName;
    private final String receiverPhone;
    private final String content;
    private final String messageType;
    private final boolean receiverRead;
    private final OffsetDateTime readAt;
    private final OffsetDateTime createdAt;
}
