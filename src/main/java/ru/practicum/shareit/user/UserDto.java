package ru.practicum.shareit.user;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

@ToString
@Getter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Jacksonized
public class UserDto {
    private int id;
    private String name;
    @NotNull
    @Email(message = "Введенный email не соответсвует формату")
    private String email;
}
