package com.shiraku.bankcard.controller;

import com.shiraku.bankcard.model.dto.UserRequest;
import com.shiraku.bankcard.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** Класс UserController представляет собой контроллер для управления пользователями
 * <p>
 * Включает следующие методы:
 * <p>
 * register(), login()
 * */
@Validated
@RestController
@RequestMapping("/api/user")
@Tag(name = "Управление пользователями", description = "Класс контроллера для управления пользователями")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /** Метод {@code register()} предназначен для создания нового пользователя
     * <p>
     * Принимает на входе:
     * @param user объект класса {@code UserRequest}
     * <p>
     * @see UserRequest
     * */
    @Operation(summary = "Регистрация нового пользователя",
            description = "Регистрирует пользователя на основе входных данных")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Пользователь успешно создан"),
            @ApiResponse(responseCode = "400", description = "Некорректные входные данные"),
            @ApiResponse(responseCode = "409", description = "Пользователь уже существует")
    })
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid UserRequest user) {
        if(userService.findByEmail(user.getEmail()) != null) {
            return new ResponseEntity<>("Email Already Exist", HttpStatus.CONFLICT);
        }
        return new ResponseEntity<>("Success registering: " + userService.save(user), HttpStatus.CREATED);
    }

    /** Метод {@code login()} предназначен для авторизации пользователя
     * <p>
     * Принимает на входе:
     * @param userRequest объект класса {@code UserRequest}
     * <p>
     * @see UserRequest
     * */
    @Operation(summary = "Вход пользователя",
            description = "Выполняет вход пользователя на основе входных данных")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь успешно вошел"),
            @ApiResponse(responseCode = "401", description = "Некорректные входные данные"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid UserRequest userRequest) {
        String token = userService.login(userRequest);
        return ResponseEntity.ok(Map.of("token", token));
    }

}
