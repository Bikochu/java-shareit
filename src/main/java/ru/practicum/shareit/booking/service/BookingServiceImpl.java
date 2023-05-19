package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundBookingException;
import ru.practicum.shareit.exception.NotFoundItemException;
import ru.practicum.shareit.exception.UnsupportedStateException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UserService userService;
    private final ItemService itemService;

    @Override
    public List<BookingDto> getAllBookingsWithState(Long userId, String state) {
        userService.findUserById(userId);
        List<BookingDto> booking = bookingRepository.findAllByBookerIdOrderByStartDesc(userId).stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
        return getByState(booking, state);
    }

    @Override
    public List<BookingDto> getBookingByOwner(Long userId, String state) {
        userService.findUserById(userId);
        List<BookingDto> booking = bookingRepository.findAllByItemOwnerIdOrderByStartDesc(userId).stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
        return getByState(booking, state);
    }

    @Override
    public List<BookingDto> getBookingByOwnerId(Long ownerId) {
        return bookingRepository.findAllByItemOwnerIdOrderByStartAsc(ownerId).stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    @Override
    public BookingDto addBooking(Long userId, BookingRequestDto bookingRequestDto) {
        User booker = UserMapper.toUser(userService.findUserById(userId));
        Item item = itemService.findItem(bookingRequestDto.getItemId());
        if (!item.isAvailable()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Item is not available.");
        }
        if (bookingRequestDto.getStart().isAfter(bookingRequestDto.getEnd())
                || bookingRequestDto.getEnd().isBefore(bookingRequestDto.getStart())
                || bookingRequestDto.getStart().equals(bookingRequestDto.getEnd())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wrong Timestamps");
        }
        if (bookingRequestDto.getStart() == null || bookingRequestDto.getEnd() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wrong Timestamps");
        }
        if (item.getOwner().equals(booker)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "You are owner!");
        }
        Booking booking = new Booking(
                bookingRequestDto.getId(),
                bookingRequestDto.getStart(),
                bookingRequestDto.getEnd(),
                item,
                booker,
                Status.WAITING);
        bookingRepository.save(booking);
        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public BookingDto findBookingById(Long userId, Long bookingId) {
        userService.findUserById(userId);
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new NotFoundBookingException("Booking not found."));
        if (booking.getBooker().getId().equals(userId) || booking.getItem().getOwner().getId().equals(userId)) {
            return BookingMapper.toBookingDto(booking);
        } else {
            throw new NotFoundItemException("Item not found.");
        }
    }

    @Override
    public void removeBooking(Long userId, Long bookingId) {
        findBookingById(userId, bookingId);
        bookingRepository.deleteById(bookingId);
    }

    @Override
    public BookingDto bookingApprove(Long ownerId, Long bookingId, boolean approved) {
        userService.findUserById(ownerId);
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() -> new NotFoundBookingException("Booking not found."));
        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "You are not owner!");
        }
        if (booking.getStatus().equals(Status.APPROVED)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "It already approved!");
        }
        if (approved) {
            booking.setStatus(Status.APPROVED);
        } else {
            booking.setStatus(Status.REJECTED);
        }
        bookingRepository.save(booking);
        return BookingMapper.toBookingDto(booking);
    }

    public List<BookingDto> getByState(List<BookingDto> bookings, String state) {
        switch (state) {
            case "WAITING":
                return bookings.stream()
                        .filter(booking1 -> booking1.getStatus().equals("WAITING"))
                        .sorted(Comparator.comparing(BookingDto::getStart).reversed())
                        .collect(Collectors.toList());
            case "CURRENT":
                return bookings.stream()
                        .filter(booking1 -> booking1.getStart().isBefore(LocalDateTime.now())
                                && booking1.getEnd().isAfter(LocalDateTime.now()))
                        .sorted(Comparator.comparing(BookingDto::getId))
                        .collect(Collectors.toList());
            case "REJECTED":
                return bookings.stream()
                        .filter(booking1 -> booking1.getStatus().equals("REJECTED") || booking1.getStatus().equals("CANCELED"))
                        .sorted(Comparator.comparing(BookingDto::getStart).reversed())
                        .collect(Collectors.toList());
            case "FUTURE":
                return bookings.stream()
                        .filter(booking1 -> booking1.getStart().isAfter(LocalDateTime.now()))
                        .sorted(Comparator.comparing(BookingDto::getStart).reversed())
                        .collect(Collectors.toList());
            case "PAST":
                return bookings.stream()
                        .filter(booking1 -> booking1.getEnd().isBefore(LocalDateTime.now()))
                        .sorted(Comparator.comparing(BookingDto::getStart).reversed())
                        .collect(Collectors.toList());
            case "UNSUPPORTED_STATUS":
                throw new UnsupportedStateException("Unknown state: " + state);
            default:
                return bookings;
        }
    }
}
