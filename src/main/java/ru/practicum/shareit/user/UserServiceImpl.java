package ru.practicum.shareit.user;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserDto addUser(UserDto userDto) {
        UserDto added = userRepository.addUser(userDto);
        log.info("Добавлен пользователь: {}", added);
        return added;
    }

    @Override
    public UserDto updateUser(int userId, UserDto userDto) {
        UserDto updated = userRepository.updateUser(userId, userDto);
        log.info("Данные о пользователе [{}] обновлены: {}", userId, updated);
        return updated;
    }

    @Override
    public UserDto getUserById(int userId) {
        UserDto userDto = userRepository.getUserById(userId);
        log.info("Вернули пользователя: {}", userDto);
        return userDto;
    }

    @Override
    public void deleteUserById(int userId) {
        UserDto userDto = userRepository.getUserById(userId);
        log.info("Удалили пользователя: {}", userDto);
        userRepository.deleteUserById(userId);
    }

    @Override
    public List<UserDto> getAllUsers() {
        List<UserDto> users = userRepository.getAllUsers();
        log.info("Вернули всех пользователей: {}", users);
        return users;
    }
}
