package com.shiraku.bankcard.model.dto;

import com.shiraku.bankcard.model.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Tag(name = "Ответ администратору", description = "Ответ администратору информации о карте")
public class AdminCardDto {
    @Schema(description = "Уникальный идентификатор пользователя")
    private UUID id;
    @Schema(description = "Уникальный номер карты")
    private String number;
    @Schema(description = "Срок действия карты")
    private String expiryDate;
    @Schema(description = "Статус карты")
    private Status status;
    @Schema(description = "Владелец карты")
    private UUID ownerId;
}

