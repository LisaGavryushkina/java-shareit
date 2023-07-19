package ru.practicum.shareit.user;

import java.util.List;

public interface UserService {

    UserDto addUser(UserDto userDto);

    UserDto updateUser(int userId, UserDto userDto);

    UserDto getUserById(int userId);

    void deleteUserById(int userId);

    List<UserDto> getAllUsers();
}
