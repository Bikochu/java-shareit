package ru.practicum.shareit.booking.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

/**
 * TODO Sprint add-bookings.
 */
@Data
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingDto {
    Long id;
    LocalDate start;
    LocalDate end;
    Long itemId;
    Long bookerId;
    String status;
}
