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
import ru.practicum.shareit.booking.BookingForItemDto;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.error_handler.ErrorHandler;
import ru.practicum.shareit.item.CommentDto;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.ItemDto;
import ru.practicum.shareit.item.ItemNotFoundException;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.ItemWithBookingAndCommentsDto;

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
public class ItemControllerTest {

    public static final ItemDto ITEM_DTO_1 = new ItemDto(1, "knife", "for vegetables", true, null);
    public static final ItemDto ITEM_DTO_1_UPDATED = new ItemDto(1, "knife", "for vegetables", false, null);
    public static final BookingForItemDto LAST_BOOKING = new BookingForItemDto(1, LocalDateTime.now().minusDays(6),
            LocalDateTime.now().minusDays(3), 1, 2, BookingStatus.APPROVED);
    public static final BookingForItemDto NEXT_BOOKING = new BookingForItemDto(2, LocalDateTime.now().plusDays(3),
            LocalDateTime.now().plusDays(6), 1, 3, BookingStatus.WAITING);
    public static final CommentDto COMMENT_DTO = new CommentDto(1, "comment from user2", 1, "user2",
            LocalDateTime.now().minusDays(2));
    public static final ItemWithBookingAndCommentsDto ITEM_WITH_BOOKING_AND_COMMENTS_DTO_1 =
            new ItemWithBookingAndCommentsDto(1, "knife", "for vegetables",
                    true, null, LAST_BOOKING, NEXT_BOOKING, List.of(COMMENT_DTO));
    @Autowired
    private ItemController itemController;
    @Autowired
    private ObjectMapper mapper;
    @MockBean
    private ItemService itemService;
    @Autowired
    private MockMvc mockMvc;

    private static String getJson(String name, Object... args) throws IOException {
        try (InputStream resourceAsStream = requireNonNull(ItemControllerTest.class.getResourceAsStream(name))) {
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
    void whenAddItem_thenStatus200andItemAdded() throws Exception {
        when(itemService.addItem(any(), eq(1)))
                .thenReturn(ITEM_DTO_1);

        mockMvc.perform(post("/items")
                        .content(getJson("/item/item_to_add.json"))
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().json(mapper.writeValueAsString(ITEM_DTO_1)));
    }

    @Test
    void whenAddItemWithoutHeaderUserId_thenStatus500() throws Exception {

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
    void whenAddItemWithBlankName_thenStatus400() throws Exception {

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
    void whenAddItemWithEmptyDescription_thenStatus400() throws Exception {

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
    void whenAddItemWithoutAvailable_thenStatus400() throws Exception {

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
    void whenUpdateItem_thenStatus200andItemUpdated() throws Exception {
        when(itemService.updateItem(eq(1), any(), eq(1)))
                .thenReturn(ITEM_DTO_1_UPDATED);

        mockMvc.perform(patch("/items/1")
                        .content(getJson("/item/item_update_available.json"))
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().json(mapper.writeValueAsString(ITEM_DTO_1_UPDATED)));
    }

    @Test
    void whenUpdateItemNotFound_thenStatus404() throws Exception {
        when(itemService.updateItem(eq(2), any(), eq(1)))
                .thenThrow(new ItemNotFoundException(2));

        mockMvc.perform(patch("/items/2")
                        .content(getJson("/item/item_update_available.json"))
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound())
                .andDo(print())
                .andExpect(jsonPath("$.error", is("Вещь [2] не найдена")));
    }

    @Test
    void whenGetItemById_thenStatus200andItemReturned() throws Exception {
        when(itemService.getItemById(1, 1))
                .thenReturn(ITEM_WITH_BOOKING_AND_COMMENTS_DTO_1);

        mockMvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().json(mapper.writeValueAsString(ITEM_WITH_BOOKING_AND_COMMENTS_DTO_1)));
    }

    @Test
    void whenFindOwnerItems_thenStatus200andItemsReturned() throws Exception {
        when(itemService.findOwnerItems(1, 0, 5))
                .thenReturn(List.of(ITEM_WITH_BOOKING_AND_COMMENTS_DTO_1));

        mockMvc.perform(get("/items?from=0&size=5")
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().json(mapper.writeValueAsString(List.of(ITEM_WITH_BOOKING_AND_COMMENTS_DTO_1))));
    }

    @Test
    void whenFindOwnerItemsWithNegativeFrom_thenStatus400() throws Exception {
        mockMvc.perform(get("/items?from=-1&size=5")
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andExpect(jsonPath("$.error", is("findOwnerItems.from: must be greater than or equal to 0")));
    }

    @Test
    void whenFindOwnerItemsWithZeroSize_thenStatus400() throws Exception {
        mockMvc.perform(get("/items?from=0&size=0")
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andExpect(jsonPath("$.error", is("findOwnerItems.size: must be greater than or equal to 1")));
    }

    @Test
    void whenSearch_thenStatus200andItemsReturned() throws Exception {
        when(itemService.findItems("knife", 0, 5))
                .thenReturn(List.of(ITEM_DTO_1));

        mockMvc.perform(get("/items/search?text=knife&from=0&size=5")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().json(mapper.writeValueAsString(List.of(ITEM_DTO_1))));
    }

    @Test
    void whenSearchWithoutFromSize_thenStatus200andItemsReturned() throws Exception {
        when(itemService.findItems("knife", 0, 15))
                .thenReturn(List.of(ITEM_DTO_1));

        mockMvc.perform(get("/items/search?text=knife")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().json(mapper.writeValueAsString(List.of(ITEM_DTO_1))));
    }

    @Test
    void whenSearchWithoutText_thenStatus200andEmptyListReturned() throws Exception {
        when(itemService.findItems("", 0, 15))
                .thenReturn(Collections.emptyList());

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
                .andExpect(jsonPath("$.error", is("findItems.from: must be greater than or equal to 0")));
    }

    @Test
    void whenSearchWithZeroSize_thenStatus400() throws Exception {
        mockMvc.perform(get("/items/search?text=knife&from=0&size=0")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andExpect(jsonPath("$.error", is("findItems.size: must be greater than or equal to 1")));
    }

    @Test
    void whenPostComment_thenStatus200andCommentPosted() throws Exception {
        when(itemService.postComment(eq(1), eq(2), any(CommentDto.class)))
                .thenReturn(COMMENT_DTO);

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
