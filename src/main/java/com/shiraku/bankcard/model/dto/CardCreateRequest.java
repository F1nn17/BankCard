package com.shiraku.bankcard.model.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Tag(name = "Запрос новой карты", description = "Запрос на создание новой карты для пользователя")
public class CardCreateRequest {
    @Schema(description = "owner - адрес электронной почты пользователя")
    @NotBlank
    @Email
    private String owner;
}
