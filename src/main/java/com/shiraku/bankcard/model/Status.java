package com.shiraku.bankcard.model;

import io.swagger.v3.oas.annotations.tags.Tag;

/** Перечисление Status имеет значения статуса для банковской карты
 * Значения:
 * <p>
 * ACTIVE - Активная карта
 * <p>
 * INACTIVE - Неактивная карта
 * <p>
 * BLOCKED - Заблокированная карта
 * */
@Tag(name = "Статус", description = "Статус карты")
public enum Status {
    ACTIVE, INACTIVE, BLOCKED
}
