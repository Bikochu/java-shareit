package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Controller
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
@Validated
public class RequestController {

    private static final String header = "X-Sharer-User-Id";

    private final RequestClient requestClient;

    @GetMapping
    public ResponseEntity<Object> getAllOwnRequests(@RequestHeader(header) Long userId) {
        return requestClient.getAllOwnRequests(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllOthersRequests(@RequestHeader(header) Long userId,
                                                       @PositiveOrZero @RequestParam(value = "from", defaultValue = "0") Integer from,
                                                       @Positive @RequestParam(value = "size", defaultValue = "10") Integer size) {
        return requestClient.getAllOthersRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getRequest(@RequestHeader(header) Long userId,
                                             @PathVariable @NotNull Long requestId) {
        return requestClient.getRequest(userId, requestId);
    }

    @PostMapping
    public ResponseEntity<Object> addRequest(@RequestHeader(header) Long userId,
                                             @RequestBody @Valid ItemRequestDto itemRequestDto) {
        log.info("Creating request {}, userId={}", itemRequestDto, userId);
        return requestClient.addRequest(userId, itemRequestDto);
    }
}
