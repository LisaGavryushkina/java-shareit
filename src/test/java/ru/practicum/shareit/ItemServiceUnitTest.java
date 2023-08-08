package ru.practicum.shareit;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingForItemDto;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingNotFoundException;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.error_handler.ShareitInvalidArgumentException;
import ru.practicum.shareit.item.Comment;
import ru.practicum.shareit.item.CommentDto;
import ru.practicum.shareit.item.CommentMapper;
import ru.practicum.shareit.item.CommentRepository;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemDto;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.ItemNotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.ItemServiceImpl;
import ru.practicum.shareit.item.ItemWithBookingAndCommentsDto;
import ru.practicum.shareit.pageable.OffsetPageRequest;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestNotFoundException;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserNotFoundException;
import ru.practicum.shareit.user.UserRepository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static ru.practicum.shareit.item.ItemRepository.ItemWithBooking;

@ExtendWith(MockitoExtension.class)
public class ItemServiceUnitTest {
    public static final User USER_1 = new User(1, "user1", "user1@mail.ru");
    public static final User USER_2 = new User(2, "user2", "user2@mail.ru");
    public static final ItemRequest REQUEST_1 = new ItemRequest(1, "plate for soup", USER_2,
            LocalDateTime.now().minusDays(1), Collections.emptyList());
    public static final Item ITEM_2 = new Item(2, "plate", "for soup", true, USER_1, REQUEST_1);
    public static final Item ITEM_1 = new Item(1, "knife", "for vegetables", true, USER_1, null);
    public static final Comment COMMENT = new Comment(1, "comment from user2", ITEM_1, USER_2,
            LocalDateTime.now().minusDays(2));
    public static final Item ITEM_1_UPDATED = new Item(1, "knife", "for vegetables", false, USER_1, null);
    public static final ItemDto ITEM_DTO_1 = new ItemDto(1, "knife", "for vegetables", true, null);
    public static final ItemDto ITEM_DTO_1_UPDATE = new ItemDto(1, "knife", "for vegetables", false, null);
    public static final ItemDto ITEM_DTO_2 = new ItemDto(2, "plate", "for soup", true, 1);
    public static final CommentDto COMMENT_DTO = new CommentDto(1, "comment from user2", 1, "user2",
            LocalDateTime.now().minusDays(2));
    public static final ItemWithBookingAndCommentsDto ITEM_WITHOUT_BOOKING_AND_COMMENTS_DTO =
            new ItemWithBookingAndCommentsDto(1, "knife", "for vegetables", true, null,
                    null, null, List.of(COMMENT_DTO));
    private static final LocalDateTime START_1 = LocalDateTime.of(2023, Month.AUGUST, 1, 0, 0);
    private static final LocalDateTime START_2 = LocalDateTime.now().minusDays(1);
    private static final LocalDateTime END_1 = LocalDateTime.of(2023, Month.AUGUST, 4, 0, 0);
    public static final Booking BOOKING_1 = new Booking(1, START_1, END_1, ITEM_1, USER_2, BookingStatus.WAITING);
    public static final BookingForItemDto BOOKING_FOR_ITEM_DTO_1 = new BookingForItemDto(1, START_1, END_1, 1, 2,
            BookingStatus.WAITING);
    private static final LocalDateTime END_2 = LocalDateTime.now().plusDays(5);
    public static final Booking BOOKING_2 = new Booking(2, START_2, END_2, ITEM_1, USER_2, BookingStatus.WAITING);
    public static final BookingForItemDto BOOKING_FOR_ITEM_DTO_2 = new BookingForItemDto(2, START_2, END_2, 1, 2,
            BookingStatus.WAITING);
    public static final ItemWithBookingAndCommentsDto ITEM_WITH_BOOKING_AND_COMMENTS_DTO =
            new ItemWithBookingAndCommentsDto(1, "knife", "for vegetables", true, null,
                    BOOKING_FOR_ITEM_DTO_1, BOOKING_FOR_ITEM_DTO_2, List.of(COMMENT_DTO));
    private ItemService itemService;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private ItemMapper itemMapper;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private BookingMapper bookingMapper;
    @Mock
    private ItemRequestRepository itemRequestRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private CommentMapper commentMapper;

    @BeforeEach
    public void start() {
        itemService = new ItemServiceImpl(itemRepository, itemMapper, userRepository, bookingRepository,
                bookingMapper, itemRequestRepository, commentRepository, commentMapper);
    }

    @Test
    void whenAddItem_thenItemAdded() {

        when(userRepository.findById(1)).thenReturn(Optional.of(USER_1));
        when(itemMapper.toItem(ITEM_DTO_1, USER_1, null)).thenReturn(ITEM_1);
        when(itemRepository.save(ITEM_1)).thenReturn(ITEM_1);
        when(itemMapper.toItemDto(ITEM_1)).thenReturn(ITEM_DTO_1);

        ItemDto actual = itemService.addItem(ITEM_DTO_1, 1);

        verify(itemRepository, times(1)).save(ITEM_1);
        assertThat(actual, equalTo(ITEM_DTO_1));
    }

    @Test
    void whenAddItemWithWrongUserId_thenThrowUserNotFoundException() {

        when(userRepository.findById(1)).thenReturn(Optional.empty());

        UserNotFoundException ex = assertThrows(UserNotFoundException.class,
                () -> itemService.addItem(ITEM_DTO_1, 1));

        assertThat(ex.getMessage(), equalTo("Пользователь [1] не найден"));
        verifyNoInteractions(itemRepository);
    }

    @Test
    void whenAddItemWithWrongRequestId_thenThrowItemRequestNotFoundException() {

        when(userRepository.findById(1)).thenReturn(Optional.of(USER_1));
        when(itemRequestRepository.findById(1)).thenReturn(Optional.empty());

        ItemRequestNotFoundException ex = assertThrows(ItemRequestNotFoundException.class,
                () -> itemService.addItem(ITEM_DTO_2, 1));

        assertThat(ex.getMessage(), equalTo("Запрос [1] не найден"));
        verifyNoInteractions(itemRepository);
    }

    @Test
    void whenUpdateItem_thenItemUpdated() {

        when(itemRepository.findById(1)).thenReturn(Optional.of(ITEM_1));
        when(itemMapper.toItemWithUpdate(ITEM_DTO_1_UPDATE, ITEM_1)).thenReturn(ITEM_1_UPDATED);
        when(itemRepository.save(ITEM_1_UPDATED)).thenReturn(ITEM_1_UPDATED);
        when(itemMapper.toItemDto(ITEM_1_UPDATED)).thenReturn(ITEM_DTO_1_UPDATE);

        ItemDto actual = itemService.updateItem(1, ITEM_DTO_1_UPDATE, 1);

        verify(itemRepository, times(1)).save(ITEM_1_UPDATED);
        assertThat(actual, equalTo(ITEM_DTO_1_UPDATE));
    }

    @Test
    void whenUpdateItemWithWrongItemId_thenThrowItemNotFoundException() {

        when(itemRepository.findById(1)).thenReturn(Optional.empty());

        ItemNotFoundException ex = assertThrows(ItemNotFoundException.class,
                () -> itemService.updateItem(1, ITEM_DTO_1_UPDATE, 1));

        assertThat(ex.getMessage(), equalTo("Вещь [1] не найдена"));
        verifyNoMoreInteractions(itemRepository);
    }

    @Test
    void whenUpdateItemWithNotOwnerId_thenThrowUserNotFoundException() {

        when(itemRepository.findById(1)).thenReturn(Optional.of(ITEM_1));

        UserNotFoundException ex = assertThrows(UserNotFoundException.class,
                () -> itemService.updateItem(1, ITEM_DTO_1_UPDATE, 2));

        assertThat(ex.getMessage(), equalTo("Пользователь [2] не найден"));
        verifyNoMoreInteractions(itemRepository);
    }

    @Test
    void whenGetItemByIdWithOwnerId_thenItemWithBookingsReturned() {

        when(itemRepository.findById(1)).thenReturn(Optional.of(ITEM_1));
        ItemWithBooking itemWithBooking = mock(ItemWithBooking.class);
        when(itemRepository.findItemWithBookingsByItemId(eq(1), any(LocalDateTime.class)))
                .thenReturn(itemWithBooking);
        when(itemWithBooking.getLastBooking()).thenReturn(1);
        when(itemWithBooking.getNextBooking()).thenReturn(2);
        when(bookingRepository.findById(1)).thenReturn(Optional.of(BOOKING_1));
        when(bookingMapper.toBookingForItemDto(BOOKING_1)).thenReturn(BOOKING_FOR_ITEM_DTO_1);
        when(bookingRepository.findById(2)).thenReturn(Optional.of(BOOKING_2));
        when(bookingMapper.toBookingForItemDto(BOOKING_2)).thenReturn(BOOKING_FOR_ITEM_DTO_2);
        when(commentRepository.findAllByItemId(1)).thenReturn(List.of(COMMENT));
        when(commentMapper.toCommentDto(COMMENT)).thenReturn(COMMENT_DTO);
        when(itemMapper.toItemWithBookingAndCommentsDto(itemWithBooking, BOOKING_FOR_ITEM_DTO_1,
                BOOKING_FOR_ITEM_DTO_2, List.of(COMMENT_DTO))).thenReturn(ITEM_WITH_BOOKING_AND_COMMENTS_DTO);

        ItemWithBookingAndCommentsDto actual = itemService.getItemById(1, 1);

        assertThat(actual, equalTo(ITEM_WITH_BOOKING_AND_COMMENTS_DTO));
    }

    @Test
    void whenGetItemByIdAndNextBookingNotFound_thenThrowBookingNotFoundException() {

        when(itemRepository.findById(1)).thenReturn(Optional.of(ITEM_1));
        ItemWithBooking itemWithBooking = mock(ItemWithBooking.class);
        when(itemRepository.findItemWithBookingsByItemId(eq(1), any(LocalDateTime.class)))
                .thenReturn(itemWithBooking);
        when(itemWithBooking.getLastBooking()).thenReturn(1);
        when(itemWithBooking.getNextBooking()).thenReturn(2);
        when(bookingRepository.findById(1)).thenReturn(Optional.of(BOOKING_1));
        when(bookingMapper.toBookingForItemDto(BOOKING_1)).thenReturn(BOOKING_FOR_ITEM_DTO_1);
        when(bookingRepository.findById(2)).thenReturn(Optional.empty());

        BookingNotFoundException ex = assertThrows(BookingNotFoundException.class,
                () -> itemService.getItemById(1, 1));

        assertThat(ex.getMessage(), equalTo("Бронирование [2] не найдено"));
    }

    @Test
    void whenGetItemByIdAndLastBookingNotFound_thenThrowBookingNotFoundException() {

        when(itemRepository.findById(1)).thenReturn(Optional.of(ITEM_1));
        ItemWithBooking itemWithBooking = mock(ItemWithBooking.class);
        when(itemRepository.findItemWithBookingsByItemId(eq(1), any(LocalDateTime.class)))
                .thenReturn(itemWithBooking);
        when(itemWithBooking.getLastBooking()).thenReturn(1);
        when(itemWithBooking.getNextBooking()).thenReturn(2);
        when(bookingRepository.findById(1)).thenReturn(Optional.empty());

        BookingNotFoundException ex = assertThrows(BookingNotFoundException.class,
                () -> itemService.getItemById(1, 1));

        assertThat(ex.getMessage(), equalTo("Бронирование [1] не найдено"));
    }

    @Test
    void whenGetItemByIdWithNotOwnerId_thenItemWithoutBookingsReturned() {

        when(itemRepository.findById(1)).thenReturn(Optional.of(ITEM_1));
        ItemWithBooking itemWithBooking = mock(ItemWithBooking.class);
        when(itemRepository.findItemWithBookingsByItemId(eq(1), any(LocalDateTime.class)))
                .thenReturn(itemWithBooking);
        when(commentRepository.findAllByItemId(1)).thenReturn(List.of(COMMENT));
        when(commentMapper.toCommentDto(COMMENT)).thenReturn(COMMENT_DTO);
        when(itemMapper.toItemWithBookingAndCommentsDto(itemWithBooking, null,
                null, List.of(COMMENT_DTO))).thenReturn(ITEM_WITHOUT_BOOKING_AND_COMMENTS_DTO);

        ItemWithBookingAndCommentsDto actual = itemService.getItemById(1, 2);

        assertThat(actual, equalTo(ITEM_WITHOUT_BOOKING_AND_COMMENTS_DTO));
        verifyNoInteractions(bookingRepository);
    }

    @Test
    void whenGetItemByIdWithWrongId_thenThrowItemNotFoundException() {

        when(itemRepository.findById(1)).thenReturn(Optional.empty());

        ItemNotFoundException ex = assertThrows(ItemNotFoundException.class,
                () -> itemService.getItemById(1, 1));

        assertThat(ex.getMessage(), equalTo("Вещь [1] не найдена"));
    }

    @Test
    void whenFindOwnerItems_thenItemsWithBookingsReturned() {

        when(userRepository.findById(1)).thenReturn(Optional.of(USER_1));
        ItemWithBooking itemWithBooking = mock(ItemWithBooking.class);
        when(itemRepository.findItemsWithBookingsByOwnerId(eq(1), any(LocalDateTime.class),
                any(OffsetPageRequest.class))).thenReturn(new PageImpl<>(List.of(itemWithBooking)));
        when(itemWithBooking.getLastBooking()).thenReturn(1);
        when(itemWithBooking.getNextBooking()).thenReturn(2);
        when(bookingRepository.findAllById(any())).thenReturn(List.of(BOOKING_1, BOOKING_2));
        when(bookingMapper.toBookingForItemDto(BOOKING_1)).thenReturn(BOOKING_FOR_ITEM_DTO_1);
        when(bookingMapper.toBookingForItemDto(BOOKING_2)).thenReturn(BOOKING_FOR_ITEM_DTO_2);
        when(itemWithBooking.getId()).thenReturn(1);
        when(commentRepository.findAllByItemIdIn(List.of(1))).thenReturn(List.of(COMMENT));
        when(commentMapper.toCommentDto(COMMENT)).thenReturn(COMMENT_DTO);
        when(itemMapper.toItemWithBookingAndCommentsDto(List.of(itemWithBooking), List.of(BOOKING_FOR_ITEM_DTO_1,
                BOOKING_FOR_ITEM_DTO_2), List.of(COMMENT_DTO))).thenReturn(List.of(ITEM_WITH_BOOKING_AND_COMMENTS_DTO));

        List<ItemWithBookingAndCommentsDto> actual = itemService.findOwnerItems(1, 0, 5);

        assertThat(actual.get(0), equalTo(ITEM_WITH_BOOKING_AND_COMMENTS_DTO));
    }

    @Test
    void whenFindOwnerItemsWithWrongOwnerId_thenThrowUserNotFoundException() {

        when(userRepository.findById(1)).thenReturn(Optional.empty());

        UserNotFoundException ex = assertThrows(UserNotFoundException.class,
                () -> itemService.findOwnerItems(1, 0, 5));

        assertThat(ex.getMessage(), equalTo("Пользователь [1] не найден"));
    }

    @Test
    void whenFindItems_thenReturnItems() {

        when(itemRepository.findAllByText("knife", new OffsetPageRequest(0, 5)))
                .thenReturn(new PageImpl<>(List.of(ITEM_1)));
        when(itemMapper.toItemDto(List.of(ITEM_1))).thenReturn(List.of(ITEM_DTO_1));

        List<ItemDto> actual = itemService.findItems("knife", 0, 5);

        assertThat(actual.get(0), equalTo(ITEM_DTO_1));
    }

    @Test
    void whenFindItemsWithBlankText_thenReturnEmptyList() {

        List<ItemDto> actual = itemService.findItems("", 0, 5);

        assertThat(actual, equalTo(Collections.emptyList()));
    }

    @Test
    void whenPostComment_thenCommentPosted() {
        when(userRepository.findById(2)).thenReturn(Optional.of(USER_2));
        when(itemRepository.findById(1)).thenReturn(Optional.of(ITEM_1));
        when(bookingRepository.findByItemIdAndBookerId(1, 2)).thenReturn(Optional.of(BOOKING_1));
        when(commentMapper.toComment(eq(COMMENT_DTO), eq(ITEM_1), eq(USER_2), any(LocalDateTime.class))).thenReturn(COMMENT);
        when(commentRepository.save(COMMENT)).thenReturn(COMMENT);
        when(commentMapper.toCommentDto(COMMENT)).thenReturn(COMMENT_DTO);

        CommentDto actual = itemService.postComment(1, 2, COMMENT_DTO);

        assertThat(actual, equalTo(COMMENT_DTO));
    }

    @Test
    void whenPostCommentAndBookingIsNotFinished_thenThrowShareitInvalidArgumentException() {
        when(userRepository.findById(2)).thenReturn(Optional.of(USER_2));
        when(itemRepository.findById(1)).thenReturn(Optional.of(ITEM_1));
        when(bookingRepository.findByItemIdAndBookerId(1, 2)).thenReturn(Optional.of(BOOKING_2));

        ShareitInvalidArgumentException ex = assertThrows(ShareitInvalidArgumentException.class,
                () -> itemService.postComment(1, 2, COMMENT_DTO));

        assertThat(ex.getMessage(), equalTo("Добавить комментарий можно только после окончания периода " +
                "бронирования"));
    }

    @Test
    void whenPostCommentAndBookingNotFound_thenThrowShareitInvalidArgumentException() {
        when(userRepository.findById(2)).thenReturn(Optional.of(USER_2));
        when(itemRepository.findById(1)).thenReturn(Optional.of(ITEM_1));
        when(bookingRepository.findByItemIdAndBookerId(1, 2)).thenReturn(Optional.empty());

        ShareitInvalidArgumentException ex = assertThrows(ShareitInvalidArgumentException.class,
                () -> itemService.postComment(1, 2, COMMENT_DTO));

        assertThat(ex.getMessage(), equalTo("Добавить комментарий можно только после оформления и" +
                " окончания бронирования"));
    }

    @Test
    void whenPostCommentAndItemNotFound_thenThrowItemNotFoundException() {
        when(userRepository.findById(2)).thenReturn(Optional.of(USER_2));
        when(itemRepository.findById(1)).thenReturn(Optional.empty());

        ItemNotFoundException ex = assertThrows(ItemNotFoundException.class,
                () -> itemService.postComment(1, 2, COMMENT_DTO));

        assertThat(ex.getMessage(), equalTo("Вещь [1] не найдена"));
    }

    @Test
    void whenPostCommentAndUserNotFound_thenThrowUserNotFoundException() {
        when(userRepository.findById(2)).thenReturn(Optional.empty());

        UserNotFoundException ex = assertThrows(UserNotFoundException.class,
                () -> itemService.postComment(1, 2, COMMENT_DTO));

        assertThat(ex.getMessage(), equalTo("Пользователь [2] не найден"));
    }


}
