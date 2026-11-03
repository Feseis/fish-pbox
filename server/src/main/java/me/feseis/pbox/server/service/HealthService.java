package me.feseis.pbox.server.service;

import lombok.RequiredArgsConstructor;
import me.feseis.pbox.server.exp.BusinessException;
import me.feseis.pbox.server.exp.ErrorCode;
import me.feseis.pbox.server.mapper.SmsMessageMapper;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HealthService {

    private final SmsMessageMapper smsMessageMapper;

    public void checkDatabase() {
        try {
            smsMessageMapper.ping();
        } catch (DataAccessException exception) {
            throw new BusinessException(ErrorCode.DB_UNAVAILABLE);
        }
    }
}
