package ru.practicum.shareit;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.Month;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.shareit.booking.BookingClient;
import ru.practicum.shareit.booking.BookingControllerGateway;
import ru.practicum.shareit.booking.BookingRequestDto;
import ru.practicum.shareit.booking.BookingState;
import ru.practicum.shareit.error_handler.ErrorHandler;

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
public class BookingControllerGatewayTest {

    @Autowired
    private BookingControllerGateway bookingController;
    @Autowired
    private ObjectMapper mapper;
    @MockBean
    private BookingClient bookingClient;
    @Autowired
    private MockMvc mockMvc;

    private static final LocalDateTime START = LocalDateTime.of(2023, Month.NOVEMBER,  1, 0, 0);
    private static final LocalDateTime END = LocalDateTime.of(2023, Month.DECEMBER,  1, 0, 0);
    public static final BookingRequestDto BOOKING_REQUEST_1_DTO = BookingRequestDto.builder()
            .start(START)
            .end(END)
            .itemId(1)
            .build();

    private static String getJson(String name, Object... args) throws IOException {
        try (InputStream resourceAsStream =
                     requireNonNull(BookingControllerGatewayTest.class.getResourceAsStream(name))) {
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
    void whenPostBooking_thenStatus200() throws Exception {
        when(bookingClient.postBooking(eq(2), any(BookingRequestDto.class)))
                .thenReturn(ResponseEntity.ok(getJson("/booking/booking.json")));

        mockMvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(BOOKING_REQUEST_1_DTO))
                        .header("X-Sharer-User-Id", 2)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().json(getJson("/booking/booking.json"), true));
    }

    @Test
    void whenPostBookingWithoutHeaderUserId_thenStatus500() throws Exception {

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
    void whenPostBookingWithoutStart_thenStatus400() throws Exception {

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
    void whenPostBookingWithStartIsPast_thenStatus400() throws Exception {

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
    void whenPostBookingWithEndBeforeStart_thenStatus400() throws Exception {

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
        when(bookingClient.approveOrRejectBooking(1, 1, true))
                .thenReturn(ResponseEntity.ok(getJson("/booking/approved.json")));

        mockMvc.perform(patch("/bookings/1?approved=true")
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().json(getJson("/booking/approved.json")));
    }

    @Test
    void whenGetBooking_thenStatus200andBookingReturned() throws Exception {
        when(bookingClient.getBookingById(2, 1))
                .thenReturn(ResponseEntity.ok(getJson("/booking/booking.json")));

        mockMvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 2)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().json(getJson("/booking/booking.json")));
    }

    @Test
    void whenGetUserBookings_thenStatus200andBookingsReturned() throws Exception {
        when(bookingClient.getUserBookings(2, BookingState.ALL, 0, 5))
                .thenReturn(ResponseEntity.ok(getJson("/booking/ALL.json")));

        mockMvc.perform(get("/bookings?state=ALL&from=0&size=5")
                        .header("X-Sharer-User-Id", 2)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().json(getJson("/booking/ALL.json"), true));
    }

    @Test
    void whenGetUserBookingsWithUnknownState_thenStatus400() throws Exception {
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
    void whenGetUserBookingsWithNegativeFrom_thenStatus400() throws Exception {
        mockMvc.perform(get("/bookings?state=ALL&from=-1&size=5")
                        .header("X-Sharer-User-Id", 2)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andExpect(jsonPath("$.error", is("getUserBookings.from: must be greater than or equal to 0")));
    }

    @Test
    void whenGetUserBookingsWithZeroSize_thenStatus400() throws Exception {
        mockMvc.perform(get("/bookings?state=ALL&from=0&size=0")
                        .header("X-Sharer-User-Id", 2)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andExpect(jsonPath("$.error", is("getUserBookings.size: must be greater than or equal to 1")));
    }

    @Test
    void whenGetOwnerItemsBookings_thenStatus200andBookingsReturned() throws Exception {
        when(bookingClient.getOwnerItemsBookings(1, BookingState.ALL, 0, 5))
                .thenReturn(ResponseEntity.ok(getJson("/booking/ALL.json")));

        mockMvc.perform(get("/bookings/owner?state=ALL&from=0&size=5")
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().json(getJson("/booking/ALL.json"), true));
    }

    @Test
    void whenGetOwnerItemsBookingsWithUnknownState_thenStatus400() throws Exception {
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
    void whenGetOwnerItemsBookingsWithNegativeFrom_thenStatus400() throws Exception {
        mockMvc.perform(get("/bookings/owner?state=ALL&from=-1&size=5")
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andExpect(jsonPath("$.error", is("getOwnerItemsBookings.from: must be greater than or equal to 0")));
    }

    @Test
    void whenGetOwnerItemsBookingsWithZeroSize_thenStatus400() throws Exception {
        mockMvc.perform(get("/bookings/owner?state=ALL&from=0&size=0")
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andExpect(jsonPath("$.error", is("getOwnerItemsBookings.size: must be greater than or equal to 1")));
    }

}
