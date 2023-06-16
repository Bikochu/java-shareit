package ru.practicum.shareit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.controller.BookingController;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
class BookingControllerTest {

    static final String header = "X-Sharer-User-Id";

    @Autowired
    MockMvc mockMvc;

    @MockBean
    BookingService bookingService;

    static ObjectMapper objectMapper = new ObjectMapper();

    public static String toJson(Object object) throws Exception {
        return objectMapper.writeValueAsString(object);
    }

    @Test
    void getAllBookingsWithState() throws Exception {
        Long userId = 1L;
        String state = "WAITING";
        Integer from = 0;
        Integer size = 10;
        UserDto booker = new UserDto();
        booker.setId(userId);
        ItemDto item = new ItemDto();
        List<BookingDto> bookingList = Arrays.asList(
                new BookingDto(1L, null, null, booker, item, "WAITING"),
                new BookingDto(2L, null, null, booker, item, "WAITING"));

        when(bookingService.getAllBookingsWithState(userId, state, from, size)).thenReturn(bookingList);

        mockMvc.perform(get("/bookings")
                        .header(header, userId)
                        .param("state", state)
                        .param("from", from.toString())
                        .param("size", size.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].status").value("WAITING"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].status").value("WAITING"));
    }

    @Test
    void getAllBookingByOwner() throws Exception {
        Long userId = 1L;
        String state = "WAITING";
        Integer from = 0;
        Integer size = 10;
        UserDto booker = new UserDto();
        booker.setId(userId);
        ItemDto item = new ItemDto();
        List<BookingDto> bookingList = Arrays.asList(
                new BookingDto(1L, null, null, booker, item, "WAITING"),
                new BookingDto(2L, null, null, booker, item, "WAITING"));

        when(bookingService.getBookingByOwner(userId, state, from, size)).thenReturn(bookingList);

        mockMvc.perform(get("/bookings/owner")
                        .header(header, userId)
                        .param("state", state)
                        .param("from", from.toString())
                        .param("size", size.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].status").value("WAITING"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].status").value("WAITING"));
    }

    @Test
    void findBookingById() throws Exception {
        Long userId = 1L;
        Long bookingId = 1L;
        UserDto booker = new UserDto();
        booker.setId(userId);
        ItemDto item = new ItemDto();
        BookingDto booking = new BookingDto(1L, null, null, booker, item, "WAITING");

        when(bookingService.findBookingById(userId, bookingId)).thenReturn(booking);

        mockMvc.perform(get("/bookings/1")
                        .header(header, userId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("WAITING"));
    }

    @Test
    void addBooking() throws Exception {
        Long userId = 1L;
        Long itemId = 1L;
        UserDto booker = new UserDto();
        booker.setId(userId);
        ItemDto item = new ItemDto();
        BookingDto booking = new BookingDto(1L, null, null, booker, item, "WAITING");
        BookingRequestDto bookingRequestDto = new BookingRequestDto(1L, null, null, userId, itemId, "WAITING");

        when(bookingService.addBooking(userId, bookingRequestDto)).thenReturn(booking);

        mockMvc.perform(post("/bookings")
                        .header(header, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(bookingRequestDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.booker.id").value(1));
    }

    @Test
    void bookingApprove() throws Exception {
        Long userId = 1L;
        Long bookingId = 1L;
        boolean approved = true;
        UserDto booker = new UserDto();
        booker.setId(userId);
        ItemDto item = new ItemDto();
        BookingDto booking = new BookingDto(bookingId, null, null, booker, item, "ACCEPTED");

        when(bookingService.bookingApprove(userId, bookingId, approved)).thenReturn(booking);

        mockMvc.perform(patch("/bookings/1").header(header, userId).param("approved", String.valueOf(approved)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }
}