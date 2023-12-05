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
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestDto;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.ItemRequestService;
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
public class ItemRequestServiceIT {

    public static final User REQUESTOR_1 = new User(1, "user1", "user1@mail.ru");
    public static final User REQUESTOR_2 = new User(2, "user2", "user2@mail.ru");
    public static final ItemRequest REQUEST_1 = new ItemRequest(1, "knife for vegetables", REQUESTOR_1,
            LocalDateTime.of(2023, Month.AUGUST,  1, 0, 0), Collections.emptyList());
    public static final ItemRequest REQUEST_2 = new ItemRequest(2, "plate for soup", REQUESTOR_1,
            LocalDateTime.of(2023, Month.AUGUST,  3, 0, 0), Collections.emptyList());
    public static final ItemRequest REQUEST_3 = new ItemRequest(3, "sunglasses", REQUESTOR_2,
            LocalDateTime.of(2023, Month.AUGUST,  5, 0, 0), Collections.emptyList());
    public static final ItemRequestDto REQUEST_1_DTO = new ItemRequestDto(1, "knife for vegetables", 1,
            LocalDateTime.of(2023, Month.AUGUST,  1, 0, 0), Collections.emptyList());
    public static final ItemRequestDto REQUEST_2_DTO = new ItemRequestDto(2, "plate for soup", 1,
            LocalDateTime.of(2023, Month.AUGUST,  3, 0, 0), Collections.emptyList());
    public static final ItemRequestDto REQUEST_3_DTO = new ItemRequestDto(3, "sunglasses", 2,
            LocalDateTime.of(2023, Month.AUGUST,  5, 0, 0), Collections.emptyList());
    private final TestEntityManager em;
    private final ItemRequestService requestService;
    private final ItemRequestRepository requestRepository;
    private final UserRepository userRepository;

    @Test
    void addRequest() {
        int requestorId = 1;
        userRepository.save(REQUESTOR_1);
        requestRepository.save(REQUEST_1);

        TypedQuery<ItemRequest> query = em.getEntityManager().createQuery(
                "Select r from ItemRequest r join r.requestor as " +
                        "requestor where requestor.id = :requestorId", ItemRequest.class);
        ItemRequest request = query.setParameter("requestorId", requestorId)
                .getSingleResult();

        assertThat(request, equalTo(REQUEST_1));
    }

    @Test
    void findUserRequests() {
        int requestorId = 1;
        userRepository.save(REQUESTOR_1);
        requestRepository.save(REQUEST_1);
        requestRepository.save(REQUEST_2);

        List<ItemRequestDto> actual = requestService.findUserRequests(requestorId);

        assertThat(actual, hasSize(2));
        assertThat(actual.get(0), equalTo(REQUEST_2_DTO));
        assertThat(actual.get(1), equalTo(REQUEST_1_DTO));
    }

    @Test
    void findAllRequests() {
        int userId = 2;
        userRepository.save(REQUESTOR_1);
        userRepository.save(REQUESTOR_2);
        requestRepository.save(REQUEST_1);
        requestRepository.save(REQUEST_2);
        requestRepository.save(REQUEST_3);

        List<ItemRequestDto> actual = requestService.findAllRequests(0, 5, userId);

        assertThat(actual, hasSize(2));
        assertThat(actual.get(0), equalTo(REQUEST_2_DTO));
        assertThat(actual.get(1), equalTo(REQUEST_1_DTO));
    }

    @Test
    void findRequestById() {
        int requestId = 1;
        int userId = 1;
        userRepository.save(REQUESTOR_1);
        requestRepository.save(REQUEST_1);
        requestRepository.save(REQUEST_2);

        ItemRequestDto actual = requestService.findRequestById(requestId, userId);

        assertThat(actual, equalTo(REQUEST_1_DTO));
    }
}
