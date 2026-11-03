package me.feseis.pbox.server.controller;

import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;
import me.feseis.pbox.server.dto.RegisterPhoneRequest;
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
public class RegisteredPhoneController {

    private final RegisteredPhoneService registeredPhoneService;

    public RegisteredPhoneController(RegisteredPhoneService registeredPhoneService) {
        this.registeredPhoneService = registeredPhoneService;
    }

    @GetMapping
    public List<String> list() {
        return registeredPhoneService.listAll();
    }

    @PostMapping
    public Map<String, String> register(@Valid @RequestBody RegisterPhoneRequest request) {
        String phone = registeredPhoneService.register(request.getPhone());
        return Map.of("phone", phone);
    }

    @DeleteMapping
    public Map<String, Boolean> delete(@RequestParam(value = "phone", required = false) String phone) {
        registeredPhoneService.delete(phone);
        return Map.of("deleted", true);
    }
}
