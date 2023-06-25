package ru.practicum.shareit.error_handler;

public class NotFoundException extends RuntimeException {

    @Override
    public String getMessage() {
        return "По вашему запросу ничего не найдено";
    }
}
