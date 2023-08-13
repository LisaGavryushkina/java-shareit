package ru.practicum.shareit.user;

import ru.practicum.shareit.error_handler.ShareitNotFoundException;

public class UserNotFoundException extends ShareitNotFoundException {

    public UserNotFoundException(int id) {
        super(String.format("Пользователь [%d] не найден", id));
    }
}
