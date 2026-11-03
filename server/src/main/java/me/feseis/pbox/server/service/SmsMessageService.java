package me.feseis.pbox.server.service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

import lombok.RequiredArgsConstructor;
import me.feseis.pbox.server.dto.sms.MarkReadRequest;
import me.feseis.pbox.server.dto.sms.MessageListResponse;
import me.feseis.pbox.server.dto.sms.SendSmsByApiRequest;
import me.feseis.pbox.server.dto.sms.SendSmsByUserRequest;
import me.feseis.pbox.server.dto.sms.SmsMessageResponse;
import me.feseis.pbox.server.entity.SmsMessage;
import me.feseis.pbox.server.entity.SpecialSender;
import me.feseis.pbox.server.exp.BusinessException;
import me.feseis.pbox.server.exp.ErrorCode;
import me.feseis.pbox.server.mapper.SmsMessageMapper;
import me.feseis.pbox.server.mapper.SpecialSenderMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SmsMessageService {

    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");
    private static final int MAX_LIMIT = 100;
    private static final String TYPE_USER = "USER";
    private static final String TYPE_API = "API";

    private final SmsMessageMapper smsMessageMapper;
    private final SpecialSenderMapper specialSenderMapper;

    @Transactional
    public SmsMessageResponse sendByUser(SendSmsByUserRequest request) {
        return save(request.senderPhone().trim(), request.receiverPhone().trim(),
                request.content().trim(), TYPE_USER);
    }

    @Transactional
    public SmsMessageResponse sendByApi(SendSmsByApiRequest request) {
        SpecialSender sender = specialSenderMapper.findByApiCode(request.apiCode().trim());
        if (sender == null) {
            throw new BusinessException(ErrorCode.INVALID_API_CODE);
        }
        return save(sender.getPhone(), request.receiverPhone().trim(),
                request.content().trim(), TYPE_API);
    }

    public MessageListResponse list(String receiverPhone, long afterId, int limit) {
        String phone = requirePhone(receiverPhone);
        if (afterId < 0) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT);
        }
        if (limit < 1 || limit > MAX_LIMIT) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT);
        }

        List<SmsMessage> rows = smsMessageMapper.findAfter(phone, afterId, limit);
        List<SmsMessageResponse> messages = rows.stream()
                .map(this::toResponse)
                .toList();
        long lastId = rows.isEmpty() ? afterId : rows.get(rows.size() - 1).getId();
        return new MessageListResponse(messages, lastId);
    }

    @Transactional
    public int markRead(MarkReadRequest request) {
        String phone = requirePhone(request.receiverPhone());
        return smsMessageMapper.markRead(phone, request.lastId());
    }

    @Transactional
    public void delete(long id, String receiverPhone) {
        String phone = requirePhone(receiverPhone);
        if (smsMessageMapper.softDelete(id, phone) == 0) {
            throw new BusinessException(ErrorCode.MESSAGE_NOT_FOUND);
        }
    }

    private SmsMessageResponse save(String senderPhone, String receiverPhone, String content, String messageType) {
        SmsMessage message = new SmsMessage();
        message.setSenderPhone(senderPhone);
        message.setReceiverPhone(receiverPhone);
        message.setContent(content);
        message.setMessageType(messageType);
        smsMessageMapper.insert(message);

        SmsMessage saved = smsMessageMapper.findById(message.getId());
        if (saved == null) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR);
        }
        return toResponse(saved);
    }

    private String requirePhone(String receiverPhone) {
        if (receiverPhone == null || receiverPhone.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT);
        }
        return receiverPhone.trim();
    }

    private SmsMessageResponse toResponse(SmsMessage message) {
        return new SmsMessageResponse(
                message.getId(),
                message.getSenderPhone(),
                message.getSenderName(),
                message.getReceiverPhone(),
                message.getContent(),
                message.getMessageType(),
                message.getReadAt() != null,
                toOffset(message.getReadAt()),
                toOffset(message.getCreatedAt()));
    }

    private OffsetDateTime toOffset(LocalDateTime time) {
        return time == null ? null : time.atZone(ZONE).toOffsetDateTime();
    }
}
