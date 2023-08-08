package ru.practicum.shareit;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.Month;
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
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.BookingNotFoundException;
import ru.practicum.shareit.booking.BookingRequestDto;
import ru.practicum.shareit.booking.BookingResponseDto;
import ru.practicum.shareit.booking.BookingService;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.error_handler.ErrorHandler;
import ru.practicum.shareit.error_handler.ShareitInvalidArgumentException;
import ru.practicum.shareit.item.ItemDto;
import ru.practicum.shareit.user.UserDto;

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
public class BookingControllerTest {

    public static final UserDto BOOKER_DTO = new UserDto(2, "user2", "user2@mail.ru");
    public static final ItemDto ITEM_DTO_1 = new ItemDto(1, "knife", "for vegetables", true, null);
    public static final ItemDto ITEM_DTO_2 = new ItemDto(2, "plate", "for soup", true, null);
    private static final LocalDateTime START = LocalDateTime.of(2023, Month.NOVEMBER, 1, 0, 0);
    private static final LocalDateTime END = LocalDateTime.of(2023, Month.DECEMBER, 1, 0, 0);
    public static final BookingRequestDto BOOKING_REQUEST_1_DTO = BookingRequestDto.builder()
            .start(START)
            .end(END)
            .itemId(1)
            .build();
    public static final BookingResponseDto BOOKING_RESPONSE_1_DTO = BookingResponseDto.builder()
            .id(1)
            .start(START)
            .end(END)
            .item(ITEM_DTO_1)
            .booker(BOOKER_DTO)
            .status(BookingStatus.WAITING)
            .build();
    public static final BookingResponseDto BOOKING_RESPONSE_2_DTO = BookingResponseDto.builder()
            .id(2)
            .start(START)
            .end(END)
            .item(ITEM_DTO_2)
            .booker(BOOKER_DTO)
            .status(BookingStatus.WAITING)
            .build();
    public static final BookingResponseDto BOOKING_RESPONSE_APPROVED = BookingResponseDto.builder()
            .id(1)
            .start(START)
            .end(END)
            .item(ITEM_DTO_1)
            .booker(BOOKER_DTO)
            .status(BookingStatus.APPROVED)
            .build();
    @Autowired
    private BookingController bookingController;
    @Autowired
    private ObjectMapper mapper;
    @MockBean
    private BookingService bookingService;
    @Autowired
    private MockMvc mockMvc;

    private static String getJson(String name, Object... args) throws IOException {
        try (InputStream resourceAsStream = requireNonNull(BookingControllerTest.class.getResourceAsStream(name))) {
            return String.format(new String(resourceAsStream.readAllBytes()), args);
        }
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(bookingController)
                .setControllerAdvice(ErrorHandler.class)
                .build();
    }

    @Test
    void whenAddBooking_thenStatus200andBookingAdded() throws Exception {
        when(bookingService.addBooking(any(), eq(2)))
                .thenReturn(BOOKING_RESPONSE_1_DTO);

        mockMvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(BOOKING_REQUEST_1_DTO))
                        .header("X-Sharer-User-Id", 2)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().json(getJson("/booking/added.json"), true));
    }

    @Test
    void whenAddBookingWithoutHeaderUserId_thenStatus500() throws Exception {

        mockMvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(BOOKING_REQUEST_1_DTO))
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
    void whenAddBookingWithoutStart_thenStatus400() throws Exception {

        mockMvc.perform(post("/bookings")
                        .content(getJson("/booking/booking_without_start.json"))
                        .header("X-Sharer-User-Id", 2)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andExpect(jsonPath("$.error", is("{Start and end dates are invalid}")));
    }

    @Test
    void whenAddBookingWithStartIsPast_thenStatus400() throws Exception {

        mockMvc.perform(post("/bookings")
                        .content(getJson("/booking/booking_start_in_past.json"))
                        .header("X-Sharer-User-Id", 2)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andExpect(jsonPath("$.error", is("must be a date in the present or in the future")));
    }

    @Test
    void whenAddBookingWithEndBeforeStart_thenStatus400() throws Exception {

        mockMvc.perform(post("/bookings")
                        .content(getJson("/booking/booking_end_before_start.json"))
                        .header("X-Sharer-User-Id", 2)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andExpect(jsonPath("$.error", is("{Start and end dates are invalid}")));
    }

    @Test
    void whenApproveBooking_thenStatus200andBookingApproved() throws Exception {
        when(bookingService.approveOrRejectBooking(1, 1, true))
                .thenReturn(BOOKING_RESPONSE_APPROVED);

        mockMvc.perform(patch("/bookings/1?approved=true")
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().json(mapper.writeValueAsString(BOOKING_RESPONSE_APPROVED)));
    }

    @Test
    void whenShareItInvalidArgumentException_thenStatus400() throws Exception {
        when(bookingService.approveOrRejectBooking(1, 2, true))
                .thenThrow(new ShareitInvalidArgumentException("Invalid argument"));

        mockMvc.perform(patch("/bookings/1?approved=true")
                        .header("X-Sharer-User-Id", 2)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andExpect(jsonPath("$.error", is("Invalid argument")));
    }

    @Test
    void whenFindBooking_thenStatus200andBookingReturned() throws Exception {
        when(bookingService.findBooking(1, 2))
                .thenReturn(BOOKING_RESPONSE_1_DTO);

        mockMvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 2)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().json(mapper.writeValueAsString(BOOKING_RESPONSE_1_DTO)));

    }

    @Test
    void whenBookingNotFoundException_thenStatus404() throws Exception {
        when(bookingService.findBooking(1, 2))
                .thenThrow(new BookingNotFoundException(1));

        mockMvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 2)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound())
                .andDo(print())
                .andExpect(jsonPath("$.error", is("Бронирование [1] не найдено")));
    }

    @Test
    void whenFindUserBookings_thenStatus200andBookingsReturned() throws Exception {
        when(bookingService.findUserBookings(2, BookingState.ALL, 0, 5))
                .thenReturn(List.of(BOOKING_RESPONSE_1_DTO, BOOKING_RESPONSE_2_DTO));

        mockMvc.perform(get("/bookings?state=ALL&from=0&size=5")
                        .header("X-Sharer-User-Id", 2)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().json(getJson("/booking/bookings_ALL.json"), true));
    }

    @Test
    void whenFindUserBookingsWithUnknownState_thenStatus400() throws Exception {
        mockMvc.perform(get("/bookings?state=KEK&from=0&size=5")
                        .header("X-Sharer-User-Id", 2)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andExpect(jsonPath("$.error", is("Unknown state: KEK")));
    }

    @Test
    void whenFindUserBookingsWithNegativeFrom_thenStatus400() throws Exception {
        mockMvc.perform(get("/bookings?state=ALL&from=-1&size=5")
                        .header("X-Sharer-User-Id", 2)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andExpect(jsonPath("$.error", is("Параметры from и size не могут быть отрицательными")));
    }

    @Test
    void whenFindUserBookingsWithZeroSize_thenStatus400() throws Exception {
        mockMvc.perform(get("/bookings?state=ALL&from=0&size=0")
                        .header("X-Sharer-User-Id", 2)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andExpect(jsonPath("$.error", is("Параметры from и size не могут быть отрицательными")));
    }

    @Test
    void whenFindOwnerItemsBookings_thenStatus200andBookingsReturned() throws Exception {
        when(bookingService.findOwnerItemsBookings(1, BookingState.ALL, 0, 5))
                .thenReturn(List.of(BOOKING_RESPONSE_1_DTO, BOOKING_RESPONSE_2_DTO));

        mockMvc.perform(get("/bookings/owner?state=ALL&from=0&size=5")
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().json(getJson("/booking/bookings_ALL.json"), true));
    }

    @Test
    void whenFindOwnerItemsBookingsWithUnknownState_thenStatus400() throws Exception {
        mockMvc.perform(get("/bookings/owner?state=KEK&from=0&size=5")
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andExpect(jsonPath("$.error", is("Unknown state: KEK")));
    }

    @Test
    void whenFindOwnerItemsBookingsWithNegativeFrom_thenStatus400() throws Exception {
        mockMvc.perform(get("/bookings/owner?state=ALL&from=-1&size=5")
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andExpect(jsonPath("$.error", is("Параметры from и size не могут быть отрицательными")));
    }

    @Test
    void whenFindOwnerItemsBookingsWithZeroSize_thenStatus400() throws Exception {
        mockMvc.perform(get("/bookings/owner?state=ALL&from=0&size=0")
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andExpect(jsonPath("$.error", is("Параметры from и size не могут быть отрицательными")));
    }

}
