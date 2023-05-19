package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingDtoIn;
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
                                                    @RequestParam(value = "state", defaultValue = "ALL") String state) {
        return bookingService.getAllBookingsWithState(userId, state);
    }

    @GetMapping("/owner")
    public List<BookingDto> getAllBookingByOwner(@RequestHeader(header) Long userId,
                                                 @RequestParam(value = "state", defaultValue = "ALL") String state) {
        return bookingService.getBookingByOwner(userId, state);
    }

    @GetMapping("/{bookingId}")
    public BookingDto findBookingById(@RequestHeader(header) Long userId,
                                      @PathVariable Long bookingId) {
        return bookingService.findBookingById(userId, bookingId);
    }

    @GetMapping("/owner/{ownerId}")
    public List<BookingDto> getAllBookingByOwnerId(@PathVariable Long ownerId) {
        return bookingService.getBookingByOwnerId(ownerId);
    }

    @PostMapping
    public BookingDto addBooking(@Valid @RequestHeader(header) Long userId,
                                 @Valid @RequestBody BookingDtoIn bookingDtoIn) {
        return bookingService.addBooking(userId, bookingDtoIn);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto bookingApprove(@RequestHeader(header) Long ownerId,
                                     @PathVariable Long bookingId,
                                     @RequestParam(value = "approved") boolean approved) {
        return bookingService.bookingApprove(ownerId, bookingId, approved);
    }

}
