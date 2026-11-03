package me.feseis.pbox.server.service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;

import me.feseis.pbox.server.dto.CreateSpecialSenderRequest;
import me.feseis.pbox.server.dto.SpecialSenderResponse;
import me.feseis.pbox.server.entity.SpecialSender;
import me.feseis.pbox.server.exp.BusinessException;
import me.feseis.pbox.server.exp.ErrorCode;
import me.feseis.pbox.server.mapper.SpecialSenderMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SpecialSenderService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String API_CODE_PREFIX = "ps_";
    private static final int API_CODE_BYTES = 24;

    private final SpecialSenderMapper specialSenderMapper;

    public SpecialSenderService(SpecialSenderMapper specialSenderMapper) {
        this.specialSenderMapper = specialSenderMapper;
    }

    @Transactional
    public SpecialSenderResponse register(CreateSpecialSenderRequest request) {
        String phone = request.getPhone().trim();
        String senderName = request.getSenderName().trim();

        if (findApiCodeByPhone(phone) != null) {
            return toResponse(specialSenderMapper.findByPhone(phone));
        }

        SpecialSender sender = new SpecialSender(phone, senderName, generateApiCode());
        try {
            specialSenderMapper.insert(sender);
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(ErrorCode.SENDER_ALREADY_EXISTS);
        }

        return toResponse(sender);
    }

    public List<SpecialSenderResponse> listAll() {
        return specialSenderMapper.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public SpecialSenderResponse updateSenderName(String phone, String senderName) {
        String normalizedPhone = phone.trim();
        String normalizedName = senderName.trim();

        SpecialSender existing = specialSenderMapper.findByPhone(normalizedPhone);
        if (existing == null) {
            throw new BusinessException(ErrorCode.SENDER_NOT_FOUND);
        }

        specialSenderMapper.updateSenderName(normalizedPhone, normalizedName);
        existing.setSenderName(normalizedName);
        return toResponse(existing);
    }

    public String findApiCodeByPhone(String phone) {
        return specialSenderMapper.findApiCodeByPhone(phone);
    }

    @Transactional
    public void delete(String phone) {
        if (phone == null || phone.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT);
        }

        String normalizedPhone = phone.trim();
        if (specialSenderMapper.deleteByPhone(normalizedPhone) == 0) {
            throw new BusinessException(ErrorCode.SENDER_NOT_FOUND);
        }
    }

    private SpecialSenderResponse toResponse(SpecialSender sender) {
        return new SpecialSenderResponse(sender.getPhone(), sender.getSenderName(), sender.getApiCode());
    }

    private String generateApiCode() {
        byte[] bytes = new byte[API_CODE_BYTES];
        RANDOM.nextBytes(bytes);
        return API_CODE_PREFIX + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
