package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class EmailAlreadyExistException extends RuntimeException {

    private final String email;

    public String getMessage() {
        return "Email: " + email + " занят";
    }
}
