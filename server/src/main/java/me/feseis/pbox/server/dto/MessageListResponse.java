package me.feseis.pbox.server.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MessageListResponse {

    private final List<SmsMessageResponse> messages;
    private final long lastId;
}
