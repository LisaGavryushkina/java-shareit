package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl {

//    private final Map<Integer, User> users;
//    private int id = 0;
//    private final UserMapper mapper;
//
//    private int createId() {
//        return ++id;
//    }
//
//    private boolean isEmailAlreadyExist(UserDto userDto, int userId) {
//        for (User user : users.values()) {
//            if (user.getEmail().equals(userDto.getEmail()) && user.getId() != userId) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    public UserDto addUser(UserDto userDto) {
//        if (isEmailAlreadyExist(userDto, -1)) {
//            throw new EmailAlreadyExistException(userDto.getEmail());
//        }
//        User user = mapper.toUser(createId(), userDto);
//        users.put(user.getId(), user);
//        return mapper.toUserDto(user);
//    }
//
//    public UserDto updateUser(int userId, UserDto userDto) {
//        if (users.get(userId) == null) {
//            throw new UserNotFoundException(userId);
//        }
//        if (userDto.getEmail() != null && isEmailAlreadyExist(userDto, userId)) {
//            throw new EmailAlreadyExistException(userDto.getEmail());
//        }
//        User user = mapper.toUserWithUpdate(userDto, users.get(userId));
//        users.put(user.getId(), user);
//        return mapper.toUserDto(user);
//    }
//
//    public UserDto getUserById(int userId) {
//        if (users.get(userId) == null) {
//            throw new UserNotFoundException(userId);
//        }
//        return mapper.toUserDto(users.get(userId));
//    }
//
//    public void deleteUserById(int userId) {
//        users.remove(userId);
//    }
//
//    public List<UserDto> getAllUsers() {
//        return users.values().stream()
//                .map(mapper::toUserDto)
//                .collect(Collectors.toList());
//    }
}
