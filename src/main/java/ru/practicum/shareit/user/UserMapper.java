package ru.practicum.shareit.user;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDto toUserDto(User user) {
        return new UserDto(user.getId(),
                user.getName(),
                user.getEmail());
    }

    public User toUser(int userId, UserDto userDto) {
        return new User(userId,
                userDto.getName(),
                userDto.getEmail());
    }

    public User toUserWithUpdate(UserDto userDto, User user) {
        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        }
        if (userDto.getEmail() != null) {
            user.setEmail(userDto.getEmail());
        }
        return user;
    }

    public List<UserDto> toUserDto(List<User> users) {
       return users.stream()
                .map(this::toUserDto)
                .collect(Collectors.toList());
    }
}
