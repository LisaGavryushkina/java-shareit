package ru.practicum.shareit.user;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final Map<Integer, User> users;
    private int id = 0;

    private int createId() {
        return ++id;
    }

    private boolean isEmailAlreadyExist(UserDto userDto, int userId) {
        for (User user : users.values()) {
            if (user.getEmail().equals(userDto.getEmail()) && user.getId() != userId) {
                return true;
            }
        }
        return false;
    }

    @Override
    public UserDto addUser(UserDto userDto) {
        if (isEmailAlreadyExist(userDto, -1)) {
            throw new EmailAlreadyExistException(userDto.getEmail());
        }
        User user = UserMapper.toUser(createId(), userDto);
        users.put(user.getId(), user);
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto updateUser(int userId, UserDto userDto) {
        if (users.get(userId) == null) {
            throw new UserNotFoundException(userId);
        }
        if (userDto.getEmail() != null && isEmailAlreadyExist(userDto, userId)) {
            throw new EmailAlreadyExistException(userDto.getEmail());
        }
        User user = UserMapper.toUserWithUpdate(userDto, users.get(userId));
        users.put(user.getId(), user);
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto getUserById(int userId) {
        if (users.get(userId) == null) {
            throw new UserNotFoundException(userId);
        }
        return UserMapper.toUserDto(users.get(userId));
    }

    @Override
    public void deleteUserById(int userId) {
        users.remove(userId);
    }

    @Override
    public List<UserDto> getAllUsers() {
        return users.values().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }
}
