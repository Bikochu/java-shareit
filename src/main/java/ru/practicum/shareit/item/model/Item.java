package ru.practicum.shareit.item.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * TODO Sprint add-controllers.
 */
@Data
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Item {
    Long id;

    @NotEmpty(message = "Item name can't be Empty")
    String name;

    @NotEmpty(message = "Item description can't be Empty")
    String description;

    @NotNull(message = "Item availability can't be Null")
    Boolean available;

    UserDto owner;
    ItemRequest request;

    public boolean isAvailable() {
        return available;
    }
}
