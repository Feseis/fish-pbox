package me.feseis.pbox.server.controller;

import java.util.Map;

import jakarta.validation.Valid;
import me.feseis.pbox.server.dto.MarkReadRequest;
import me.feseis.pbox.server.dto.MessageListResponse;
import me.feseis.pbox.server.dto.SendSmsRequest;
import me.feseis.pbox.server.dto.SmsMessageResponse;
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
public class SmsController {

    private final SmsMessageService smsMessageService;

    public SmsController(SmsMessageService smsMessageService) {
        this.smsMessageService = smsMessageService;
    }

    @PostMapping("/send")
    public SmsMessageResponse send(@Valid @RequestBody SendSmsRequest request) {
        return smsMessageService.send(request);
    }

    @GetMapping("/messages")
    public MessageListResponse list(
            @RequestParam(value = "receiverPhone", required = false) String receiverPhone,
            @RequestParam(value = "afterId", defaultValue = "0") long afterId,
            @RequestParam(value = "limit", defaultValue = "20") int limit) {
        return smsMessageService.list(receiverPhone, afterId, limit);
    }

    @PostMapping("/messages/read")
    public Map<String, Integer> markRead(@Valid @RequestBody MarkReadRequest request) {
        return Map.of("updated", smsMessageService.markRead(request));
    }

    @DeleteMapping("/messages/{id}")
    public Map<String, Boolean> delete(
            @PathVariable("id") Long id,
            @RequestParam(value = "receiverPhone", required = false) String receiverPhone) {
        smsMessageService.delete(id, receiverPhone);
        return Map.of("deleted", true);
    }

    @GetMapping("/health")
    public Map<String, Boolean> health() {
        smsMessageService.checkDatabase();
        return Map.of("database", true);
    }
}
