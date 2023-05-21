package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findBookingByItemId(Long itemId);

    List<Booking> findAllByBookerIdOrderByStartDesc(Long userId);

    List<Booking> findAllByBookerIdAndStatusOrderByStartDesc(Long userId, Status status);

    List<Booking> findAllByBookerIdAndStartAfterOrderByStartDesc(Long userId, LocalDateTime start);

    List<Booking> findAllByBookerIdAndEndBeforeOrderByStartDesc(Long userId, LocalDateTime end);

    List<Booking> findAllByBookerIdAndStartBeforeAndEndAfterOrderByIdAsc(Long userId, LocalDateTime start, LocalDateTime end);

    List<Booking> findAllByItemOwnerIdOrderByStartDesc(Long userId);

    List<Booking> findAllByItemOwnerIdOrderByStartAsc(Long userId);

    List<Booking> findAllByItemOwnerIdAndStatusOrderByStartAsc(Long userId, Status status);

    List<Booking> findBookingByItemIdAndBookerIdAndStatus(Long itemId, Long userId, Status status);

    List<Booking> findAllByItemOwnerIdAndStatusOrderByStartDesc(Long userId, Status status);

    List<Booking> findAllByItemOwnerIdAndStartAfterOrderByStartDesc(Long userId, LocalDateTime start);

    List<Booking> findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(Long userId, LocalDateTime end);

    List<Booking> findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByIdAsc(Long userId, LocalDateTime Start, LocalDateTime end);
}
