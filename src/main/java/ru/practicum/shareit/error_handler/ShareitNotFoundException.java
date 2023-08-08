package ru.practicum.shareit.error_handler;

import org.springframework.http.HttpStatus;

public class ShareitNotFoundException extends ShareitException {

    public ShareitNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
