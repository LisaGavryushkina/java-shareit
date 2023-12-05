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
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.pageable.OffsetPageRequest;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestDto;
import ru.practicum.shareit.request.ItemRequestMapper;
import ru.practicum.shareit.request.ItemRequestNotFoundException;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.request.ItemRequestService;
import ru.practicum.shareit.request.ItemRequestServiceImpl;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserNotFoundException;
import ru.practicum.shareit.user.UserRepository;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ItemRequestServiceUnitTest {

    public static final User REQUESTOR = new User(2, "user2", "user2@mail.ru");
    public static final ItemRequest REQUEST_1 = new ItemRequest(1, "knife for vegetables", REQUESTOR,
            LocalDateTime.of(2023, Month.AUGUST,  1, 0, 0), Collections.emptyList());
    public static final ItemRequestDto REQUEST_1_DTO = new ItemRequestDto(1, "knife for vegetables", 2,
            LocalDateTime.of(2023, Month.AUGUST,  1, 0, 0), Collections.emptyList());
    private ItemRequestService requestService;
    @Mock
    private ItemRequestRepository requestRepository;
    @Mock
    private ItemRequestMapper requestMapper;
    @Mock
    private UserRepository userRepository;

    @BeforeEach
    public void start() {
        requestService = new ItemRequestServiceImpl(requestRepository, requestMapper, userRepository);
    }

    @Test
    void whenAddRequest_thenRequestAdded() {
        when(userRepository.findById(2)).thenReturn(Optional.of(REQUESTOR));
        when(requestMapper.toItemRequest(any(ItemRequestDto.class), any(User.class), any(LocalDateTime.class), any()))
                .thenReturn(REQUEST_1);
        when(requestRepository.save(REQUEST_1)).thenReturn(REQUEST_1);
        when(requestMapper.toItemRequestDto(REQUEST_1)).thenReturn(REQUEST_1_DTO);

        ItemRequestDto actual = requestService.addRequest(REQUEST_1_DTO, 2, LocalDateTime.now());

        verify(requestRepository, times(1)).save(REQUEST_1);
        assertThat(actual, equalTo(REQUEST_1_DTO));
    }

    @Test
    void whenAddRequestWithWrongUserId_thenThrowUserNotFoundException() {
        when(userRepository.findById(2)).thenReturn(Optional.empty());

        UserNotFoundException ex = assertThrows(UserNotFoundException.class,
                () -> requestService.addRequest(REQUEST_1_DTO, 2, LocalDateTime.now()));

        assertThat(ex.getMessage(), equalTo("Пользователь [2] не найден"));
        verifyNoInteractions(requestRepository);
    }

    @Test
    void whenFindUserRequests_thenRequestsReturned() {
        when(userRepository.findById(2)).thenReturn(Optional.of(REQUESTOR));
        when(requestRepository.findAllByRequestorId(2)).thenReturn(List.of(REQUEST_1));
        when(requestMapper.toItemRequestDto(List.of(REQUEST_1))).thenReturn(List.of(REQUEST_1_DTO));

        List<ItemRequestDto> actual = requestService.findUserRequests(2);

        assertThat(actual, hasSize(1));
        assertThat(actual.get(0), equalTo(REQUEST_1_DTO));
    }

    @Test
    void whenFindUserRequestsWithWrongUserId_thenThrowUserNotFoundException() {
        when(userRepository.findById(2)).thenReturn(Optional.empty());

        UserNotFoundException ex = assertThrows(UserNotFoundException.class,
                () -> requestService.findUserRequests(2));

        assertThat(ex.getMessage(), equalTo("Пользователь [2] не найден"));
        verifyNoInteractions(requestRepository);
    }

    @Test
    void whenFindAllRequests_thenRequestsReturned() {
        when(userRepository.findById(2)).thenReturn(Optional.of(REQUESTOR));
        when(requestRepository.findAllWithoutUserRequests(2, new OffsetPageRequest(0, 5,
                Sort.by("created").descending()))).thenReturn(new PageImpl<>(List.of(REQUEST_1)));
        when(requestMapper.toItemRequestDto(REQUEST_1)).thenReturn(REQUEST_1_DTO);

        List<ItemRequestDto> actual = requestService.findAllRequests(0, 5, 2);

        assertThat(actual, hasSize(1));
        assertThat(actual.get(0), equalTo(REQUEST_1_DTO));
    }

    @Test
    void whenFindAllRequestsWithWrongUserId_thenThrowUserNotFoundException() {
        when(userRepository.findById(2)).thenReturn(Optional.empty());

        UserNotFoundException ex = assertThrows(UserNotFoundException.class,
                () -> requestService.findAllRequests(0, 5, 2));

        assertThat(ex.getMessage(), equalTo("Пользователь [2] не найден"));
        verifyNoInteractions(requestRepository);
    }

    @Test
    void whenFindRequestById_thenRequestReturned() {
        when(userRepository.findById(2)).thenReturn(Optional.of(REQUESTOR));
        when(requestRepository.findById(1)).thenReturn(Optional.of(REQUEST_1));
        when(requestMapper.toItemRequestDto(REQUEST_1)).thenReturn(REQUEST_1_DTO);

        ItemRequestDto actual = requestService.findRequestById(1, 2);

        assertThat(actual, equalTo(REQUEST_1_DTO));
    }

    @Test
    void whenFindRequestByIdAndRequestNotFound_thenThrowItemRequestNotFoundException() {
        when(userRepository.findById(2)).thenReturn(Optional.of(REQUESTOR));
        when(requestRepository.findById(1)).thenReturn(Optional.empty());

        ItemRequestNotFoundException ex = assertThrows(ItemRequestNotFoundException.class,
                () -> requestService.findRequestById(1, 2));

        assertThat(ex.getMessage(), equalTo("Запрос [1] не найден"));
    }

    @Test
    void whenFindRequestByIdWithWrongUserId_thenThrowUserNotFoundException() {
        when(userRepository.findById(2)).thenReturn(Optional.empty());

        UserNotFoundException ex = assertThrows(UserNotFoundException.class,
                () -> requestService.findRequestById(1, 2));

        assertThat(ex.getMessage(), equalTo("Пользователь [2] не найден"));
        verifyNoInteractions(requestRepository);
    }
}
