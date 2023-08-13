package ru.practicum.shareit;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.UserNotFoundException;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.UserServiceImpl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceUnitTest {

    public static final User USER_1 = new User(1, "user1", "user1@mail.ru");
    public static final User USER_1_WITH_UPDATE = new User(1, "Lisa", "user1@mail.ru");
    public static final User USER_2 = new User(2, "user2", "user2@mail.ru");
    public static final UserDto USER_1_DTO = new UserDto(1, "user1", "user1@mail.ru");
    public static final UserDto USER_1_WITH_UPDATE_DTO = new UserDto(1, "Lisa", "user1@mail.ru");
    public static final UserDto USER_2_DTO = new UserDto(2, "user2", "user2@mail.ru");
    private UserService userService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper mapper;

    @BeforeEach
    public void start() {
        userService = new UserServiceImpl(userRepository, mapper);
    }

    @Test
    void whenAddUser_thenUserAdded() {
        when(mapper.toUser(USER_1_DTO)).thenReturn(USER_1);
        when(userRepository.save(USER_1)).thenReturn(USER_1);
        when(mapper.toUserDto(USER_1)).thenReturn(USER_1_DTO);

        UserDto actual = userService.addUser(USER_1_DTO);

        assertThat(actual, equalTo(USER_1_DTO));
    }

    @Test
    void whenUpdateUser_thenUserUpdated() {
        when(userRepository.findById(1)).thenReturn(Optional.of(USER_1));
        when(mapper.toUserWithUpdate(USER_1_WITH_UPDATE_DTO, USER_1)).thenReturn(USER_1_WITH_UPDATE);
        when(userRepository.save(USER_1_WITH_UPDATE)).thenReturn(USER_1_WITH_UPDATE);
        when(mapper.toUserDto(USER_1_WITH_UPDATE)).thenReturn(USER_1_WITH_UPDATE_DTO);

        UserDto actual = userService.updateUser(1, USER_1_WITH_UPDATE_DTO);

        assertThat(actual, equalTo(USER_1_WITH_UPDATE_DTO));
    }

    @Test
    void whenUpdateUserWithWrongId_thenThrowUserNotFoundException() {
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        UserNotFoundException ex = assertThrows(UserNotFoundException.class,
                () -> userService.updateUser(1, USER_1_WITH_UPDATE_DTO));

        assertThat(ex.getMessage(), equalTo("Пользователь [1] не найден"));
        verify(userRepository, times(0)).save(any());
    }

    @Test
    void whenGetUserById_thenUserReturned() {
        when(userRepository.findById(1)).thenReturn(Optional.of(USER_1));
        when(mapper.toUserDto(USER_1)).thenReturn(USER_1_DTO);

        UserDto actual = userService.getUserById(1);

        assertThat(actual, equalTo(USER_1_DTO));
    }

    @Test
    void whenGetUserByIdWithWrongId_thenThrowUserNotFoundException() {
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        UserNotFoundException ex = assertThrows(UserNotFoundException.class,
                () -> userService.getUserById(1));

        assertThat(ex.getMessage(), equalTo("Пользователь [1] не найден"));
    }

    @Test
    void whenDeleteUserById_thenUserDeleted() {
        when(userRepository.findById(1)).thenReturn(Optional.of(USER_1));

        userService.deleteUserById(1);

        verify(userRepository, times(1)).deleteById(1);
    }

    @Test
    void whenDeleteUserByIdWithWrongId_thenThrowUserNotFoundException() {
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        UserNotFoundException ex = assertThrows(UserNotFoundException.class,
                () -> userService.deleteUserById(1));

        assertThat(ex.getMessage(), equalTo("Пользователь [1] не найден"));
    }

    @Test
    void whenGetAllUsers_thenUsersReturned() {
        when(userRepository.findAll()).thenReturn(List.of(USER_1, USER_2));
        when(mapper.toUserDto(List.of(USER_1, USER_2))).thenReturn(List.of(USER_1_DTO, USER_2_DTO));

        List<UserDto> actual = userService.getAllUsers();

        assertThat(actual, equalTo(List.of(USER_1_DTO, USER_2_DTO)));
    }
}
