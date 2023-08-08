package ru.practicum.shareit;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingNotFoundException;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingRequestDto;
import ru.practicum.shareit.booking.BookingResponseDto;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.BookingServiceImpl;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.error_handler.ShareitInvalidArgumentException;
import ru.practicum.shareit.error_handler.ShareitNotFoundException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemDto;
import ru.practicum.shareit.item.ItemNotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.pageable.OffsetPageRequest;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.UserNotFoundException;
import ru.practicum.shareit.user.UserRepository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BookingServiceUnitTest {
    public static final User OWNER = new User(1, "user1", "user1@mail.ru");
    public static final User BOOKER = new User(2, "user2", "user2@mail.ru");
    public static final UserDto BOOKER_DTO = new UserDto(2, "user2", "user2@mail.ru");
    public static final Item ITEM = new Item(1, "knife", "for vegetables", true, OWNER, null);
    public static final ItemDto ITEM_DTO = new ItemDto(1, "knife", "for vegetables", true, null);
    private static final LocalDateTime START = LocalDateTime.of(2023, Month.AUGUST, 1, 0, 0);
    private static final LocalDateTime END = LocalDateTime.of(2023, Month.SEPTEMBER, 1, 0, 0);
    public static final BookingRequestDto BOOKING_REQUEST_1_DTO = BookingRequestDto.builder()
            .start(START)
            .end(END)
            .itemId(1)
            .build();
    public static final Booking BOOKING_1 = new Booking(1, START, END, ITEM, BOOKER, BookingStatus.WAITING);
    public static final BookingResponseDto BOOKING_RESPONSE_1_DTO = BookingResponseDto.builder()
            .id(1)
            .start(START)
            .end(END)
            .item(ITEM_DTO)
            .booker(BOOKER_DTO)
            .status(BookingStatus.WAITING)
            .build();
    private BookingService bookingService;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private BookingMapper mapper;

    @BeforeEach
    public void start() {
        bookingService = new BookingServiceImpl(bookingRepository, userRepository, itemRepository, mapper);
    }

    @Test
    void whenAddBooking_thenBookingAdded() {

        when(userRepository.findById(2)).thenReturn(Optional.of(BOOKER));
        when(itemRepository.findById(1)).thenReturn(Optional.of(ITEM));
        when(mapper.toBooking(BOOKING_REQUEST_1_DTO, ITEM, BOOKER, BookingStatus.WAITING)).thenReturn(BOOKING_1);
        when(bookingRepository.save(BOOKING_1)).thenReturn(BOOKING_1);
        when(mapper.toBookingResponseDto(BOOKING_1)).thenReturn(BOOKING_RESPONSE_1_DTO);

        BookingResponseDto actual = bookingService.addBooking(BOOKING_REQUEST_1_DTO, 2);

        verify(bookingRepository, times(1)).save(BOOKING_1);
        assertThat(actual, equalTo(BOOKING_RESPONSE_1_DTO));
    }

    @Test
    void whenAddBookingWithWrongUserId_thenThrowUserNotFoundException() {

        when(userRepository.findById(2)).thenReturn(Optional.empty());

        UserNotFoundException ex = assertThrows(UserNotFoundException.class,
                () -> bookingService.addBooking(BOOKING_REQUEST_1_DTO, 2));

        assertThat(ex.getMessage(), equalTo("Пользователь [2] не найден"));
        verifyNoInteractions(bookingRepository);
    }

    @Test
    void whenAddBookingWithWrongItemId_thenThrowItemNotFoundException() {

        when(userRepository.findById(2)).thenReturn(Optional.of(BOOKER));
        when(itemRepository.findById(1)).thenReturn(Optional.empty());

        ItemNotFoundException ex = assertThrows(ItemNotFoundException.class,
                () -> bookingService.addBooking(BOOKING_REQUEST_1_DTO, 2));

        assertThat(ex.getMessage(), equalTo("Вещь [1] не найдена"));
        verifyNoInteractions(bookingRepository);
    }

    @Test
    void whenAddBookingWithOwnerId_thenThrowShareitNotFoundException() {

        when(userRepository.findById(1)).thenReturn(Optional.of(OWNER));
        when(itemRepository.findById(1)).thenReturn(Optional.of(ITEM));

        ShareitNotFoundException ex = assertThrows(ShareitNotFoundException.class,
                () -> bookingService.addBooking(BOOKING_REQUEST_1_DTO, 1));

        assertThat(ex.getMessage(), equalTo("Владелец вещи не может ее забронировать"));
        verifyNoInteractions(bookingRepository);
    }

    @Test
    void whenAddBookingWithUnavailableItem_thenThrowShareitInvalidArgumentException() {

        BookingRequestDto bookingRequestDto = BookingRequestDto.builder()
                .start(START)
                .end(END)
                .itemId(2)
                .build();
        Item itemUnavailable = new Item(2, "item", "*", false, OWNER, null);
        when(userRepository.findById(2)).thenReturn(Optional.of(BOOKER));
        when(itemRepository.findById(2)).thenReturn(Optional.of(itemUnavailable));

        ShareitInvalidArgumentException ex = assertThrows(ShareitInvalidArgumentException.class,
                () -> bookingService.addBooking(bookingRequestDto, 2));

        assertThat(ex.getMessage(), equalTo("Вещь 2 недоступна для бронирования"));
        verifyNoInteractions(bookingRepository);
    }

    @Test
    void whenApproveBooking_thenBookingApproved() {

        Booking bookingWaiting = new Booking(1, START, END, ITEM, BOOKER, BookingStatus.WAITING);
        Booking bookingApproved = new Booking(1, START, END, ITEM, BOOKER, BookingStatus.APPROVED);
        BookingResponseDto bookingResponseDto = BookingResponseDto.builder()
                .id(1)
                .start(START)
                .end(END)
                .item(ITEM_DTO)
                .booker(BOOKER_DTO)
                .status(BookingStatus.APPROVED)
                .build();
        when(bookingRepository.findById(1)).thenReturn(Optional.of(bookingWaiting));
        when(bookingRepository.save(bookingApproved)).thenReturn(bookingApproved);
        when(mapper.toBookingResponseDto(bookingApproved)).thenReturn(bookingResponseDto);

        BookingResponseDto actual = bookingService.approveOrRejectBooking(1, 1, true);
        verify(bookingRepository, times(1)).save(bookingApproved);
        assertThat(actual, equalTo(bookingResponseDto));

    }

    @Test
    void whenApproveBookingNotFound_thenThrowBookingNotFoundException() {

        when(bookingRepository.findById(1)).thenReturn(Optional.empty());

        BookingNotFoundException ex = assertThrows(BookingNotFoundException.class,
                () -> bookingService.approveOrRejectBooking(1, 1, true));

        assertThat(ex.getMessage(), equalTo("Бронирование [1] не найдено"));
        verify(bookingRepository, times(0)).save(any());
    }

    @Test
    void whenApproveBookingThatAlreadyApproved_thenThrowShareitInvalidArgumentException() {

        Booking bookingApproved = new Booking(1, START, END, ITEM, BOOKER, BookingStatus.APPROVED);

        when(bookingRepository.findById(1)).thenReturn(Optional.of(bookingApproved));

        ShareitInvalidArgumentException ex = assertThrows(ShareitInvalidArgumentException.class,
                () -> bookingService.approveOrRejectBooking(1, 1, true));

        assertThat(ex.getMessage(), equalTo("Нельзя изменить статус бронирования после подтверждения"));
        verify(bookingRepository, times(0)).save(any());
    }

    @Test
    void whenApproveBookingWithNotItemOwner_thenThrowShareitInvalidArgumentException() {

        when(bookingRepository.findById(1)).thenReturn(Optional.of(BOOKING_1));

        ShareitInvalidArgumentException ex = assertThrows(ShareitInvalidArgumentException.class,
                () -> bookingService.approveOrRejectBooking(1, 3, true));

        assertThat(ex.getMessage(), equalTo("У пользователя 3 нет доступа к изменению статуса бронирования 1"));
        verify(bookingRepository, times(0)).save(any());
    }

    @Test
    void whenApproveBookingWithBooker_thenThrowShareitNotFoundException() {

        when(bookingRepository.findById(1)).thenReturn(Optional.of(BOOKING_1));

        ShareitNotFoundException ex = assertThrows(ShareitNotFoundException.class,
                () -> bookingService.approveOrRejectBooking(1, 2, true));

        assertThat(ex.getMessage(), equalTo("Пользователь 2 не может изменить статус бронирования"));
        verify(bookingRepository, times(0)).save(any());
    }

    @Test
    void whenFindBooking_thenReturnBooking() {

        when(bookingRepository.findByBookingIdAndUserId(1, 2)).thenReturn(Optional.of(BOOKING_1));
        when(mapper.toBookingResponseDto(BOOKING_1)).thenReturn(BOOKING_RESPONSE_1_DTO);

        BookingResponseDto actual = bookingService.findBooking(1, 2);
        assertThat(actual, equalTo(BOOKING_RESPONSE_1_DTO));
    }

    @Test
    void whenFindBookingNotFound_thenThrowBookingNotFoundException() {

        when(bookingRepository.findByBookingIdAndUserId(1, 2)).thenReturn(Optional.empty());

        BookingNotFoundException ex = assertThrows(BookingNotFoundException.class,
                () -> bookingService.findBooking(1, 2));

        assertThat(ex.getMessage(), equalTo("Бронирование [1] не найдено"));
    }

    @Test
    void whenFindUserBookingsWithNotFoundUser_thenThrowUserNotFoundException() {

        when(userRepository.findById(2)).thenReturn(Optional.empty());

        UserNotFoundException ex = assertThrows(UserNotFoundException.class,
                () -> bookingService.findUserBookings(2, BookingState.ALL, 0, 5));

        assertThat(ex.getMessage(), equalTo("Пользователь [2] не найден"));
    }

    @Test
    void whenFindUserBookingsAll_thenFindAllByBookerIdOrderByStartDesc() {
        Page<Booking> bookings = new PageImpl<>(List.of(BOOKING_1));
        List<BookingResponseDto> expected = List.of(BOOKING_RESPONSE_1_DTO);

        when(userRepository.findById(2)).thenReturn(Optional.of(BOOKER));
        when(bookingRepository.findAllByBookerIdOrderByStartDesc(2, new OffsetPageRequest(0, 5,
                Sort.by("start").descending()))).thenReturn(bookings);
        when(mapper.toBookingResponseDto(BOOKING_1)).thenReturn(BOOKING_RESPONSE_1_DTO);

        List<BookingResponseDto> actual = bookingService.findUserBookings(2, BookingState.ALL, 0, 5);
        verify(bookingRepository, times(1)).findAllByBookerIdOrderByStartDesc(2, new OffsetPageRequest(0, 5,
                Sort.by("start").descending()));
        assertThat(actual, equalTo(expected));
    }

    @Test
    void whenFindUserBookingsCurrent_thenFindAllByBookerIdAndCurrent() {
        Page<Booking> bookings = new PageImpl<>(List.of(BOOKING_1));
        List<BookingResponseDto> expected = List.of(BOOKING_RESPONSE_1_DTO);

        when(userRepository.findById(2)).thenReturn(Optional.of(BOOKER));
        when(bookingRepository.findAllByBookerIdAndCurrent(eq(2), any(LocalDateTime.class),
                eq(new OffsetPageRequest(0, 5)))).thenReturn(bookings);
        when(mapper.toBookingResponseDto(BOOKING_1)).thenReturn(BOOKING_RESPONSE_1_DTO);

        List<BookingResponseDto> actual = bookingService.findUserBookings(2, BookingState.CURRENT, 0, 5);
        verify(bookingRepository, times(1)).findAllByBookerIdAndCurrent(eq(2), any(LocalDateTime.class),
                eq(new OffsetPageRequest(0, 5)));
        assertThat(actual, equalTo(expected));
    }

    @Test
    void whenFindUserBookingsPast_thenFindAllByBookerIdAndEndBeforeOrderByStartDesc() {
        Page<Booking> bookings = new PageImpl<>(List.of(BOOKING_1));
        List<BookingResponseDto> expected = List.of(BOOKING_RESPONSE_1_DTO);

        when(userRepository.findById(2)).thenReturn(Optional.of(BOOKER));
        when(bookingRepository.findAllByBookerIdAndEndBeforeOrderByStartDesc(eq(2), any(LocalDateTime.class),
                eq(new OffsetPageRequest(0, 5)))).thenReturn(bookings);
        when(mapper.toBookingResponseDto(BOOKING_1)).thenReturn(BOOKING_RESPONSE_1_DTO);

        List<BookingResponseDto> actual = bookingService.findUserBookings(2, BookingState.PAST, 0, 5);
        verify(bookingRepository, times(1)).findAllByBookerIdAndEndBeforeOrderByStartDesc(eq(2),
                any(LocalDateTime.class), eq(new OffsetPageRequest(0, 5)));
        assertThat(actual, equalTo(expected));
    }

    @Test
    void whenFindUserBookingsFuture_thenFindAllByBookerIdAndStartAfterOrderByStartDesc() {
        Page<Booking> bookings = new PageImpl<>(List.of(BOOKING_1));
        List<BookingResponseDto> expected = List.of(BOOKING_RESPONSE_1_DTO);

        when(userRepository.findById(2)).thenReturn(Optional.of(BOOKER));
        when(bookingRepository.findAllByBookerIdAndStartAfterOrderByStartDesc(eq(2), any(LocalDateTime.class),
                eq(new OffsetPageRequest(0, 5)))).thenReturn(bookings);
        when(mapper.toBookingResponseDto(BOOKING_1)).thenReturn(BOOKING_RESPONSE_1_DTO);

        List<BookingResponseDto> actual = bookingService.findUserBookings(2, BookingState.FUTURE, 0, 5);
        verify(bookingRepository, times(1)).findAllByBookerIdAndStartAfterOrderByStartDesc(eq(2),
                any(LocalDateTime.class), eq(new OffsetPageRequest(0, 5)));
        assertThat(actual, equalTo(expected));
    }

    @Test
    void whenFindUserBookingsWaiting_thenFindByBookerIdAndStatus() {
        Page<Booking> bookings = new PageImpl<>(List.of(BOOKING_1));
        List<BookingResponseDto> expected = List.of(BOOKING_RESPONSE_1_DTO);

        when(userRepository.findById(2)).thenReturn(Optional.of(BOOKER));
        when(bookingRepository.findByBookerIdAndStatus(2, BookingStatus.WAITING,
                new OffsetPageRequest(0, 5))).thenReturn(bookings);
        when(mapper.toBookingResponseDto(BOOKING_1)).thenReturn(BOOKING_RESPONSE_1_DTO);

        List<BookingResponseDto> actual = bookingService.findUserBookings(2, BookingState.WAITING, 0, 5);
        verify(bookingRepository, times(1)).findByBookerIdAndStatus(2, BookingStatus.WAITING,
                new OffsetPageRequest(0, 5));
        assertThat(actual, equalTo(expected));
    }

    @Test
    void whenFindUserBookingsRejected_thenFindByBookerIdAndStatus() {
        Page<Booking> bookings = new PageImpl<>(List.of(BOOKING_1));
        List<BookingResponseDto> expected = List.of(BOOKING_RESPONSE_1_DTO);

        when(userRepository.findById(2)).thenReturn(Optional.of(BOOKER));
        when(bookingRepository.findByBookerIdAndStatus(2, BookingStatus.REJECTED,
                new OffsetPageRequest(0, 5))).thenReturn(bookings);
        when(mapper.toBookingResponseDto(BOOKING_1)).thenReturn(BOOKING_RESPONSE_1_DTO);

        List<BookingResponseDto> actual = bookingService.findUserBookings(2, BookingState.REJECTED, 0, 5);
        verify(bookingRepository, times(1)).findByBookerIdAndStatus(2, BookingStatus.REJECTED,
                new OffsetPageRequest(0, 5));
        assertThat(actual, equalTo(expected));
    }

    @Test
    void whenFindOwnerItemsBookingsWithNotFoundUser_thenThrowUserNotFoundException() {

        when(userRepository.findById(1)).thenReturn(Optional.empty());

        UserNotFoundException ex = assertThrows(UserNotFoundException.class,
                () -> bookingService.findOwnerItemsBookings(1, BookingState.ALL, 0, 5));

        assertThat(ex.getMessage(), equalTo("Пользователь [1] не найден"));
    }

    @Test
    void whenFindOwnerItemsBookingsAll_thenFindAllByOwnerItems() {
        Page<Booking> bookings = new PageImpl<>(List.of(BOOKING_1));
        List<BookingResponseDto> expected = List.of(BOOKING_RESPONSE_1_DTO);

        when(userRepository.findById(1)).thenReturn(Optional.of(OWNER));
        when(bookingRepository.findAllByOwnerItems(1, new OffsetPageRequest(0, 5))).thenReturn(bookings);
        when(mapper.toBookingResponseDto(BOOKING_1)).thenReturn(BOOKING_RESPONSE_1_DTO);

        List<BookingResponseDto> actual = bookingService.findOwnerItemsBookings(1, BookingState.ALL, 0, 5);
        verify(bookingRepository, times(1)).findAllByOwnerItems(1, new OffsetPageRequest(0, 5));
        assertThat(actual, equalTo(expected));
    }

    @Test
    void whenFindOwnerItemsBookingsCurrent_thenFindAllByOwnerItemsAndCurrent() {
        Page<Booking> bookings = new PageImpl<>(List.of(BOOKING_1));
        List<BookingResponseDto> expected = List.of(BOOKING_RESPONSE_1_DTO);

        when(userRepository.findById(1)).thenReturn(Optional.of(OWNER));
        when(bookingRepository.findAllByOwnerItemsAndCurrent(eq(1), any(LocalDateTime.class),
                eq(new OffsetPageRequest(0, 5))))
                .thenReturn(bookings);
        when(mapper.toBookingResponseDto(BOOKING_1)).thenReturn(BOOKING_RESPONSE_1_DTO);

        List<BookingResponseDto> actual = bookingService.findOwnerItemsBookings(1, BookingState.CURRENT, 0, 5);
        verify(bookingRepository, times(1)).findAllByOwnerItemsAndCurrent(eq(1), any(LocalDateTime.class),
                eq(new OffsetPageRequest(0, 5)));
        assertThat(actual, equalTo(expected));
    }

    @Test
    void whenFindOwnerItemsBookingsPast_thenFindAllByOwnerItemsAndPast() {
        Page<Booking> bookings = new PageImpl<>(List.of(BOOKING_1));
        List<BookingResponseDto> expected = List.of(BOOKING_RESPONSE_1_DTO);

        when(userRepository.findById(1)).thenReturn(Optional.of(OWNER));
        when(bookingRepository.findAllByOwnerItemsAndPast(eq(1), any(LocalDateTime.class),
                eq(new OffsetPageRequest(0, 5)))).thenReturn(bookings);
        when(mapper.toBookingResponseDto(BOOKING_1)).thenReturn(BOOKING_RESPONSE_1_DTO);

        List<BookingResponseDto> actual = bookingService.findOwnerItemsBookings(1, BookingState.PAST, 0, 5);
        verify(bookingRepository, times(1)).findAllByOwnerItemsAndPast(eq(1), any(LocalDateTime.class),
                eq(new OffsetPageRequest(0, 5)));
        assertThat(actual, equalTo(expected));
    }

    @Test
    void whenFindOwnerItemsBookingsFuture_thenFindAllByOwnerItemsAndFuture() {
        Page<Booking> bookings = new PageImpl<>(List.of(BOOKING_1));
        List<BookingResponseDto> expected = List.of(BOOKING_RESPONSE_1_DTO);

        when(userRepository.findById(1)).thenReturn(Optional.of(OWNER));
        when(bookingRepository.findAllByOwnerItemsAndFuture(eq(1), any(LocalDateTime.class),
                eq(new OffsetPageRequest(0, 5))))
                .thenReturn(bookings);
        when(mapper.toBookingResponseDto(BOOKING_1)).thenReturn(BOOKING_RESPONSE_1_DTO);

        List<BookingResponseDto> actual = bookingService.findOwnerItemsBookings(1, BookingState.FUTURE, 0, 5);
        verify(bookingRepository, times(1)).findAllByOwnerItemsAndFuture(eq(1), any(LocalDateTime.class),
                eq(new OffsetPageRequest(0, 5)));
        assertThat(actual, equalTo(expected));
    }

    @Test
    void whenFindOwnerItemsBookingsWaiting_thenFindAllByOwnerItemsAndStatus() {
        Page<Booking> bookings = new PageImpl<>(List.of(BOOKING_1));
        List<BookingResponseDto> expected = List.of(BOOKING_RESPONSE_1_DTO);

        when(userRepository.findById(1)).thenReturn(Optional.of(OWNER));
        when(bookingRepository.findAllByOwnerItemsAndStatus(1, BookingStatus.WAITING, new OffsetPageRequest(0, 5)))
                .thenReturn(bookings);
        when(mapper.toBookingResponseDto(BOOKING_1)).thenReturn(BOOKING_RESPONSE_1_DTO);

        List<BookingResponseDto> actual = bookingService.findOwnerItemsBookings(1, BookingState.WAITING, 0, 5);
        verify(bookingRepository, times(1)).findAllByOwnerItemsAndStatus(1, BookingStatus.WAITING,
                new OffsetPageRequest(0,
                        5));
        assertThat(actual, equalTo(expected));
    }

    @Test
    void whenFindOwnerItemsBookingsRejected_thenFindAllByOwnerItemsAndStatus() {
        Page<Booking> bookings = new PageImpl<>(List.of(BOOKING_1));
        List<BookingResponseDto> expected = List.of(BOOKING_RESPONSE_1_DTO);

        when(userRepository.findById(1)).thenReturn(Optional.of(OWNER));
        when(bookingRepository.findAllByOwnerItemsAndStatus(1, BookingStatus.REJECTED, new OffsetPageRequest(0, 5)))
                .thenReturn(bookings);
        when(mapper.toBookingResponseDto(BOOKING_1)).thenReturn(BOOKING_RESPONSE_1_DTO);

        List<BookingResponseDto> actual = bookingService.findOwnerItemsBookings(1, BookingState.REJECTED, 0, 5);
        verify(bookingRepository, times(1)).findAllByOwnerItemsAndStatus(1, BookingStatus.REJECTED,
                new OffsetPageRequest(0,
                        5));
        assertThat(actual, equalTo(expected));
    }
}

