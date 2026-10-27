package kz.delivery.domain;

public enum DeliveryPriority {
    URGENT,    // срочно: приоритет времени
    STANDARD,  // обычная доставка: баланс критериев
    ECONOMY,   // экономия: приоритет стоимости
    ECO        // экологичная доставка: приоритет выбросов
}