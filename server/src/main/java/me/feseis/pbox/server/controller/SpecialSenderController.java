package me.feseis.pbox.server.controller;

import java.util.List;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.feseis.pbox.server.dto.common.DeleteResponse;
import me.feseis.pbox.server.dto.sender.CreateSpecialSenderRequest;
import me.feseis.pbox.server.dto.sender.SpecialSenderResponse;
import me.feseis.pbox.server.dto.sender.UpdateSpecialSenderRequest;
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
@RequiredArgsConstructor
public class SpecialSenderController {

    private final SpecialSenderService specialSenderService;

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
        return specialSenderService.updateSenderName(request.phone(), request.senderName());
    }

    @DeleteMapping
    public DeleteResponse delete(@RequestParam(value = "phone", required = false) String phone) {
        specialSenderService.delete(phone);
        return new DeleteResponse(true);
    }
}
