package ru.practicum.shareit;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import javax.persistence.TypedQuery;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingRequestDto;
import ru.practicum.shareit.booking.BookingResponseDto;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemDto;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.UserRepository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureTestEntityManager
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BookingServiceIT {

    public static final User OWNER = new User(1, "user1", "user1@mail.ru");
    public static final User BOOKER = new User(2, "user2", "user2@mail.ru");
    public static final UserDto BOOKER_DTO = new UserDto(2, "user2", "user2@mail.ru");
    public static final Item ITEM = new Item(1, "knife", "for vegetables", true, OWNER, null);
    public static final ItemDto ITEM_DTO = new ItemDto(1, "knife", "for vegetables", true, null);
    private static final LocalDateTime START = LocalDateTime.of(2023, Month.AUGUST,  1, 0, 0);
    private static final LocalDateTime END = LocalDateTime.of(2023, Month.SEPTEMBER,  1, 0, 0);
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
    private final TestEntityManager em;
    private final BookingService bookingService;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @Test
    void addBooking() {
        int bookerId = 2;

        userRepository.save(OWNER);
        userRepository.save(BOOKER);
        itemRepository.save(ITEM);
        bookingService.addBooking(BOOKING_REQUEST_1_DTO, bookerId);

        TypedQuery<Booking> query = em.getEntityManager().createQuery("Select b from Booking b join b.booker as " +
                "booker where booker.id = :bookerId", Booking.class);
        Booking booking = query.setParameter("bookerId", bookerId)
                .getSingleResult();

        assertThat(booking.getId(), equalTo(1));
        assertThat(booking.getStart(), equalTo(BOOKING_REQUEST_1_DTO.getStart()));
        assertThat(booking.getEnd(), equalTo(BOOKING_REQUEST_1_DTO.getEnd()));
        assertThat(booking.getItem(), equalTo(ITEM));
        assertThat(booking.getBooker(), equalTo(BOOKER));
        assertThat(booking.getStatus(), equalTo(BookingStatus.WAITING));
    }

    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @Test
    void approveOrRejectBooking() {
        int ownerId = 1;
        int bookingId = 1;

        userRepository.save(OWNER);
        userRepository.save(BOOKER);
        itemRepository.save(ITEM);
        bookingRepository.save(BOOKING_1);
        bookingService.approveOrRejectBooking(bookingId, ownerId, true);

        TypedQuery<Booking> query = em.getEntityManager().createQuery("Select b from Booking b " +
                " where b.id = :bookingId", Booking.class);
        Booking booking = query.setParameter("bookingId", bookingId)
                .getSingleResult();

        assertThat(booking.getId(), equalTo(bookingId));
        assertThat(booking.getStart(), equalTo(BOOKING_1.getStart()));
        assertThat(booking.getEnd(), equalTo(BOOKING_1.getEnd()));
        assertThat(booking.getItem(), equalTo(ITEM));
        assertThat(booking.getBooker(), equalTo(BOOKER));
        assertThat(booking.getStatus(), equalTo(BookingStatus.APPROVED));
    }

    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @Test
    void findBooking() {
        int ownerId = 1;
        int bookingId = 1;

        userRepository.save(OWNER);
        userRepository.save(BOOKER);
        itemRepository.save(ITEM);
        bookingRepository.save(BOOKING_1);
        BookingResponseDto booking = bookingService.findBooking(bookingId, ownerId);

        assertThat(booking.getId(), equalTo(bookingId));
        assertThat(booking.getStart(), equalTo(BOOKING_1.getStart()));
        assertThat(booking.getEnd(), equalTo(BOOKING_1.getEnd()));
        assertThat(booking.getItem().getId(), equalTo(ITEM.getId()));
        assertThat(booking.getBooker().getId(), equalTo(BOOKER.getId()));
        assertThat(booking.getStatus(), equalTo(BookingStatus.WAITING));
    }

    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @Test
    void findUserBookings() {
        int bookerId = 2;
        int bookingId = 1;

        userRepository.save(OWNER);
        userRepository.save(BOOKER);
        itemRepository.save(ITEM);
        bookingRepository.save(BOOKING_1);

        List<BookingResponseDto> bookings = bookingService.findUserBookings(bookerId, BookingState.WAITING, 0, 5);

        assertThat(bookings.size(), equalTo(1));
        assertThat(bookings.get(0).getId(), equalTo(bookingId));
        assertThat(bookings.get(0).getStatus(), equalTo(BookingStatus.WAITING));
    }

    @DirtiesContext(methodMode = DirtiesContext.MethodMode.AFTER_METHOD)
    @Test
    void findOwnerItemsBookings() {
        int ownerId = 1;
        int bookingId = 1;

        userRepository.save(OWNER);
        userRepository.save(BOOKER);
        itemRepository.save(ITEM);
        bookingRepository.save(BOOKING_1);
        bookingService.approveOrRejectBooking(bookingId, ownerId, false);

        List<BookingResponseDto> bookings = bookingService.findOwnerItemsBookings(ownerId, BookingState.REJECTED, 0, 5);

        assertThat(bookings.size(), equalTo(1));
        assertThat(bookings.get(0).getId(), equalTo(bookingId));
        assertThat(bookings.get(0).getStatus(), equalTo(BookingStatus.REJECTED));
    }
}
