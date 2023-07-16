package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import ru.practicum.shareit.error_handler.NotFoundException;

@RequiredArgsConstructor
public class BookingNotFoundException extends NotFoundException {

    private final int id;

    @Override
    public String getMessage() {
        return String.format("Бронирование [%d] не найдено", id);
    }
}

