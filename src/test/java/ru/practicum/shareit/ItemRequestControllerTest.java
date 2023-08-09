package ru.practicum.shareit;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
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
import ru.practicum.shareit.request.ItemRequestController;
import ru.practicum.shareit.request.ItemRequestDto;
import ru.practicum.shareit.request.ItemRequestNotFoundException;
import ru.practicum.shareit.request.ItemRequestService;
import ru.practicum.shareit.user.UserNotFoundException;

import static java.util.Objects.requireNonNull;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ItemRequestControllerTest {
    public static final ItemRequestDto ITEM_REQUEST_DTO_1 = new ItemRequestDto(1, "knife for vegetables", 2,
            LocalDateTime.now().minusHours(1), Collections.emptyList());
    @Autowired
    private ItemRequestController itemRequestController;
    @Autowired
    private ObjectMapper mapper;
    @MockBean
    private ItemRequestService itemRequestService;
    @Autowired
    private MockMvc mockMvc;

    private static String getJson(String name, Object... args) throws IOException {
        try (InputStream resourceAsStream = requireNonNull(ItemRequestController.class.getResourceAsStream(name))) {
            return String.format(new String(resourceAsStream.readAllBytes()), args);
        }
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(itemRequestController)
                .setControllerAdvice(ErrorHandler.class)
                .build();
    }

    @Test
    void whenAddRequest_thenStatus200andRequestAdded() throws Exception {
        when(itemRequestService.addRequest(any(ItemRequestDto.class), eq(2), any(LocalDateTime.class)))
                .thenReturn(ITEM_REQUEST_DTO_1);

        mockMvc.perform(post("/requests")
                        .content(getJson("/request/request_to_add.json"))
                        .header("X-Sharer-User-Id", 2)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().json(mapper.writeValueAsString(ITEM_REQUEST_DTO_1)));
    }

    @Test
    void whenAddRequestWithoutHeaderUserId_thenStatus500() throws Exception {

        mockMvc.perform(post("/requests")
                        .content(getJson("/request/request_to_add.json"))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isInternalServerError())
                .andDo(print())
                .andExpect(jsonPath("$.error", is("Required request header 'X-Sharer-User-Id' for method parameter " +
                        "type int is not present")));
    }

    @Test
    void whenAddRequestWithBlankDescription_thenStatus400() throws Exception {

        mockMvc.perform(post("/requests")
                        .content(getJson("/request/request_with_blank_description.json"))
                        .header("X-Sharer-User-Id", 2)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andExpect(jsonPath("$.error", is("must not be blank")));
    }

    @Test
    void whenFindUserRequests_thenStatus200andRequestsReturned() throws Exception {
        when(itemRequestService.findUserRequests(2))
                .thenReturn(List.of(ITEM_REQUEST_DTO_1));

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 2)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().json(mapper.writeValueAsString(List.of(ITEM_REQUEST_DTO_1))));
    }

    @Test
    void whenFindUserRequestsAndUserNotFound_thenStatus404() throws Exception {
        when(itemRequestService.findUserRequests(2))
                .thenThrow(new UserNotFoundException(2));

        mockMvc.perform(get("/requests")
                        .header("X-Sharer-User-Id", 2)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound())
                .andDo(print())
                .andExpect(jsonPath("$.error", is("Пользователь [2] не найден")));
    }

    @Test
    void whenFindAllRequests_thenStatus200andRequestsReturned() throws Exception {
        when(itemRequestService.findAllRequests(0, 5, 2))
                .thenReturn(List.of(ITEM_REQUEST_DTO_1));

        mockMvc.perform(get("/requests/all?from=0&size=5")
                        .header("X-Sharer-User-Id", 2)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().json(mapper.writeValueAsString(List.of(ITEM_REQUEST_DTO_1))));
    }

    @Test
    void whenFindAllRequestsWithoutFromSize_thenStatus200andRequestsReturned() throws Exception {
        when(itemRequestService.findAllRequests(0, 15, 2))
                .thenReturn(List.of(ITEM_REQUEST_DTO_1));

        mockMvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 2)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().json(mapper.writeValueAsString(List.of(ITEM_REQUEST_DTO_1))));
    }


    @Test
    void whenFindAllRequestsWithNegativeFrom_thenStatus400() throws Exception {
        mockMvc.perform(get("/requests/all?from=-1&size=5")
                        .header("X-Sharer-User-Id", 2)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andExpect(jsonPath("$.error", is("findAllRequests.from: must be greater than or equal to 0")));
    }

    @Test
    void whenFindAllRequestsWithZeroSize_thenStatus400() throws Exception {
        mockMvc.perform(get("/requests/all?from=0&size=0")
                        .header("X-Sharer-User-Id", 2)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andExpect(jsonPath("$.error", is("findAllRequests.size: must be greater than or equal to 1")));
    }

    @Test
    void whenFindRequestById_thenStatus200andRequestReturned() throws Exception {
        when(itemRequestService.findRequestById(1, 2))
                .thenReturn(ITEM_REQUEST_DTO_1);

        mockMvc.perform(get("/requests/1")
                        .header("X-Sharer-User-Id", 2)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().json(mapper.writeValueAsString(ITEM_REQUEST_DTO_1)));
    }

    @Test
    void whenFindRequestByIdNotFound_thenStatus404() throws Exception {
        when(itemRequestService.findRequestById(1, 2))
                .thenThrow(new ItemRequestNotFoundException(1));

        mockMvc.perform(get("/requests/1")
                        .header("X-Sharer-User-Id", 2)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound())
                .andDo(print())
                .andExpect(jsonPath("$.error", is("Запрос [1] не найден")));
    }

}
