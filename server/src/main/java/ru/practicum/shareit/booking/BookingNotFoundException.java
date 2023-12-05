package ru.practicum.shareit.booking;

import ru.practicum.shareit.error_handler.ShareitNotFoundException;

public class BookingNotFoundException extends ShareitNotFoundException {

    public BookingNotFoundException(int id) {
        super(String.format("Бронирование [%d] не найдено", id));
    }
}

