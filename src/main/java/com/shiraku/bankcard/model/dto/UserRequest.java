package com.shiraku.bankcard.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Tag(name = "Запрос пользователя", description = "Запрос пользователя на регистрация || вход")
public class UserRequest {
    @Schema(description = "Адрес электронной почты пользователя")
    @NotBlank
    @Email
    private String email;
    @Schema(description = "Пароль пользователя")
    @NotBlank
    @Size(min = 8, max = 24)
    private String password;
}
