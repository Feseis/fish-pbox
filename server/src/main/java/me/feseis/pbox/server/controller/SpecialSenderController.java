package me.feseis.pbox.server.controller;

import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;
import me.feseis.pbox.server.dto.CreateSpecialSenderRequest;
import me.feseis.pbox.server.dto.SpecialSenderResponse;
import me.feseis.pbox.server.dto.UpdateSpecialSenderRequest;
import me.feseis.pbox.server.service.SpecialSenderService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sms/senders")
public class SpecialSenderController {

    private final SpecialSenderService specialSenderService;

    public SpecialSenderController(SpecialSenderService specialSenderService) {
        this.specialSenderService = specialSenderService;
    }
    @GetMapping
    public List<SpecialSenderResponse> list() {
        return specialSenderService.listAll();
    }

    @PostMapping
    public SpecialSenderResponse register(@Valid @RequestBody CreateSpecialSenderRequest request) {
        return specialSenderService.register(request);
    }

    @PutMapping
    public SpecialSenderResponse updateSenderName(@Valid @RequestBody UpdateSpecialSenderRequest request) {
        return specialSenderService.updateSenderName(request.getPhone(), request.getSenderName());
    }

    @DeleteMapping
    public Map<String, Boolean> delete(@RequestParam(value = "phone", required = false) String phone) {
        specialSenderService.delete(phone);
        return Map.of("deleted", true);
    }
}
