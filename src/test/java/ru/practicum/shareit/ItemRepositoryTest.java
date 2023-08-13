package ru.practicum.shareit;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.pageable.OffsetPageRequest;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static ru.practicum.shareit.item.ItemRepository.ItemWithBooking;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ItemRepositoryTest {

    public static final User USER_1 = new User(1, "user1", "user1@mail.ru");
    public static final User USER_2 = new User(2, "user2", "user2@mail.ru");
    public static final ItemRequest REQUEST_1 = new ItemRequest(1, "plate for soup", USER_2,
            LocalDateTime.now().minusDays(1), Collections.emptyList());
    public static final Item ITEM_2 = new Item(2, "plate", "for soup", true, USER_1, REQUEST_1);
    public static final Item ITEM_1 = new Item(1, "knife", "for vegetables", true, USER_1, null);
    private static final LocalDateTime START_1 = LocalDateTime.of(2023, Month.JULY, 1, 0, 0);
    private static final LocalDateTime END_1 = LocalDateTime.of(2023, Month.JULY, 5, 0, 0);
    public static final Booking BOOKING_1 = new Booking(1, START_1, END_1, ITEM_1, USER_2, BookingStatus.APPROVED);
    private static final LocalDateTime START_2 = LocalDateTime.of(2023, Month.AUGUST, 1, 0, 0);
    private static final LocalDateTime END_2 = LocalDateTime.of(2023, Month.AUGUST, 5, 0, 0);
    public static final Booking BOOKING_2_SHOULD_BE_LAST = new Booking(2, START_2, END_2, ITEM_1, USER_2,
            BookingStatus.APPROVED);
    public static final Booking BOOKING_2_SHOULD_BE_LAST_REJECTED = new Booking(2, START_2, END_2, ITEM_1, USER_2,
            BookingStatus.REJECTED);
    private static final LocalDateTime START_3 = LocalDateTime.of(2023, Month.SEPTEMBER, 1, 0, 0);
    private static final LocalDateTime END_3 = LocalDateTime.of(2023, Month.SEPTEMBER, 5, 0, 0);
    public static final Booking BOOKING_3_SHOULD_BE_NEXT = new Booking(3, START_3, END_3, ITEM_1, USER_2,
            BookingStatus.WAITING);
    public static final Booking BOOKING_3_SHOULD_BE_NEXT_REJECTED = new Booking(3, START_3, END_3, ITEM_1, USER_2,
            BookingStatus.REJECTED);
    private static final LocalDateTime START_4 = LocalDateTime.of(2023, Month.NOVEMBER, 1, 0, 0);
    private static final LocalDateTime END_4 = LocalDateTime.of(2023, Month.NOVEMBER, 1, 0, 0);
    public static final Booking BOOKING_4 = new Booking(4, START_4, END_4, ITEM_1, USER_2, BookingStatus.WAITING);
    @Autowired
    private TestEntityManager testEntityManager;
    @Autowired
    private ItemRepository itemRepository;

    @Test
    void shouldFindAllByText_withOnlyPartOfWord() {
        testEntityManager.merge(USER_1);
        testEntityManager.merge(USER_2);
        testEntityManager.merge(REQUEST_1);
        testEntityManager.merge(ITEM_1);
        testEntityManager.merge(ITEM_2);

        List<Item> items = itemRepository.findAllByText("kni", new OffsetPageRequest(0, 5)).getContent();

        assertThat(items, hasSize(1));
        assertThat(items.get(0), equalTo(ITEM_1));
    }

    @Test
    void shouldFindAllByText_withIgnoringCase() {
        testEntityManager.merge(USER_1);
        testEntityManager.merge(USER_2);
        testEntityManager.merge(REQUEST_1);
        testEntityManager.merge(ITEM_1);
        testEntityManager.merge(ITEM_2);

        List<Item> items = itemRepository.findAllByText("KNIFE", new OffsetPageRequest(0, 5)).getContent();

        assertThat(items, hasSize(1));
        assertThat(items.get(0), equalTo(ITEM_1));
    }

    @Test
    void shouldNotFindByText() {
        testEntityManager.merge(USER_1);
        testEntityManager.merge(USER_2);
        testEntityManager.merge(ITEM_1);

        List<Item> items = itemRepository.findAllByText("plate", new OffsetPageRequest(0, 5)).getContent();

        assertThat(items, equalTo(Collections.emptyList()));
    }

    @Test
    void shouldFindItemsWithBookingsByOwnerId() {
        int ownerId = 1;
        int lastBookingId = 2;
        int nextBookingId = 3;
        testEntityManager.merge(USER_1);
        testEntityManager.merge(USER_2);
        testEntityManager.merge(ITEM_1);
        testEntityManager.merge(BOOKING_1);
        testEntityManager.merge(BOOKING_2_SHOULD_BE_LAST);
        testEntityManager.merge(BOOKING_3_SHOULD_BE_NEXT);
        testEntityManager.merge(BOOKING_4);

        List<ItemWithBooking> items = itemRepository.findItemsWithBookingsByOwnerId(ownerId, LocalDateTime.now(),
                new OffsetPageRequest(0, 5)).getContent();

        assertThat(items, hasSize(1));
        assertThat(items.get(0).getLastBooking(), equalTo(lastBookingId));
        assertThat(items.get(0).getNextBooking(), equalTo(nextBookingId));
    }

    @Test
    void shouldFindItemsWithOnlyLastBookingByOwnerId() {
        int ownerId = 1;
        int lastBookingId = 2;
        testEntityManager.merge(USER_1);
        testEntityManager.merge(USER_2);
        testEntityManager.merge(ITEM_1);
        testEntityManager.merge(BOOKING_1);
        testEntityManager.merge(BOOKING_2_SHOULD_BE_LAST);

        List<ItemWithBooking> items = itemRepository.findItemsWithBookingsByOwnerId(ownerId, LocalDateTime.now(),
                new OffsetPageRequest(0, 5)).getContent();

        assertThat(items, hasSize(1));
        assertThat(items.get(0).getLastBooking(), equalTo(lastBookingId));
        assertThat(items.get(0).getNextBooking(), equalTo(null));
    }

    @Test
    void shouldFindItemWithBookingsByItemId() {
        int itemId = 1;
        int lastBookingId = 2;
        int nextBookingId = 3;
        testEntityManager.merge(USER_1);
        testEntityManager.merge(USER_2);
        testEntityManager.merge(ITEM_1);
        testEntityManager.merge(BOOKING_1);
        testEntityManager.merge(BOOKING_2_SHOULD_BE_LAST);
        testEntityManager.merge(BOOKING_3_SHOULD_BE_NEXT);
        testEntityManager.merge(BOOKING_4);

        ItemWithBooking item = itemRepository.findItemWithBookingsByItemId(itemId, LocalDateTime.now());

        assertThat(item.getLastBooking(), equalTo(lastBookingId));
        assertThat(item.getNextBooking(), equalTo(nextBookingId));
    }

    @Test
    void shouldFindItemWithBookingsNotRejectedByItemId() {
        int itemId = 1;
        int lastBookingId = 1;
        int nextBookingId = 4;
        testEntityManager.merge(USER_1);
        testEntityManager.merge(USER_2);
        testEntityManager.merge(ITEM_1);
        testEntityManager.merge(BOOKING_1);
        testEntityManager.merge(BOOKING_2_SHOULD_BE_LAST_REJECTED);
        testEntityManager.merge(BOOKING_3_SHOULD_BE_NEXT_REJECTED);
        testEntityManager.merge(BOOKING_4);

        ItemWithBooking item = itemRepository.findItemWithBookingsByItemId(itemId, LocalDateTime.now());

        assertThat(item.getLastBooking(), equalTo(lastBookingId));
        assertThat(item.getNextBooking(), equalTo(nextBookingId));
    }
}
