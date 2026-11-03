package me.feseis.pbox.server.service;

import java.util.List;
import java.util.regex.Pattern;

import me.feseis.pbox.server.exp.BusinessException;
import me.feseis.pbox.server.exp.ErrorCode;
import me.feseis.pbox.server.mapper.RegisteredPhoneMapper;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisteredPhoneService {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    private final RegisteredPhoneMapper registeredPhoneMapper;

    public RegisteredPhoneService(RegisteredPhoneMapper registeredPhoneMapper) {
        this.registeredPhoneMapper = registeredPhoneMapper;
    }

    @Transactional
    public String register(String phone) {
        String normalizedPhone = validate(phone);

        if (registeredPhoneMapper.findByPhone(normalizedPhone) != null) {
            return normalizedPhone;
        }

        try {
            registeredPhoneMapper.insert(normalizedPhone);
        } catch (DuplicateKeyException exception) {
            // 并发重复注册：唯一键兜底，按幂等成功处理
        }
        return normalizedPhone;
    }

    public List<String> listAll() {
        return registeredPhoneMapper.findAll();
    }

    @Transactional
    public void delete(String phone) {
        String normalizedPhone = validate(phone);

        if (registeredPhoneMapper.deleteByPhone(normalizedPhone) == 0) {
            throw new BusinessException(ErrorCode.PHONE_NOT_FOUND);
        }
    }

    private String validate(String phone) {
        if (phone == null || phone.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT);
        }

        String normalizedPhone = phone.trim();
        if (!PHONE_PATTERN.matcher(normalizedPhone).matches()) {
            throw new BusinessException(ErrorCode.INVALID_ARGUMENT);
        }
        return normalizedPhone;
    }
}
