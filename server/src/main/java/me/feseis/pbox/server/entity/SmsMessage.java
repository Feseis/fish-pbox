package me.feseis.pbox.server.entity;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SmsMessage {

    private Long id;
    private String senderPhone;
    private String senderApiCode;
    private String senderName;
    private String receiverPhone;
    private String content;
    private String messageType;
    private Boolean receiverRead;
    private LocalDateTime readAt;
    private LocalDateTime deletedAt;
    private LocalDateTime createdAt;
}
