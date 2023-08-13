package ru.practicum.shareit;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.shareit.error_handler.ErrorHandler;
import ru.practicum.shareit.user.EmailAlreadyExistException;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.UserNotFoundException;
import ru.practicum.shareit.user.UserService;

import static java.util.Objects.requireNonNull;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {

    public static final UserDto USER_DTO_1 = new UserDto(1, "user1", "user1@mail.ru");
    public static final UserDto USER_DTO_2 = new UserDto(2, "user2", "user2@mail.ru");
    public static final UserDto USER_DTO_UPDATED = new UserDto(1, "Bob", "user1@mail.ru");
    @Autowired
    private UserController userController;
    @Autowired
    private ObjectMapper mapper;
    @MockBean
    private UserService userService;
    @Autowired
    private MockMvc mockMvc;

    private static String getJson(String name, Object... args) throws IOException {
        try (InputStream resourceAsStream = requireNonNull(UserControllerTest.class.getResourceAsStream(name))) {
            return String.format(new String(resourceAsStream.readAllBytes()), args);
        }
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(userController)
                .setControllerAdvice(ErrorHandler.class)
                .build();
    }

    @Test
    void whenGetAllUsers_thenStatus200andUsersReturned() throws Exception {
        when(userService.getAllUsers())
                .thenReturn(List.of(USER_DTO_1, USER_DTO_2));

        mockMvc.perform(get("/users")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().json(mapper.writeValueAsString(List.of(USER_DTO_1, USER_DTO_2))));
    }

    @Test
    void whenAddUser_thenStatus200andUserAdded() throws Exception {
        when(userService.addUser(any(UserDto.class)))
                .thenReturn(USER_DTO_1);

        mockMvc.perform(post("/users")
                        .content(getJson("/user/user_to_add.json"))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().json(mapper.writeValueAsString(USER_DTO_1)));
    }

    @Test
    void whenAddUserWithoutEmail_thenStatus400() throws Exception {

        mockMvc.perform(post("/users")
                        .content(getJson("/user/user_without_email.json"))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andExpect(jsonPath("$.error", is("must not be null")));
    }

    @Test
    void whenAddUserWithWrongEmail_thenStatus400() throws Exception {

        mockMvc.perform(post("/users")
                        .content(getJson("/user/user_with_wrong_email.json"))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andExpect(jsonPath("$.error", is("Введенный email не соответсвует формату")));
    }

    @Test
    void whenAddUserAndEmailAlreadyExist_thenStatus409() throws Exception {
        when(userService.addUser(any(UserDto.class)))
                .thenThrow(new EmailAlreadyExistException("user1@mail.ru"));

        mockMvc.perform(post("/users")
                        .content(getJson("/user/user_to_add.json"))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isConflict())
                .andDo(print())
                .andExpect(jsonPath("$.error", is("Email: user1@mail.ru занят")));
    }

    @Test
    void whenUpdateUser_thenStatus200andUserUpdated() throws Exception {
        when(userService.updateUser(eq(1), any(UserDto.class)))
                .thenReturn(USER_DTO_UPDATED);

        mockMvc.perform(patch("/users/1")
                        .content(getJson("/user/user_to_update.json"))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().json(mapper.writeValueAsString(USER_DTO_UPDATED)));
    }

    @Test
    void whenUpdateAndUserNotFound_thenStatus404() throws Exception {
        when(userService.updateUser(eq(1), any(UserDto.class)))
                .thenThrow(new UserNotFoundException(1));

        mockMvc.perform(patch("/users/1")
                        .content(getJson("/user/user_to_update.json"))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound())
                .andDo(print())
                .andExpect(jsonPath("$.error", is("Пользователь [1] не найден")));
    }

    @Test
    void whenGetUserById_thenStatus200andUserReturned() throws Exception {
        when(userService.getUserById(1))
                .thenReturn(USER_DTO_1);

        mockMvc.perform(get("/users/1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().json(mapper.writeValueAsString(USER_DTO_1)));
    }

    @Test
    void whenDeleteUserById_thenStatus200andUserDeleted() throws Exception {

        mockMvc.perform(delete("/users/1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(print());
    }
}
