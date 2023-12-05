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
import ru.practicum.shareit.item.CommentDto;
import ru.practicum.shareit.item.ItemClient;
import ru.practicum.shareit.item.ItemControllerGateway;
import ru.practicum.shareit.item.ItemDto;

import static java.util.Objects.requireNonNull;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ItemControllerGatewayTest {

    public static final ItemDto ITEM_DTO = new ItemDto(1, "knife", "for vegetables", true, null);
    public static final CommentDto COMMENT_DTO = new CommentDto(1, "comment from user2", 1, "user2",
            LocalDateTime.now().minusDays(2));
    @Autowired
    private ItemControllerGateway itemController;
    @Autowired
    private ObjectMapper mapper;
    @MockBean
    private ItemClient itemClient;
    @Autowired
    private MockMvc mockMvc;

    private static String getJson(String name, Object... args) throws IOException {
        try (InputStream resourceAsStream = requireNonNull(ItemControllerGatewayTest.class.getResourceAsStream(name))) {
            return String.format(new String(resourceAsStream.readAllBytes()), args);
        }
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(itemController)
                .setControllerAdvice(ErrorHandler.class)
                .build();
    }

    @Test
    void whenPostItem_thenStatus200() throws Exception {
        when(itemClient.postItem(eq(1), any(ItemDto.class)))
                .thenReturn(ResponseEntity.ok(getJson("/item/item.json")));

        mockMvc.perform(post("/items")
                        .content(getJson("/item/item_to_add.json"))
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().json(getJson("/item/item.json")));
    }

    @Test
    void whenPostItemWithoutHeaderUserId_thenStatus500() throws Exception {

        mockMvc.perform(post("/items")
                        .content(getJson("/item/item_to_add.json"))
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
    void whenPostItemWithBlankName_thenStatus400() throws Exception {

        mockMvc.perform(post("/items")
                        .content(getJson("/item/item_with_blank_name.json"))
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andExpect(jsonPath("$.error", is("must not be blank")));
    }

    @Test
    void whenPostItemWithEmptyDescription_thenStatus400() throws Exception {

        mockMvc.perform(post("/items")
                        .content(getJson("/item/item_with_empty_description.json"))
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andExpect(jsonPath("$.error", is("must not be blank")));
    }

    @Test
    void whenPostItemWithoutAvailable_thenStatus400() throws Exception {

        mockMvc.perform(post("/items")
                        .content(getJson("/item/item_without_available.json"))
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andExpect(jsonPath("$.error", is("must not be null")));
    }

    @Test
    void whenUpdateItem_thenStatus200() throws Exception {
        when(itemClient.updateItem(eq(1), eq(1), any(ItemDto.class)))
                .thenReturn(ResponseEntity.ok(getJson("/item/item.json")));

        mockMvc.perform(patch("/items/1")
                        .content(getJson("/item/item_update_available.json"))
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().json(getJson("/item/item.json")));
    }

    @Test
    void whenGetItemById_thenStatus200() throws Exception {
        when(itemClient.getItemById(1, 1))
                .thenReturn(ResponseEntity.ok(getJson("/item/item.json")));

        mockMvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().json(getJson("/item/item.json")));
    }

    @Test
    void whenGetOwnerItems_thenStatus200() throws Exception {
        when(itemClient.getOwnerItems(1, 0, 5))
                .thenReturn(ResponseEntity.ok(mapper.writeValueAsString(List.of(ITEM_DTO))));

        mockMvc.perform(get("/items?from=0&size=5")
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().json(mapper.writeValueAsString(List.of(ITEM_DTO))));
    }

    @Test
    void whenGetOwnerItemsWithNegativeFrom_thenStatus400() throws Exception {
        mockMvc.perform(get("/items?from=-1&size=5")
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andExpect(jsonPath("$.error", is("getOwnerItems.from: must be greater than or equal to 0")));
    }

    @Test
    void whenGetOwnerItemsWithZeroSize_thenStatus400() throws Exception {
        mockMvc.perform(get("/items?from=0&size=0")
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andExpect(jsonPath("$.error", is("getOwnerItems.size: must be greater than or equal to 1")));
    }

    @Test
    void whenSearch_thenStatus200() throws Exception {
        when(itemClient.searchItems("knife", 0, 5))
                .thenReturn(ResponseEntity.ok(mapper.writeValueAsString(List.of(ITEM_DTO))));

        mockMvc.perform(get("/items/search?text=knife&from=0&size=5")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().json(mapper.writeValueAsString(List.of(ITEM_DTO))));
    }

    @Test
    void whenSearchWithoutFromSize_thenStatus200andItemsReturned() throws Exception {
        when(itemClient.searchItems("knife", 0, 15))
                .thenReturn(ResponseEntity.ok(mapper.writeValueAsString(List.of(ITEM_DTO))));

        mockMvc.perform(get("/items/search?text=knife")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().json(mapper.writeValueAsString(List.of(ITEM_DTO))));
    }

    @Test
    void whenSearchWithoutText_thenStatus200andEmptyListReturned() throws Exception {
        when(itemClient.searchItems("", 0, 15))
                .thenReturn(ResponseEntity.ok(mapper.writeValueAsString(Collections.emptyList())));

        mockMvc.perform(get("/items/search")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().json(mapper.writeValueAsString(Collections.emptyList())));
    }

    @Test
    void whenSearchWithNegativeFrom_thenStatus400() throws Exception {
        mockMvc.perform(get("/items/search?text=knife&from=-1&size=5")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andExpect(jsonPath("$.error", is("searchItems.from: must be greater than or equal to 0")));
    }

    @Test
    void whenSearchWithZeroSize_thenStatus400() throws Exception {
        mockMvc.perform(get("/items/search?text=knife&from=0&size=0")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andExpect(jsonPath("$.error", is("searchItems.size: must be greater than or equal to 1")));
    }

    @Test
    void whenPostComment_thenStatus200() throws Exception {
        when(itemClient.postComment(eq(2), eq(1), any(CommentDto.class)))
                .thenReturn(ResponseEntity.ok(mapper.writeValueAsString(COMMENT_DTO)));

        mockMvc.perform(post("/items/1/comment")
                        .content(getJson("/item/post_comment.json"))
                        .header("X-Sharer-User-Id", 2)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().json(mapper.writeValueAsString(COMMENT_DTO)));
    }

    @Test
    void whenPostCommentWithBlankText_thenStatus400() throws Exception {

        mockMvc.perform(post("/items/1/comment")
                        .content(getJson("/item/comment_with_blank_text.json"))
                        .header("X-Sharer-User-Id", 2)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andExpect(jsonPath("$.error", is("must not be blank")));
    }

}
