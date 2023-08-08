package ru.practicum.shareit;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Collections;
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
import ru.practicum.shareit.booking.BookingForItemDto;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.item.Comment;
import ru.practicum.shareit.item.CommentDto;
import ru.practicum.shareit.item.CommentRepository;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemDto;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.ItemService;
import ru.practicum.shareit.item.ItemWithBookingAndCommentsDto;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureTestEntityManager
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemServiceIT {

    public static final User USER_1 = new User(1, "user1", "user1@mail.ru");
    public static final User USER_2 = new User(2, "user2", "user2@mail.ru");
    public static final ItemRequest REQUEST_1 = new ItemRequest(1, "plate for soup", USER_2,
            LocalDateTime.now().minusDays(1), Collections.emptyList());
    public static final Item ITEM_2 = new Item(2, "plate", "for soup", true, USER_1, REQUEST_1);
    public static final Item ITEM_1 = new Item(1, "knife", "for vegetables", true, USER_1, null);
    public static final Comment COMMENT = new Comment(1, "comment from user2", ITEM_1, USER_2,
            LocalDateTime.of(2023, Month.AUGUST, 5, 0, 0));
    public static final Item ITEM_1_UPDATED = new Item(1, "knife", "for vegetables", false, USER_1, null);
    public static final ItemDto ITEM_DTO_1 = new ItemDto(1, "knife", "for vegetables", true, null);
    public static final ItemDto ITEM_DTO_1_UPDATE = new ItemDto(1, "knife", "for vegetables", false, null);
    public static final CommentDto COMMENT_DTO = new CommentDto(1, "comment from user2", 1, "user2",
            LocalDateTime.of(2023, Month.AUGUST, 5, 0, 0));
    public static final ItemWithBookingAndCommentsDto ITEM_WITH_BOOKING_AND_COMMENTS_DTO_2 =
            new ItemWithBookingAndCommentsDto(2, "plate", "for soup", true, null,
                    null, null, Collections.emptyList());
    private static final LocalDateTime START_1 = LocalDateTime.now().minusDays(5);
    private static final LocalDateTime END_1 = LocalDateTime.now().minusDays(1);
    public static final Booking BOOKING_1 = new Booking(1, START_1, END_1, ITEM_1, USER_2, BookingStatus.WAITING);
    public static final BookingForItemDto BOOKING_FOR_ITEM_DTO_1 = new BookingForItemDto(1, START_1, END_1, 1, 2,
            BookingStatus.WAITING);
    private static final LocalDateTime START_2 = LocalDateTime.now().plusDays(5);
    private static final LocalDateTime END_2 = LocalDateTime.now().plusDays(7);
    public static final Booking BOOKING_2 = new Booking(2, START_2, END_2, ITEM_1, USER_2, BookingStatus.WAITING);
    public static final BookingForItemDto BOOKING_FOR_ITEM_DTO_2 = new BookingForItemDto(2, START_2, END_2, 1, 2,
            BookingStatus.WAITING);
    public static final ItemWithBookingAndCommentsDto ITEM_WITH_BOOKING_AND_COMMENTS_DTO_1 =
            new ItemWithBookingAndCommentsDto(1, "knife", "for vegetables", true, null,
                    BOOKING_FOR_ITEM_DTO_1, BOOKING_FOR_ITEM_DTO_2, List.of(COMMENT_DTO));
    private final TestEntityManager em;
    private final ItemService itemService;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final ItemRequestRepository itemRequestRepository;
    private final CommentRepository commentRepository;

    @Test
    void addItem() {
        int ownerId = 1;
        userRepository.save(USER_1);
        itemService.addItem(ITEM_DTO_1, ownerId);

        TypedQuery<Item> query = em.getEntityManager().createQuery("Select i from Item i join i.owner as " +
                "owner where owner.id = :ownerId", Item.class);
        Item item = query.setParameter("ownerId", ownerId)
                .getSingleResult();

        assertThat(item, equalTo(ITEM_1));
    }

    @Test
    void updateItem() {
        int ownerId = 1;
        int itemId = 1;
        userRepository.save(USER_1);
        itemRepository.save(ITEM_1);
        itemService.updateItem(itemId, ITEM_DTO_1_UPDATE, ownerId);

        TypedQuery<Item> query = em.getEntityManager().createQuery("Select i from Item i where i.id = :itemId",
                Item.class);
        Item item = query.setParameter("itemId", itemId)
                .getSingleResult();

        assertThat(item, equalTo(ITEM_1_UPDATED));
    }

    @Test
    void getItemById() {
        int ownerId = 1;
        int itemId = 1;
        userRepository.save(USER_1);
        userRepository.save(USER_2);
        itemRepository.save(ITEM_1);
        bookingRepository.save(BOOKING_1);
        bookingRepository.save(BOOKING_2);
        commentRepository.save(COMMENT);

        ItemWithBookingAndCommentsDto item = itemService.getItemById(itemId, ownerId);

        assertThat(item, equalTo(ITEM_WITH_BOOKING_AND_COMMENTS_DTO_1));
    }

    @Test
    void findOwnerItems() {
        int ownerId = 1;
        userRepository.save(USER_1);
        userRepository.save(USER_2);
        itemRequestRepository.save(REQUEST_1);
        itemRepository.save(ITEM_1);
        itemRepository.save(ITEM_2);
        bookingRepository.save(BOOKING_1);
        bookingRepository.save(BOOKING_2);
        commentRepository.save(COMMENT);

        List<ItemWithBookingAndCommentsDto> items = itemService.findOwnerItems(ownerId, 0, 5);

        assertThat(items, hasSize(2));
        assertThat(items.get(0), equalTo(ITEM_WITH_BOOKING_AND_COMMENTS_DTO_1));
        assertThat(items.get(1), equalTo(ITEM_WITH_BOOKING_AND_COMMENTS_DTO_2));
    }

    @Test
    void findItems() {
        userRepository.save(USER_1);
        userRepository.save(USER_2);
        itemRequestRepository.save(REQUEST_1);
        itemRepository.save(ITEM_1);
        itemRepository.save(ITEM_2);

        List<ItemDto> items = itemService.findItems("knife", 0, 5);

        assertThat(items, hasSize(1));
        assertThat(items.get(0), equalTo(ITEM_DTO_1));
    }

    @Test
    void postComment() {
        int itemId = 1;
        userRepository.save(USER_1);
        userRepository.save(USER_2);
        itemRepository.save(ITEM_1);
        bookingRepository.save(BOOKING_1);
        commentRepository.save(COMMENT);

        TypedQuery<Comment> query = em.getEntityManager().createQuery("Select c from Comment c join c.item as item " +
                "where item.id = :itemId", Comment.class);
        Comment comment = query.setParameter("itemId", itemId)
                .getSingleResult();

        assertThat(comment, equalTo(COMMENT));
    }
}
