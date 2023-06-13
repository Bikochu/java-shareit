package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.exception.UnsupportedStateException;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Controller
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {

	private static final String header = "X-Sharer-User-Id";
	private final BookingClient bookingClient;

	@GetMapping
	public ResponseEntity<Object> getAllBookingsWithState(@RequestHeader(header) long userId,
														  @RequestParam(name = "state", defaultValue = "all") String stateParam,
														  @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
														  @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
		BookingState state = BookingState.from(stateParam)
				.orElseThrow(() -> new UnsupportedStateException("Unknown state: " + stateParam));
		log.info("Get booking with state {}, userId={}, from={}, size={}", stateParam, userId, from, size);
		return bookingClient.getAllBookingsWithState(userId, state, from, size);
	}

	@GetMapping("/owner")
	public ResponseEntity<Object> getAllBookingByOwner(@RequestHeader(header) long userId,
													   @RequestParam(name = "state", defaultValue = "all") String stateParam,
													   @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
													   @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {
		BookingState state = BookingState.from(stateParam)
				.orElseThrow(() -> new UnsupportedStateException("Unknown state: " + stateParam));
		log.info("Get booking with state {}, userId={}, from={}, size={}", stateParam, userId, from, size);
		return bookingClient.getAllBookingByOwner(userId, state, from, size);
	}

	@GetMapping("/{bookingId}")
	public ResponseEntity<Object> findBookingById(@RequestHeader(header) long userId,
												  @PathVariable @NotNull Long bookingId) {
		log.info("Get booking {}, userId={}", bookingId, userId);
		return bookingClient.findBookingById(userId, bookingId);
	}

	@PostMapping
	public ResponseEntity<Object> addBooking(@RequestHeader(header) long userId,
											 @RequestBody @Valid BookingRequestDto requestDto) {
		log.info("Creating booking {}, userId={}", requestDto, userId);
		return bookingClient.addBooking(userId, requestDto);
	}

	@PatchMapping("/{bookingId}")
	public ResponseEntity<Object> bookingApprove(@RequestHeader(header) Long ownerId,
												 @PathVariable Long bookingId,
												 @RequestParam(name = "approved") boolean approved) {
		return bookingClient.bookingApprove(ownerId, bookingId, approved);
	}

}
