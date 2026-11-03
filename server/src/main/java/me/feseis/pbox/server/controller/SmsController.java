package me.feseis.pbox.server.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.feseis.pbox.server.dto.common.DeleteResponse;
import me.feseis.pbox.server.dto.health.HealthResponse;
import me.feseis.pbox.server.dto.sms.MarkReadRequest;
import me.feseis.pbox.server.dto.sms.MarkReadResponse;
import me.feseis.pbox.server.dto.sms.MessageListResponse;
import me.feseis.pbox.server.dto.sms.SendSmsByApiRequest;
import me.feseis.pbox.server.dto.sms.SendSmsByUserRequest;
import me.feseis.pbox.server.dto.sms.SmsMessageResponse;
import me.feseis.pbox.server.service.HealthService;
import me.feseis.pbox.server.service.SmsMessageService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sms")
@RequiredArgsConstructor
public class SmsController {

    private final SmsMessageService smsMessageService;
    private final HealthService healthService;

    @PostMapping("/send")
    public SmsMessageResponse sendByUser(@Valid @RequestBody SendSmsByUserRequest request) {
        return smsMessageService.sendByUser(request);
    }

    @PostMapping("/send-api")
    public SmsMessageResponse sendByApi(@Valid @RequestBody SendSmsByApiRequest request) {
        return smsMessageService.sendByApi(request);
    }

    @GetMapping("/messages")
    public MessageListResponse list(
            @RequestParam(value = "receiverPhone", required = false) String receiverPhone,
            @RequestParam(value = "afterId", defaultValue = "0") long afterId,
            @RequestParam(value = "limit", defaultValue = "20") int limit) {
        return smsMessageService.list(receiverPhone, afterId, limit);
    }

    @PostMapping("/messages/read")
    public MarkReadResponse markRead(@Valid @RequestBody MarkReadRequest request) {
        return new MarkReadResponse(smsMessageService.markRead(request));
    }

    @DeleteMapping("/messages/{id}")
    public DeleteResponse delete(
            @PathVariable("id") long id,
            @RequestParam(value = "receiverPhone", required = false) String receiverPhone) {
        smsMessageService.delete(id, receiverPhone);
        return new DeleteResponse(true);
    }

    @GetMapping("/health")
    public HealthResponse health() {
        healthService.checkDatabase();
        return new HealthResponse(true);
    }
}
