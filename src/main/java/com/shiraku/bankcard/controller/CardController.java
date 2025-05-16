package com.shiraku.bankcard.controller;

import com.shiraku.bankcard.model.Status;
import com.shiraku.bankcard.model.dto.CardCreateRequest;
import com.shiraku.bankcard.model.dto.TransferRequest;
import com.shiraku.bankcard.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

/** Класс CardController представляет собой контроллер для управления банковскими картами
 * <p>
 * Включает следующие методы:
 * <p>
 * getUserCards(), getBalance(),  transferBetweenCards(), blockUserCard(), createCard(), blockCard(), activateCard(),
 * deleteCard(), getAllCards()
 * */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/card")
@Tag(name = "Управление картами", description = "Класс контроллера для управления картами пользователя")
public class CardController {
    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    // --- Общее: User и Admin ---

    /** Метод {@code getUserCards()} предназначен для получения карт пользователя
     * <p>
     * Принимает на входе:
     * @param email объект типа String - представляет собой email пользователя
     * <p>
     * @param status объект перечисления {@code Status} - представляет собой статус карты (Активная, Блокированная)
     * <p>
     * @param page объект типа Integer - представляет собой номер страницы (счёт с 0)
     * <p>
     * @param size объект типа Integer - представляет собой количество записей на одной странице
     * <p>
     * @see Status
     * */
    @Operation(summary = "Вывод карт",
            description = "Вывод карт текущего пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Карты успешно выведены"),
            @ApiResponse(responseCode = "400", description = "Некорректные входные данные"),
    })
    @GetMapping("/my_cards")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getUserCards(@AuthenticationPrincipal(expression = "username") String email,
                                          @RequestParam Status status,
                                          @RequestParam int page,
                                          @RequestParam int size) {
        return ResponseEntity.ok(cardService.getUserCards(email, status, page, size).getContent());
    }

    /** Метод {@code getBalance()} предназначен для вывода баланса карты
     * <p>
     * Принимает на входе:
     * @param cardId объект типа UUID - представляет собой id карты
     * <p>
     * @param email объект типа String - представляет собой email пользователя
     * */
    @Operation(summary = "Вывод баланса",
            description = "Вывод баланса карты по id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Баланс успешно выведен"),
            @ApiResponse(responseCode = "400", description = "Некорректные входные данные"),
    })
    @GetMapping("/balance/{cardId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getBalance(@PathVariable UUID cardId,
                                        @AuthenticationPrincipal(expression = "username") String email) {
        BigDecimal response = cardService.getCardBalance(cardId,email);
        log.info("response balance: {}", response.toString());
        return ResponseEntity.ok(response);
    }

    /** Метод {@code transferBetweenCards()} предназначен для перевода денежных средств с карты на карту пользователя
     * <p>
     * Принимает на входе:
     * @param request объект класса {@code TransferRequest}
     * <p>
     * @param email объект типа String - представляет собой email пользователя
     * @see TransferRequest
     * */
    @Operation(summary = "Перевод",
            description = "Перевод денежных средств с карты на карту пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Перевод успешно выполнен"),
            @ApiResponse(responseCode = "400", description = "Некорректные входные данные"),
    })
    @PostMapping("/transfer")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> transferBetweenCards(@RequestBody @Valid TransferRequest request,
                                                  @AuthenticationPrincipal String email) {
        cardService.transfer(request, email);
        return ResponseEntity.ok("Перевод выполнен");
    }

    /** Метод {@code blockUserCard()} предназначен для запроса на блокировку карты
     * <p>
     * Принимает на входе:
     * @param cardId объект типа UUID - представляет собой id карты
     * <p>
     * @param email объект типа String - представляет собой email пользователя
     * */
    @Operation(summary = "Блокировка карты",
            description = "Блокировка банковской карты пользователя по id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Блокировка карты успешно выполнена"),
            @ApiResponse(responseCode = "400", description = "Некорректные входные данные"),
    })
    @PostMapping("/block/{cardId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> blockUserCard(@PathVariable UUID cardId,
                                           @AuthenticationPrincipal String email) {
        cardService.blockCardByUser(cardId, email);
        return ResponseEntity.ok("Карта заблокирована");
    }

    // --- Только Admin ---

    /** Метод {@code createCard()} предназначен для создания карт пользователю администратором
     * <p>
     * Принимает на входе:
     * @param request объект класса {@code CardCreateRequest}
     * @see CardCreateRequest
     * */
    @Operation(summary = "Создание карт",
            description = "Создание карт для пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Карты успешно создана"),
            @ApiResponse(responseCode = "400", description = "Некорректные входные данные"),
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createCard(@RequestBody @Valid CardCreateRequest request) {
        return ResponseEntity.ok(cardService.createCard(request));
    }

    /** Метод {@code blockCard()} предназначен для блокировки карты администратором
     * <p>
     * Принимает на входе:
     * @param cardId объект типа UUID - представляет собой id карты
     * <p>
     * */
    @Operation(summary = "Блокировка карты",
            description = "Блокировка банковской карты пользователя по id администратором")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Блокировка карты успешно выполнена"),
            @ApiResponse(responseCode = "400", description = "Некорректные входные данные"),
    })
    @PostMapping("/block-admin/{cardId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> blockCard(@PathVariable UUID cardId) {
        cardService.blockCardByAdmin(cardId);
        return ResponseEntity.ok("Карта заблокирована админом");
    }

    /** Метод {@code activateCard()} предназначен для активации карты администратором
     * <p>
     * Принимает на входе:
     * @param cardId объект типа UUID - представляет собой id карты
     * <p>
     * */
    @Operation(summary = "Активации карты",
            description = "Активация банковской карты пользователя по id администратором")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Активация карты успешно выполнена"),
            @ApiResponse(responseCode = "400", description = "Некорректные входные данные"),
    })
    @PostMapping("/activate/{cardId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> activateCard(@PathVariable UUID cardId) {
        cardService.activateCard(cardId);
        return ResponseEntity.ok("Карта активирована");
    }

    /** Метод {@code deleteCard()} предназначен для удаления карты администратором
     * <p>
     * Принимает на входе:
     * @param cardId объект типа UUID - представляет собой id карты
     * <p>
     * */
    @Operation(summary = "Удаление карты",
            description = "Удаление банковской карты пользователя по id администратором")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Удаление карты успешно выполнена"),
            @ApiResponse(responseCode = "400", description = "Некорректные входные данные"),
    })
    @DeleteMapping("/{cardId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteCard(@PathVariable UUID cardId) {
        cardService.deleteCard(cardId);
        return ResponseEntity.ok("Карта удалена");
    }

    /** Метод {@code getAllCards()} предназначен для вывода карт пользователей
     * <p>
     * Принимает на входе:
     * @param page объект типа Integer - представляет собой номер страницы (счёт с 0)
     * <p>
     * @param size объект типа Integer - представляет собой количество записей на одной странице
     * */
    @Operation(summary = "Вывод карт",
            description = "Вывод карт пользователей")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Карты успешно выведены"),
            @ApiResponse(responseCode = "400", description = "Некорректные входные данные"),
    })
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllCards(@RequestParam int page,
                                         @RequestParam int size) {
        return ResponseEntity.ok(cardService.getAllCards(page, size).getContent());
    }


}

