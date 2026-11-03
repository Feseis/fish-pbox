package me.feseis.pbox.server.service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import me.feseis.pbox.server.dto.MarkReadRequest;
import me.feseis.pbox.server.dto.MessageListResponse;
import me.feseis.pbox.server.dto.SendSmsRequest;
import me.feseis.pbox.server.dto.SmsMessageResponse;
import me.feseis.pbox.server.entity.SmsMessage;
import me.feseis.pbox.server.entity.SpecialSender;
import me.feseis.pbox.server.exp.BusinessException;
import me.feseis.pbox.server.exp.ErrorCode;
import me.feseis.pbox.server.mapper.SmsMessageMapper;
import me.feseis.pbox.server.mapper.SpecialSenderMapper;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SmsMessageService {

    private static final ZoneId ZONE = ZoneId.of("Asia/Shanghai");
    private static final Set<String> MESSAGE_TYPES = Set.of("CODE", "TEXT");
    private static final int MAX_LIMIT = 100;

    private final SmsMessageMapper smsMessageMapper;
    private final SpecialSenderMapper specialSenderMapper;

    public SmsMessageService(SmsMessageMapper smsMessageMapper, SpecialSenderMapper specialSenderMapper) {
        this.smsMessageMapper = smsMessageMapper;
        this.specialSenderMapper = specialSenderMapper;
    }

    @Transactional
    public SmsMessageResponse send(SendSmsRequest request) {
        String senderPhone = request.getSenderPhone().trim();
        String receiverPhone = request.getReceiverPhone().trim();
        String content = request.getContent().trim();
        String messageType = request.getMessageType().trim().toUpperCase(Locale.ROOT);

        if (!MESSAGE_TYPES.contains(messageType)) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT);
        }

        String apiCode = normalizeApiCode(request.getApiCode());
        if (apiCode != null) {
            SpecialSender sender = specialSenderMapper.findByPhone(senderPhone);
            if (sender == null || !sender.getApiCode().equals(apiCode)) {
                throw new BusinessException(ErrorCode.INVALID_API_CODE);
            }
        }

        SmsMessage message = new SmsMessage();
        message.setSenderPhone(senderPhone);
        message.setSenderApiCode(apiCode);
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
        String phone = requirePhone(request.getReceiverPhone());
        return smsMessageMapper.markRead(phone, request.getLastId());
    }

    @Transactional
    public void delete(Long id, String receiverPhone) {
        String phone = requirePhone(receiverPhone);
        if (smsMessageMapper.softDelete(id, phone) == 0) {
            throw new BusinessException(ErrorCode.MESSAGE_NOT_FOUND);
        }
    }

    public void checkDatabase() {
        try {
            smsMessageMapper.ping();
        } catch (DataAccessException exception) {
            throw new BusinessException(ErrorCode.DB_UNAVAILABLE);
        }
    }

    private String normalizeApiCode(String apiCode) {
        if (apiCode == null) {
            return null;
        }
        String normalized = apiCode.trim();
        return normalized.isEmpty() ? null : normalized;
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
                Boolean.TRUE.equals(message.getReceiverRead()),
                toOffset(message.getReadAt()),
                toOffset(message.getCreatedAt()));
    }

    private OffsetDateTime toOffset(LocalDateTime time) {
        return time == null ? null : time.atZone(ZONE).toOffsetDateTime();
    }
}
