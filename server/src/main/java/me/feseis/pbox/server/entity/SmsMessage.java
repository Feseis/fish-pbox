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

    private long id;
    private String senderPhone;
    private String senderName;
    private String receiverPhone;
    private String content;
    private String messageType;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
}
