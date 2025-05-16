package com.shiraku.bankcard.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Tag(name = "Запрос на перевод", description = "Запрос пользователя на перевод денежных средств с карты на карту")
public class TransferRequest {
    @Schema(description = "Уникальный идентификатор карты откуда перевести")
    @NotNull
    private UUID fromCardId;
    @Schema(description = "Уникальный идентификатор карты куда перевести")
    @NotNull
    private UUID toCardId;
    @Schema(description = "сумма перевода")
    @NotNull
    private BigDecimal amount;
}

