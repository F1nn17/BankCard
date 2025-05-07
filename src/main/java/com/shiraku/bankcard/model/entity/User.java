package com.shiraku.bankcard.model.entity;

import com.shiraku.bankcard.model.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.List;
import java.util.UUID;

/**
 * Класс User
 * - представляет собой сущность для хранения данных пользователя
 * <p>
 * Включает следующие поля:
 * <p>
 * {@code id} - Уникальный идентификатор пользователя
 * <p>
 * {@code email} - Электронная почта пользователя
 * <p>
 * {@code password} - Пароль пользователя
 * <p>
 * {@code cards} - Количество карт пользователя
 * <p>
 * {@code role} - Роль пользователя
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
@Tag(name = "Пользователь", description = "Класс сущности пользователя")
public class User {
    @Id
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID")
    @Schema(description = "Уникальный идентификатор пользователя")
    private UUID id;

    @Column(name = "email", updatable = false, nullable = false)
    @Schema(description = "Адрес электронной почты пользователя")
    private String email;

    @Column(name = "password", nullable = false)
    @Schema(description = "Пароль пользователя")
    private String password;

    @Column(name = "cards")
    @Schema(description = "карты пользователя")
    private List<UUID> cards;

    @Column(name = "role")
    @Schema(description = "роль пользователя")
    private Role role;

    public User(UUID id, String email, Role role) {
        this.id = id;
        this.email = email;
        this.role = role;
    }

}
