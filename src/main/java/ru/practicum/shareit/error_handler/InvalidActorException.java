package ru.practicum.shareit.error_handler;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class InvalidActorException extends RuntimeException{
    private final String message;

    @Override
    public String getMessage() {
        return message;
    }
}
