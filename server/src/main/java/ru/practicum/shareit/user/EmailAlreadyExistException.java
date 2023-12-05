package ru.practicum.shareit.user;

import org.springframework.http.HttpStatus;
import ru.practicum.shareit.error_handler.ShareitException;

public class EmailAlreadyExistException extends ShareitException {

    public EmailAlreadyExistException(String email) {
        super(HttpStatus.CONFLICT, "Email: " + email + " занят");
    }
}
