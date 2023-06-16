package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.service.BookingService;

import javax.validation.Valid;
import java.util.List;

/**
 * TODO Sprint add-bookings.
 */
@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {

    private static final String header = "X-Sharer-User-Id";

    private final BookingService bookingService;

    @GetMapping
    public List<BookingDto> getAllBookingsWithState(@RequestHeader(header) Long userId,
                                                    @RequestParam(value = "state", defaultValue = "ALL") String state,
                                                    @RequestParam(value = "from", required = false) Integer from,
                                                    @RequestParam(value = "size", required = false) Integer size) {
        return bookingService.getAllBookingsWithState(userId, state, from, size);
    }

    @GetMapping("/owner")
    public List<BookingDto> getAllBookingByOwner(@RequestHeader(header) Long userId,
                                                 @RequestParam(value = "state", defaultValue = "ALL") String state,
                                                 @RequestParam(value = "from", required = false) Integer from,
                                                 @RequestParam(value = "size", required = false) Integer size) {
        return bookingService.getBookingByOwner(userId, state, from, size);
    }

    @GetMapping("/{bookingId}")
    public BookingDto findBookingById(@RequestHeader(header) Long userId,
                                      @PathVariable Long bookingId) {
        return bookingService.findBookingById(userId, bookingId);
    }

    @PostMapping
    public BookingDto addBooking(@Valid @RequestHeader(header) Long userId,
                                 @Valid @RequestBody BookingRequestDto bookingRequestDto) {
        return bookingService.addBooking(userId, bookingRequestDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto bookingApprove(@RequestHeader(header) Long ownerId,
                                     @PathVariable Long bookingId,
                                     @RequestParam(value = "approved") boolean approved) {
        return bookingService.bookingApprove(ownerId, bookingId, approved);
    }

}
