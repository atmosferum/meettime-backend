package ru.whattime.whattime.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.whattime.whattime.auth.AuthTokenProvider;
import ru.whattime.whattime.dto.UserDto;
import ru.whattime.whattime.service.UserService;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@RestController
@RequestMapping(path = "/api/v1/login")
@RequiredArgsConstructor
public class AuthController {

    private final UserService service;
    private final AuthTokenProvider tokenProvider;

    @Value("${application.auth.cookie.name}")
    private String cookieName;

    @Value("${application.auth.cookie.maxAgeInDays}")
    private Integer maxAgeInDays;

    @PostMapping(consumes = "application/json")
    public ResponseEntity<?> login(@Valid @RequestBody UserDto userDTO, HttpServletResponse response) {
        UserDto loggedIn = service.login(userDTO);
        String authToken = tokenProvider.provideToken(loggedIn)
                .orElseThrow(IllegalStateException::new);

        Cookie cookie = new Cookie(cookieName, authToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(daysToSeconds(maxAgeInDays));

        response.addCookie(cookie);

        return ResponseEntity.noContent().build();
    }

    private int daysToSeconds(int days) {
        return days * 24 * 60 * 60;
    }
}
