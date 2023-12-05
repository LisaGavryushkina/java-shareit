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
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.shareit.error_handler.ErrorHandler;
import ru.practicum.shareit.user.UserClient;
import ru.practicum.shareit.user.UserControllerGateway;
import ru.practicum.shareit.user.UserDto;

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
public class UserControllerGatewayTest {

    @Autowired
    private ObjectMapper mapper;
    @MockBean
    private UserClient userClient;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserControllerGateway userController;

    public static final UserDto USER_DTO_1 = new UserDto(1, "user1", "user1@mail.ru");
    public static final UserDto USER_DTO_2 = new UserDto(2, "user2", "user2@mail.ru");
    public static final UserDto USER_DTO_UPDATED = new UserDto(1, "Bob", "user1@mail.ru");

    private static String getJson(String name, Object... args) throws IOException {
        try (InputStream resourceAsStream = requireNonNull(UserControllerGatewayTest.class.getResourceAsStream(name))) {
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
    void whenGetAllUsers_thenStatus200() throws Exception {
        when(userClient.getAllUsers())
                .thenReturn(ResponseEntity.ok(mapper.writeValueAsString(List.of(USER_DTO_1, USER_DTO_2))));

        mockMvc.perform(get("/users")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().json(mapper.writeValueAsString(List.of(USER_DTO_1, USER_DTO_2))));
    }

    @Test
    void whenPostUser_thenStatus200() throws Exception {
        when(userClient.postUser(any(UserDto.class)))
                .thenReturn(ResponseEntity.ok(mapper.writeValueAsString(USER_DTO_1)));

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
    void whenPostUserWithoutEmail_thenStatus400() throws Exception {

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
    void whenPostUserWithWrongEmail_thenStatus400() throws Exception {

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
    void whenUpdateUser_thenStatus200() throws Exception {
        when(userClient.updateUser(eq(1), any(UserDto.class)))
                .thenReturn(ResponseEntity.ok(mapper.writeValueAsString(USER_DTO_UPDATED)));

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
    void whenGetUserById_thenStatus200() throws Exception {
        when(userClient.getUserById(1))
                .thenReturn(ResponseEntity.ok(mapper.writeValueAsString(USER_DTO_1)));

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
    void whenDeleteUserById_thenStatus200() throws Exception {
        when(userClient.deleteUserById(1))
                .thenReturn(ResponseEntity.ok(mapper.writeValueAsString(USER_DTO_1)));

        mockMvc.perform(delete("/users/1")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(print());
    }
}
