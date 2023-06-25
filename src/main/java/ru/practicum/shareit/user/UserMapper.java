package ru.practicum.shareit.user;

public class UserMapper {

    public static UserDto toUserDto(User user) {
        return new UserDto(user.getId(),
                user.getName(),
                user.getEmail());
    }

    public static User toUser(int userId, UserDto userDto) {
        return new User(userId,
                userDto.getName(),
                userDto.getEmail());
    }

    public static User toUserWithUpdate(UserDto userDto, User user) {
        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        }
        if (userDto.getEmail() != null) {
            user.setEmail(userDto.getEmail());
        }
        return user;
    }
}
