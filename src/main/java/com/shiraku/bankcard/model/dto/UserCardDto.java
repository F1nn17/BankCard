package com.shiraku.bankcard.model.dto;

import com.shiraku.bankcard.model.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Tag(name = "Ответ пользователю", description = "Ответ пользователю информации о карте")
public class UserCardDto {
    @Schema(description = "Уникальный идентификатор пользователя")
    private UUID id;
    @Schema(description = "Уникальный номер карты")
    private String number;
    @Schema(description = "Срок действия карты")
    private String expiryDate;
    @Schema(description = "Статус карты")
    private Status status;
    @Schema(description = "Баланс карты")
    private BigDecimal balance;
}

