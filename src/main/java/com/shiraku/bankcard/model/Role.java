package com.shiraku.bankcard.model;

import io.swagger.v3.oas.annotations.tags.Tag;

/** Перечисление Role имеет значения для пользователя
 * Значения:
 * <p>
 * USER - пользователь с ограниченными правами
 * <p>
 * ADMIN - пользователь с правами администратора
 * */
@Tag(name = "Роли", description = "Роли для пользователя")
public enum Role {
    ADMIN, USER
}