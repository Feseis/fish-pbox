package me.feseis.pbox.server.dto.sms;

import java.util.List;

/**
 * 轮询出参。lastId 是本次返回的最大 id，客户端作为下次 afterId。
 */
public record MessageListResponse(
        List<SmsMessageResponse> messages,
        long lastId
) {
}
