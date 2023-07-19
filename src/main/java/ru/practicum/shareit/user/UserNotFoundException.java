package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import ru.practicum.shareit.error_handler.NotFoundException;

@RequiredArgsConstructor
public class UserNotFoundException extends NotFoundException {

    private final int id;

    @Override
    public String getMessage() {
        return String.format("Пользователь [%d] не найден", id);
    }
}
