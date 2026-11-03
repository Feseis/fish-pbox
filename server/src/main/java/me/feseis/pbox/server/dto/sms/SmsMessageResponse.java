package me.feseis.pbox.server.dto.sms;

import java.time.OffsetDateTime;

/**
 * 短信出参。receiverRead 由 read_at 推导，不单独落库。
 */
public record SmsMessageResponse(
        long id,
        String senderPhone,
        String senderName,
        String receiverPhone,
        String content,
        String messageType,
        boolean receiverRead,
        OffsetDateTime readAt,
        OffsetDateTime createdAt
) {
}
