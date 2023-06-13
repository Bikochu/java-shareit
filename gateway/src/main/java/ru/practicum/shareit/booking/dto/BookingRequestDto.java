package ru.practicum.shareit.booking.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import javax.validation.constraints.Future;
import javax.validation.constraints.FutureOrPresent;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookingRequestDto {
    Long id;

    @FutureOrPresent(message = "Time can't be in past")
    LocalDateTime start;

    @Future(message = "Time can't be in past")
    LocalDateTime end;

    Long bookerId;

    Long itemId;

    String status;
}