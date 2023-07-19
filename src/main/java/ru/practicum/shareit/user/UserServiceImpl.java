package ru.practicum.shareit.user;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper mapper;

    @Override
    public UserDto addUser(UserDto userDto) {
        User user = userRepository.save(mapper.toUser(0, userDto));
        UserDto added = mapper.toUserDto(user);
        log.info("Добавлен пользователь: {}", added);
        return added;
    }

    @Override
    public UserDto updateUser(int userId, UserDto userDto) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new UserNotFoundException(userId);
        }
        UserDto updated = mapper.toUserDto(userRepository.save(mapper.toUserWithUpdate(userDto, user.get())));
        log.info("Данные о пользователе [{}] обновлены: {}", userDto, updated);
        return updated;
    }

    @Override
    public UserDto getUserById(int userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new UserNotFoundException(userId);
        }
        UserDto userDto = mapper.toUserDto(user.get());
        log.info("Вернули пользователя: {}", userDto);
        return userDto;
    }

    @Override
    public void deleteUserById(int userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new UserNotFoundException(userId);
        }
        userRepository.deleteById(userId);
        log.info("Удалили пользователя: {}", user);
    }

    @Override
    public List<UserDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserDto> usersDto = mapper.toUserDto(users);
        log.info("Вернули всех пользователей: {}", usersDto);
        return usersDto;
    }
}
