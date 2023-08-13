package ru.practicum.shareit.item;

import ru.practicum.shareit.error_handler.ShareitNotFoundException;

public class ItemNotFoundException extends ShareitNotFoundException {

    public ItemNotFoundException(int id) {
        super(String.format("Вещь [%d] не найдена", id));
    }
}
