package me.feseis.pbox.server.controller;

import java.util.List;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.feseis.pbox.server.dto.common.DeleteResponse;
import me.feseis.pbox.server.dto.phone.RegisterPhoneRequest;
import me.feseis.pbox.server.dto.phone.RegisteredPhoneResponse;
import me.feseis.pbox.server.service.RegisteredPhoneService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sms/phones")
@RequiredArgsConstructor
public class RegisteredPhoneController {

    private final RegisteredPhoneService registeredPhoneService;

    @GetMapping
    public List<String> list() {
        return registeredPhoneService.listAll();
    }

    @PostMapping
    public RegisteredPhoneResponse register(@Valid @RequestBody RegisterPhoneRequest request) {
        return new RegisteredPhoneResponse(registeredPhoneService.register(request.phone()));
    }

    @DeleteMapping
    public DeleteResponse delete(@RequestParam(value = "phone", required = false) String phone) {
        registeredPhoneService.delete(phone);
        return new DeleteResponse(true);
    }
}
