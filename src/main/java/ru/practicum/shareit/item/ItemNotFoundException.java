package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import ru.practicum.shareit.error_handler.NotFoundException;

@RequiredArgsConstructor
public class ItemNotFoundException extends NotFoundException {

    private final int id;

    @Override
    public String getMessage() {
        return String.format("Вещь [%d] не найдена", id);
    }
}
