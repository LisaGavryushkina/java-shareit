package ru.practicum.shareit.user;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class UserDto {
    private final int id;
    private final String name;
    @NotNull
    @Email(message = "Введенный email не соответсвует формату")
    private final String email;
}
