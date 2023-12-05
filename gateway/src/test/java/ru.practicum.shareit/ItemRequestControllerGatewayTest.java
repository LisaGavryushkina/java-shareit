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
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.shareit.error_handler.ErrorHandler;
import ru.practicum.shareit.request.ItemRequestClient;
import ru.practicum.shareit.request.ItemRequestControllerGateway;
import ru.practicum.shareit.request.ItemRequestDto;

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
public class ItemRequestControllerGatewayTest {
    public static final ItemRequestDto ITEM_REQUEST_DTO_1 = new ItemRequestDto(1, "knife for vegetables", 2,
            LocalDateTime.now().minusHours(1), Collections.emptyList());
    @Autowired
    private ItemRequestControllerGateway itemRequestController;
    @Autowired
    private ObjectMapper mapper;
    @MockBean
    private ItemRequestClient itemRequestClient;
    @Autowired
    private MockMvc mockMvc;

    private static String getJson(String name, Object... args) throws IOException {
        try (InputStream resourceAsStream =
                     requireNonNull(ItemRequestControllerGateway.class.getResourceAsStream(name))) {
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
    void whenPostRequest_thenStatus200() throws Exception {
        when(itemRequestClient.postRequest(eq(2), any(ItemRequestDto.class)))
                .thenReturn(ResponseEntity.ok(mapper.writeValueAsString(ITEM_REQUEST_DTO_1)));

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
    void whenPostRequestWithoutHeaderUserId_thenStatus500() throws Exception {

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
    void whenPostRequestWithBlankDescription_thenStatus400() throws Exception {

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
    void whenGetUserRequests_thenStatus200() throws Exception {
        when(itemRequestClient.getUserRequests(2))
                .thenReturn(ResponseEntity.ok(mapper.writeValueAsString(List.of(ITEM_REQUEST_DTO_1))));

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
    void whenGetAllRequests_thenStatus200() throws Exception {
        when(itemRequestClient.getAllRequests(2, 0, 5))
                .thenReturn(ResponseEntity.ok(mapper.writeValueAsString(List.of(ITEM_REQUEST_DTO_1))));

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
    void whenGetAllRequestsWithoutFromSize_thenStatus200() throws Exception {
        when(itemRequestClient.getAllRequests(2, 0, 15))
                .thenReturn(ResponseEntity.ok(mapper.writeValueAsString(List.of(ITEM_REQUEST_DTO_1))));

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
    void whenGetAllRequestsWithNegativeFrom_thenStatus400() throws Exception {
        mockMvc.perform(get("/requests/all?from=-1&size=5")
                        .header("X-Sharer-User-Id", 2)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andExpect(jsonPath("$.error", is("getAllRequests.from: must be greater than or equal to 0")));
    }

    @Test
    void whenGetAllRequestsWithZeroSize_thenStatus400() throws Exception {
        mockMvc.perform(get("/requests/all?from=0&size=0")
                        .header("X-Sharer-User-Id", 2)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andExpect(jsonPath("$.error", is("getAllRequests.size: must be greater than or equal to 1")));
    }

    @Test
    void whenGetRequestById_thenStatus200() throws Exception {
        when(itemRequestClient.getRequestById(1, 2))
                .thenReturn(ResponseEntity.ok(mapper.writeValueAsString(ITEM_REQUEST_DTO_1)));

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

}
