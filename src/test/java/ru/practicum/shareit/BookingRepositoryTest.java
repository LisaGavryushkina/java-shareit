package ru.practicum.shareit;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.pageable.OffsetPageRequest;
import ru.practicum.shareit.user.User;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInRelativeOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class BookingRepositoryTest {

    public static final User OWNER = new User(1, "user1", "user1@mail.ru");
    public static final User BOOKER = new User(2, "user2", "user2@mail.ru");
    public static final Item ITEM_1 = new Item(1, "knife", "for vegetables", true, OWNER, null);
    public static final Booking BOOKING_IN_FUTURE_SHOULD_BE_FIRST = new Booking(1, LocalDateTime.now().plusDays(2),
            LocalDateTime.now().plusDays(3), ITEM_1, BOOKER, BookingStatus.WAITING);
    public static final Booking BOOKING_IN_PAST_SHOULD_BE_FIRST = new Booking(1, LocalDateTime.now().minusDays(3),
            LocalDateTime.now().minusDays(1), ITEM_1, BOOKER, BookingStatus.WAITING);
    public static final Booking BOOKING_IN_CURRENT_SHOULD_BE_FIRST = new Booking(1, LocalDateTime.now().minusDays(1),
            LocalDateTime.now().plusDays(3), ITEM_1, BOOKER, BookingStatus.WAITING);
    public static final Item ITEM_2 = new Item(2, "plate", "for soup", true, OWNER, null);
    private static final Booking BOOKING_IN_FUTURE_SHOULD_BE_SECOND = new Booking(2, LocalDateTime.now().plusDays(1),
            LocalDateTime.now().plusDays(3), ITEM_2, BOOKER, BookingStatus.WAITING);
    public static final Booking BOOKING_IN_FUTURE_SHOULD_NOT_BE_SELECTED = new Booking(3,
            LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(4), ITEM_2, BOOKER, BookingStatus.REJECTED);
    public static final Booking BOOKING_IN_PAST_SHOULD_BE_SECOND = new Booking(2, LocalDateTime.now().minusDays(6),
            LocalDateTime.now().minusDays(3), ITEM_2, BOOKER, BookingStatus.WAITING);
    public static final Booking BOOKING_IN_PAST_SHOULD_NOT_BE_SELECTED = new Booking(3,
            LocalDateTime.now().minusDays(4), LocalDateTime.now().minusDays(2), ITEM_2, BOOKER, BookingStatus.REJECTED);
    public static final Booking BOOKING_IN_CURRENT_SHOULD_BE_SECOND = new Booking(2, LocalDateTime.now().minusDays(3),
            LocalDateTime.now().plusDays(3), ITEM_2, BOOKER, BookingStatus.WAITING);
    private static final LocalDateTime START = LocalDateTime.of(2023, Month.AUGUST, 1, 0, 0);
    private static final LocalDateTime END = LocalDateTime.of(2023, Month.SEPTEMBER, 1, 0, 0);
    public static final Booking BOOKING = new Booking(1, START, END, ITEM_1, BOOKER, BookingStatus.WAITING);
    public static final Booking BOOKING_WAITING = new Booking(1, START, END, ITEM_1, BOOKER, BookingStatus.WAITING);
    public static final Booking BOOKING_REJECTED = new Booking(2, START, END, ITEM_2, BOOKER, BookingStatus.REJECTED);
    @Autowired
    private TestEntityManager testEntityManager;
    @Autowired
    private BookingRepository bookingRepository;

    @BeforeEach
    public void setup() {
        testEntityManager.merge(OWNER);
        testEntityManager.merge(BOOKER);
        testEntityManager.merge(ITEM_1);
        testEntityManager.merge(ITEM_2);
    }

    @Test
    void shouldFindByBookingIdAndOwnerId() {
        testEntityManager.merge(BOOKING);

        Optional<Booking> actual = bookingRepository.findByBookingIdAndUserId(BOOKING.getId(), OWNER.getId());
        Optional<Booking> expected = Optional.of(BOOKING);
        assertThat(actual, equalTo(expected));
    }

    @Test
    void shouldFindByBookingIdAndBookerId() {
        testEntityManager.merge(BOOKING);

        Optional<Booking> actual = bookingRepository.findByBookingIdAndUserId(BOOKING.getId(), BOOKER.getId());
        Optional<Booking> expected = Optional.of(BOOKING);
        assertThat(actual, equalTo(expected));
    }

    @Test
    void shouldNotFindByBookingIdAndOtherUserId() {
        testEntityManager.merge(BOOKING);
        User otherUser = testEntityManager.merge(new User(0, "otherUser", "other@mail.ru"));

        Optional<Booking> actual = bookingRepository.findByBookingIdAndUserId(BOOKING.getId(), otherUser.getId());
        assertThat(actual, equalTo(Optional.empty()));
    }

    @Test
    void shouldFindByBookerIdAndStatus() {
        testEntityManager.merge(BOOKING_WAITING);
        testEntityManager.merge(BOOKING_REJECTED);

        List<Booking> bookings = bookingRepository.findByBookerIdAndStatus(BOOKER.getId(), BookingStatus.WAITING,
                new OffsetPageRequest(0, 5)).getContent();

        assertThat(bookings, hasSize(1));
        assertThat(bookings, contains(BOOKING_WAITING));
    }

    @Test
    void shouldNotFindByBookerIdAndWrongStatus() {
        testEntityManager.merge(BOOKING_WAITING);

        List<Booking> bookings = bookingRepository.findByBookerIdAndStatus(BOOKER.getId(), BookingStatus.REJECTED,
                new OffsetPageRequest(0, 5)).getContent();

        assertThat(bookings, empty());
    }

    @Test
    void shouldFindAllByBookerIdAndCurrent() {
        testEntityManager.merge(BOOKING_IN_CURRENT_SHOULD_BE_FIRST);
        testEntityManager.merge(BOOKING_IN_PAST_SHOULD_NOT_BE_SELECTED);

        List<Booking> bookings = bookingRepository.findAllByBookerIdAndCurrentOrderByStartDesc(BOOKER.getId(), LocalDateTime.now(),
                new OffsetPageRequest(0, 5)).getContent();

        assertThat(bookings, hasSize(1));
        assertThat(bookings, contains(BOOKING_IN_CURRENT_SHOULD_BE_FIRST));
    }

    @Test
    void shouldFindAllByBookerIdOrderByStartDesc() {
        testEntityManager.merge(BOOKING_IN_FUTURE_SHOULD_BE_FIRST);
        testEntityManager.merge(BOOKING_IN_FUTURE_SHOULD_BE_SECOND);

        List<Booking> bookings = bookingRepository.findAllByBookerIdOrderByStartDesc(BOOKER.getId(),
                new OffsetPageRequest(0, 5)).getContent();

        assertThat(bookings, hasSize(2));
        assertThat(bookings, containsInRelativeOrder(BOOKING_IN_FUTURE_SHOULD_BE_FIRST,
                BOOKING_IN_FUTURE_SHOULD_BE_SECOND));
    }

    @Test
    void shouldFindAllByBookerIdAndInPastOrderByStartDesc() {
        testEntityManager.merge(BOOKING_IN_PAST_SHOULD_BE_FIRST);
        testEntityManager.merge(BOOKING_IN_PAST_SHOULD_BE_SECOND);
        testEntityManager.merge(BOOKING_IN_FUTURE_SHOULD_NOT_BE_SELECTED);

        List<Booking> bookings = bookingRepository.findAllByBookerIdAndEndBeforeOrderByStartDesc(BOOKER.getId(),
                LocalDateTime.now(), new OffsetPageRequest(0, 5)).getContent();

        assertThat(bookings, hasSize(2));
        assertThat(bookings, containsInRelativeOrder(BOOKING_IN_PAST_SHOULD_BE_FIRST,
                BOOKING_IN_PAST_SHOULD_BE_SECOND));
    }

    @Test
    void shouldFindAllByBookerIdAndInFutureOrderByStartDesc() {
        testEntityManager.merge(BOOKING_IN_FUTURE_SHOULD_BE_FIRST);
        testEntityManager.merge(BOOKING_IN_FUTURE_SHOULD_BE_SECOND);
        testEntityManager.merge(BOOKING_IN_PAST_SHOULD_NOT_BE_SELECTED);

        List<Booking> bookings = bookingRepository.findAllByBookerIdAndStartAfterOrderByStartDesc(BOOKER.getId(),
                LocalDateTime.now(), new OffsetPageRequest(0, 5)).getContent();

        assertThat(bookings, hasSize(2));
        assertThat(bookings, containsInRelativeOrder(BOOKING_IN_FUTURE_SHOULD_BE_FIRST,
                BOOKING_IN_FUTURE_SHOULD_BE_SECOND));
    }

    @Test
    void shouldFindAllByOwnerItems() {
        testEntityManager.merge(BOOKING_IN_FUTURE_SHOULD_BE_FIRST);
        testEntityManager.merge(BOOKING_IN_FUTURE_SHOULD_BE_SECOND);

        List<Booking> bookings = bookingRepository.findAllByOwnerItemsOrderByStartDesc(OWNER.getId(),
                new OffsetPageRequest(0, 5)).getContent();

        assertThat(bookings, hasSize(2));
        assertThat(bookings, containsInRelativeOrder(BOOKING_IN_FUTURE_SHOULD_BE_FIRST,
                BOOKING_IN_FUTURE_SHOULD_BE_SECOND));
    }

    @Test
    void shouldFindAllByOwnerItemsAndCurrent() {
        testEntityManager.merge(BOOKING_IN_CURRENT_SHOULD_BE_FIRST);
        testEntityManager.merge(BOOKING_IN_CURRENT_SHOULD_BE_SECOND);
        testEntityManager.merge(BOOKING_IN_PAST_SHOULD_NOT_BE_SELECTED);

        List<Booking> bookings = bookingRepository.findAllByOwnerItemsAndCurrentOrderByStartDesc(OWNER.getId(), LocalDateTime.now(),
                new OffsetPageRequest(0, 5)).getContent();

        assertThat(bookings, hasSize(2));
        assertThat(bookings, containsInRelativeOrder(BOOKING_IN_CURRENT_SHOULD_BE_FIRST,
                BOOKING_IN_CURRENT_SHOULD_BE_SECOND));
    }

    @Test
    void shouldFindAllByOwnerItemsAndPast() {
        testEntityManager.merge(BOOKING_IN_PAST_SHOULD_BE_FIRST);
        testEntityManager.merge(BOOKING_IN_PAST_SHOULD_BE_SECOND);
        testEntityManager.merge(BOOKING_IN_FUTURE_SHOULD_NOT_BE_SELECTED);

        List<Booking> bookings = bookingRepository.findAllByOwnerItemsAndPastOrderByStartDesc(OWNER.getId(), LocalDateTime.now(),
                new OffsetPageRequest(0, 5)).getContent();
        System.out.println(bookings);

        assertThat(bookings, hasSize(2));
        assertThat(bookings, containsInRelativeOrder(BOOKING_IN_PAST_SHOULD_BE_FIRST,
                BOOKING_IN_PAST_SHOULD_BE_SECOND));
    }

    @Test
    void shouldFindAllByOwnerItemsAndFuture() {
        testEntityManager.merge(BOOKING_IN_FUTURE_SHOULD_BE_FIRST);
        testEntityManager.merge(BOOKING_IN_FUTURE_SHOULD_BE_SECOND);
        testEntityManager.merge(BOOKING_IN_PAST_SHOULD_NOT_BE_SELECTED);

        List<Booking> bookings = bookingRepository.findAllByOwnerItemsAndFutureOrderByStartDesc(OWNER.getId(),
                LocalDateTime.now(), new OffsetPageRequest(0, 5)).getContent();

        assertThat(bookings, hasSize(2));
        assertThat(bookings, containsInRelativeOrder(BOOKING_IN_FUTURE_SHOULD_BE_FIRST,
                BOOKING_IN_FUTURE_SHOULD_BE_SECOND));
    }

    @Test
    void shouldFindAllByOwnerItemsAndStatus() {
        testEntityManager.merge(BOOKING_WAITING);
        testEntityManager.merge(BOOKING_REJECTED);

        List<Booking> bookings = bookingRepository.findAllByOwnerItemsAndStatusOrderByStartDesc(OWNER.getId(), BookingStatus.WAITING,
                new OffsetPageRequest(0, 5)).getContent();

        assertThat(bookings, hasSize(1));
        assertThat(bookings, contains(BOOKING_WAITING));
    }

    @Test
    void shouldFindByItemIdAndBookerId() {
        testEntityManager.merge(new Booking(1, START, END, ITEM_1, BOOKER, BookingStatus.APPROVED));

        Optional<Booking> actual = bookingRepository.findByItemIdAndBookerId(ITEM_1.getId(), BOOKER.getId());

        assertThat(actual, equalTo(Optional.of(BOOKING)));
    }
}

