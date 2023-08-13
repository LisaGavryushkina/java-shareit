package ru.practicum.shareit;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.item.Comment;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class EntityTest {

    public static final User USER_1 = new User(1, "user1", "user1@mail.ru");
    public static final User USER_2 = new User(2, "user2", "user2@mail.ru");
    public static final Item ITEM_1 = new Item(1, "knife", "for vegetables", true, USER_1, null);
    public static final Item ITEM_2 = new Item(2, "plate", "for soup", true, USER_2, null);
    private static final LocalDateTime START = LocalDateTime.of(2023, Month.AUGUST, 1, 0, 0);
    private static final LocalDateTime END = LocalDateTime.of(2023, Month.SEPTEMBER, 1, 0, 0);

    @Test
    void testBookingEquals() {
        Booking booking1 = new Booking(1, START, END, ITEM_1, USER_1, BookingStatus.WAITING);
        Booking booking3 = new Booking(1, END, START, ITEM_2, USER_1, BookingStatus.WAITING);

        assertThat(booking1.equals(booking1), equalTo(true));
        assertThat(booking1.equals(booking3), equalTo(true));
        assertThat(booking3.equals(booking1), equalTo(true));
    }

    @Test
    void testBookingNotEquals() {
        Booking booking1 = new Booking(1, START, END, ITEM_1, USER_1, BookingStatus.WAITING);
        Booking booking2 = new Booking(2, START, END, ITEM_1, USER_1, BookingStatus.WAITING);

        assertThat(booking1.equals(booking2), equalTo(false));
        assertThat(booking2.equals(booking1), equalTo(false));
        assertThat(booking2.equals(USER_1), equalTo(false));
    }

    @Test
    void testCommentEquals() {
        Comment comment1 = new Comment(1, "puk", ITEM_1, USER_2, START);
        Comment comment3 = new Comment(1, "kek", ITEM_2, USER_1, END);

        assertThat(comment1.equals(comment1), equalTo(true));
        assertThat(comment1.equals(comment3), equalTo(true));
        assertThat(comment3.equals(comment1), equalTo(true));
    }

    @Test
    void testCommentNotEquals() {
        Comment comment1 = new Comment(1, "puk", ITEM_1, USER_2, START);
        Comment comment2 = new Comment(2, "puk", ITEM_1, USER_2, START);

        assertThat(comment1.equals(comment2), equalTo(false));
        assertThat(comment2.equals(comment1), equalTo(false));
        assertThat(comment2.equals(USER_1), equalTo(false));
    }

    @Test
    void testItemEquals() {
        Item item1 = new Item(1, "knife", "to kill", true, USER_1, null);
        Item item3 = new Item(1, "plate", "for soup", true, USER_2, null);

        assertThat(item1.equals(item1), equalTo(true));
        assertThat(item1.equals(item3), equalTo(true));
        assertThat(item3.equals(item1), equalTo(true));
    }

    @Test
    void testItemNotEquals() {
        Item item1 = new Item(1, "knife", "to kill", true, USER_1, null);
        Item item2 = new Item(2, "knife", "to kill", true, USER_1, null);

        assertThat(item1.equals(item2), equalTo(false));
        assertThat(item2.equals(item1), equalTo(false));
        assertThat(item2.equals(USER_1), equalTo(false));
    }

    @Test
    void testItemRequestEquals() {
        ItemRequest request1 = new ItemRequest(1, "looking for knife", USER_1, START, Collections.emptyList());
        ItemRequest request3 = new ItemRequest(1, "plate", USER_2, END, Collections.emptyList());

        assertThat(request1.equals(request1), equalTo(true));
        assertThat(request1.equals(request3), equalTo(true));
        assertThat(request3.equals(request1), equalTo(true));
    }

    @Test
    void testItemRequestNotEquals() {
        ItemRequest request1 = new ItemRequest(1, "looking for knife", USER_1, START, Collections.emptyList());
        ItemRequest request2 = new ItemRequest(2, "looking for knife", USER_1, START, Collections.emptyList());

        assertThat(request1.equals(request2), equalTo(false));
        assertThat(request2.equals(request1), equalTo(false));
        assertThat(request2.equals(ITEM_1), equalTo(false));
    }

    @Test
    void testUserEquals() {
        User user1 = new User(1, "user", "user@mail.ru");
        User user3 = new User(1, "user3", "user3@mail.ru");

        assertThat(user1.equals(user1), equalTo(true));
        assertThat(user1.equals(user3), equalTo(true));
        assertThat(user3.equals(user1), equalTo(true));
    }

    @Test
    void testUserNotEquals() {
        User user1 = new User(1, "user", "user@mail.ru");
        User user2 = new User(2, "user", "user@mail.ru");

        assertThat(user1.equals(user2), equalTo(false));
        assertThat(user2.equals(user1), equalTo(false));
        assertThat(user2.equals(ITEM_1), equalTo(false));
    }

}
