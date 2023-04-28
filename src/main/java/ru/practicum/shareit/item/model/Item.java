package ru.practicum.shareit.item.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * TODO Sprint add-controllers.
 */
@Data
@AllArgsConstructor
public class Item {
    private Long id;

    @NotEmpty(message = "Item name can't be Empty")
    private String name;

    @NotEmpty(message = "Item description can't be Empty")
    private String description;

    @NotNull(message = "Item availability can't be Null")
    private Boolean available;

    private User owner;
    private ItemRequest request;

    public boolean isAvailable() {
        return available;
    }
}
