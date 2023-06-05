package ru.practicum.shareit.item.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@Data
@AllArgsConstructor
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemDto {
    Long id;

    @NotEmpty(message = "Item name can't be Empty")
    String name;

    @NotEmpty(message = "Item description can't be Empty")
    String description;

    @NotNull(message = "Item availability can't be Null")
    Boolean available;

    UserDto owner;

    Long requestId;

    List<CommentDto> comments;

    public boolean isAvailable() {
        return available;
    }
}
