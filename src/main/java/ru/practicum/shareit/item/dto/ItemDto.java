package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * TODO Sprint add-controllers.
 */
@Data
@AllArgsConstructor
public class ItemDto {
    private Long id;

    @NotEmpty(message = "Item name can't be Empty")
    private String name;

    @NotEmpty(message = "Item description can't be Empty")
    private String description;

    @NotNull(message = "Item availability can't be Null")
    private Boolean available;

    private Long ownerId;
    private Long requestId;

    public boolean isAvailable() {
        return available;
    }
}
