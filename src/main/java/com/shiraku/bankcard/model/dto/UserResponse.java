package com.shiraku.bankcard.model.dto;

import com.shiraku.bankcard.model.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Tag(name = "Ответ о пользователе", description = "Ответ информация о пользователе")
public class UserResponse {
    @Schema(description = "Уникальный идентификатор пользователя")
    private UUID id;
    @Schema(description = "Адрес электронной почты пользователя")
    private String email;
    @Schema(description = "карты пользователя")
    private List<UUID> cards;
    @Schema(description = "роль пользователя")
    private Role role;
}

