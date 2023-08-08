package ru.practicum.shareit.request;

import ru.practicum.shareit.error_handler.ShareitNotFoundException;

public class ItemRequestNotFoundException extends ShareitNotFoundException {

    public ItemRequestNotFoundException(int id) {
        super(String.format("Запрос [%d] не найден", id));
    }
}
