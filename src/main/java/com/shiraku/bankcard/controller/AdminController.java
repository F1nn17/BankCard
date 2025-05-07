package com.shiraku.bankcard.controller;

import com.shiraku.bankcard.model.dto.RaisingRequest;
import com.shiraku.bankcard.model.dto.UserResponse;
import com.shiraku.bankcard.model.entity.User;
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

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/** Класс AdminController представляет собой панель администрирования над пользователями и банковскими картами
 * <p>
 * Включает следующие методы:
 * <p>
 * raising(), getAllUsers(), deleteUser()
 * */
@Validated
@RestController
@RequestMapping("/api/admin")
@Tag(name = "Панель Администрирования", description = "Класс контроллера панели администрирования")
public class AdminController {
    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    /** Метод {@code raising()} предназначен для повышения пользователя до администратора
     * <p>
     * Принимает на входе:
     * @param request объект класса {@code RaisingRequest}
     * <p>
     * @see RaisingRequest
     * */
    @Operation(summary = "Повышение пользователя",
            description = "Повышение пользователя до администратора на основе входных данных")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь успешно повышен"),
            @ApiResponse(responseCode = "400", description = "Некорректные входные данные"),
    })
    @PostMapping("/raising")
    public ResponseEntity<?> raising(@RequestBody @Valid RaisingRequest request) {
        if (request.getEmail() == null || request.getPassword() == null || request.getSecretKey() == null) {
            return new ResponseEntity<>("Incorrect input data", HttpStatus.BAD_REQUEST);
        }
        String secretKey = "_0_1_2_3_4_5_6_7_";
        if(!request.getSecretKey().equals(secretKey)) {
            return new ResponseEntity<>("Incorrect", HttpStatus.BAD_REQUEST);
        }
        User raisinged = userService.raising(request);
        UserResponse response = new UserResponse();
        response.setId(raisinged.getId());
        response.setEmail(raisinged.getEmail());
        response.setRole(raisinged.getRole());
        return new ResponseEntity<>("Success raising: " + response.getEmail() + " : " + response.getRole(), HttpStatus.OK);
    }

    /** Метод {@code getAllUsers()} предназначен для вывода всех пользователей в системе
     * */
    @Operation(summary = "Вывод пользователей",
            description = "вывод всех пользователей в системе")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список успешно выведен"),
    })
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        List<User> users = userService.findAll();

        List<UserResponse> response = users.stream()
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getEmail(),
                        user.getCards(),
                        user.getRole()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /** Метод {@code raising()} предназначен для удаления пользователя из системы
     * <p>
     * Принимает на входе:
     * @param userId объект типа UUID - представляет собой id пользователя
     * */
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь успешно удалён"),
            @ApiResponse(responseCode = "400", description = "Некорректные входные данные"),
    })
    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable UUID userId) {
        return ResponseEntity.ok("Success delete user: " + userService.deleteUser(userId));
    }
}
