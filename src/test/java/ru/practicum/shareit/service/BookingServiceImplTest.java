package ru.practicum.shareit.service;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.server.ResponseStatusException;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingRequestDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.exception.NotFoundBookingException;
import ru.practicum.shareit.exception.UnsupportedStateException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
class BookingServiceImplTest {

    @Mock
    Clock clock;

    @Mock
    BookingRepository bookingRepository;

    @Mock
    UserService userService;

    @Mock
    ItemService itemService;

    @InjectMocks
    BookingServiceImpl bookingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        clock = Clock.fixed(LocalDateTime.parse("2023-06-01T12:00:00").atZone(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault());
        bookingService = new BookingServiceImpl(clock, bookingRepository, userService, itemService);
    }

    @Test
    void getAllBookingsWithState_WithPages_StateAll() {
        //Создаем сущность.
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime start = now.minusHours(1L);
        LocalDateTime end = now.plusHours(1L);
        Long userId = 1L;
        String state = "ALL";
        Item item = new Item(1L, "Fork", "Kitchen thing", true, new User(), null);
        User booker = new User();
        UserDto userDto = new UserDto();
        booker.setId(userId);
        Booking booking = new Booking(1L, start, end, item, booker, Status.APPROVED);
        List<Booking> bookings = new ArrayList<>();
        bookings.add(booking);
        Page<Booking> page = new PageImpl<>(bookings);
        List<BookingDto> bookingDtoList = bookings.stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());

        when(userService.findUserById(userId)).thenReturn(userDto);
        when(bookingRepository.findAllByBookerIdOrderByStartDesc(userId, PageRequest.of(0, 10)))
                .thenReturn(page);

        //Тестируем.
        List<BookingDto> result = bookingService.getAllBookingsWithState(userId, state, 0, 10);

        //Проверяем.
        assertEquals(bookingDtoList, result);
    }

    @Test
    void getAllBookingsWithState_WithPages_StateWAITING() {
        //Создаем сущность.
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime start = now.minusHours(1L);
        LocalDateTime end = now.plusHours(1L);
        Long userId = 1L;
        String state = "WAITING";
        Item item = new Item(1L, "Fork", "Kitchen thing", true, new User(), null);
        User booker = new User();
        UserDto userDto = new UserDto();
        booker.setId(userId);
        Booking booking = new Booking(1L, start, end, item, booker, Status.WAITING);
        List<Booking> bookings = new ArrayList<>();
        bookings.add(booking);
        Page<Booking> page = new PageImpl<>(bookings);
        List<BookingDto> bookingDtoList = bookings.stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());

        when(userService.findUserById(userId)).thenReturn(userDto);
        when(bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(userId, Status.WAITING, PageRequest.of(0, 10)))
                .thenReturn(page);

        //Тестируем.
        List<BookingDto> result = bookingService.getAllBookingsWithState(userId, state, 0, 10);

        //Проверяем.
        assertEquals(bookingDtoList, result);
    }

    @Test
    void getAllBookingsWithState_WithPages_StateCurrent() {
        //Создаем сущность.
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime start = now.minusHours(1L);
        LocalDateTime end = now.plusHours(1L);
        Long userId = 1L;
        String state = "CURRENT";
        Item item = new Item(1L, "Fork", "Kitchen thing", true, new User(), null);
        User booker = new User();
        UserDto userDto = new UserDto();
        booker.setId(userId);
        Booking booking = new Booking(1L, start, end, item, booker, Status.APPROVED);
        List<Booking> bookings = new ArrayList<>();
        bookings.add(booking);
        Page<Booking> page = new PageImpl<>(bookings);
        List<BookingDto> bookingDtoList = bookings.stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());

        when(userService.findUserById(userId)).thenReturn(userDto);
        doReturn(page).when(bookingRepository).findAllByBookerIdAndStartBeforeAndEndAfterOrderByIdAsc(userId, now, now, PageRequest.of(0, 10));

        //Тестируем.
        List<BookingDto> result = bookingService.getAllBookingsWithState(userId, state, 0, 10);

        //Проверяем.
        assertEquals(bookingDtoList, result);
    }

    @Test
    void getAllBookingsWithState_WithPages_StateRejected() {
        //Создаем сущность.
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime start = now.minusHours(1L);
        LocalDateTime end = now.plusHours(1L);
        Long userId = 1L;
        String state = "REJECTED";
        Item item = new Item(1L, "Fork", "Kitchen thing", true, new User(), null);
        User booker = new User();
        UserDto userDto = new UserDto();
        booker.setId(userId);
        Booking booking = new Booking(1L, start, end, item, booker, Status.REJECTED);
        List<Booking> bookings = new ArrayList<>();
        bookings.add(booking);
        Page<Booking> page = new PageImpl<>(bookings);
        List<BookingDto> bookingDtoList = bookings.stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());

        when(userService.findUserById(userId)).thenReturn(userDto);
        when(bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(userId, Status.REJECTED, PageRequest.of(0, 10)))
                .thenReturn(page);

        //Тестируем.
        List<BookingDto> result = bookingService.getAllBookingsWithState(userId, state, 0, 10);

        //Проверяем.
        assertEquals(bookingDtoList, result);
    }

    @Test
    void getAllBookingsWithState_WithPages_StateCanceled() {
        //Создаем сущность.
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime start = now.minusHours(1L);
        LocalDateTime end = now.plusHours(1L);
        Long userId = 1L;
        String state = "CANCELED";
        Item item = new Item(1L, "Fork", "Kitchen thing", true, new User(), null);
        User booker = new User();
        UserDto userDto = new UserDto();
        booker.setId(userId);
        Booking booking = new Booking(1L, start, end, item, booker, Status.CANCELED);
        List<Booking> bookings = new ArrayList<>();
        bookings.add(booking);
        Page<Booking> page = new PageImpl<>(bookings);
        List<BookingDto> bookingDtoList = bookings.stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());

        when(userService.findUserById(userId)).thenReturn(userDto);
        when(bookingRepository.findAllByBookerIdAndStatusOrderByStartDesc(userId, Status.CANCELED, PageRequest.of(0, 10)))
                .thenReturn(page);

        //Тестируем.
        List<BookingDto> result = bookingService.getAllBookingsWithState(userId, state, 0, 10);

        //Проверяем.
        assertEquals(bookingDtoList, result);
    }

    @Test
    void getAllBookingsWithState_WithPages_StateFuture() {
        //Создаем сущность.
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime start = now.minusHours(1L);
        LocalDateTime end = now.plusHours(1L);
        Long userId = 1L;
        String state = "FUTURE";
        Item item = new Item(1L, "Fork", "Kitchen thing", true, new User(), null);
        User booker = new User();
        UserDto userDto = new UserDto();
        booker.setId(userId);
        Booking booking = new Booking(1L, start, end, item, booker, Status.APPROVED);
        List<Booking> bookings = new ArrayList<>();
        bookings.add(booking);
        Page<Booking> page = new PageImpl<>(bookings);
        List<BookingDto> bookingDtoList = bookings.stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());

        when(userService.findUserById(userId)).thenReturn(userDto);
        when(bookingRepository.findAllByBookerIdAndStartAfterOrderByStartDesc(userId, now, PageRequest.of(0, 10)))
                .thenReturn(page);

        //Тестируем.
        List<BookingDto> result = bookingService.getAllBookingsWithState(userId, state, 0, 10);

        //Проверяем.
        assertEquals(bookingDtoList, result);
    }

    @Test
    void getAllBookingsWithState_WithPages_StatePast() {
        //Создаем сущность.
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime start = now.minusHours(1L);
        LocalDateTime end = now.plusHours(1L);
        Long userId = 1L;
        String state = "PAST";
        Item item = new Item(1L, "Fork", "Kitchen thing", true, new User(), null);
        User booker = new User();
        UserDto userDto = new UserDto();
        booker.setId(userId);
        Booking booking = new Booking(1L, start, end, item, booker, Status.APPROVED);
        List<Booking> bookings = new ArrayList<>();
        bookings.add(booking);
        Page<Booking> page = new PageImpl<>(bookings);
        List<BookingDto> bookingDtoList = bookings.stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());

        when(userService.findUserById(userId)).thenReturn(userDto);
        when(bookingRepository.findAllByBookerIdAndEndBeforeOrderByStartDesc(userId, now, PageRequest.of(0, 10)))
                .thenReturn(page);

        //Тестируем.
        List<BookingDto> result = bookingService.getAllBookingsWithState(userId, state, 0, 10);

        //Проверяем.
        assertEquals(bookingDtoList, result);
    }

    @Test
    void getAllBookingsWithState_WithPages_StateUnsupportedState() {
        //Создаем сущность.
        Long userId = 1L;
        String state = "UNSUPPORTED_STATUS";

        //Тестируем и проверяем.
        assertThrows(UnsupportedStateException.class, () -> bookingService.getAllBookingsWithState(userId, state, 0, 10));
    }

    @Test
    void getAllBookingsWithState_WithoutPages_StateUnsupportedState() {
        //Создаем сущность.
        Long userId = 1L;
        String state = "UNSUPPORTED_STATUS";
        Integer from = null;
        Integer size = null;

        //Тестируем и проверяем.
        assertThrows(UnsupportedStateException.class, () -> bookingService.getAllBookingsWithState(userId, state, from, size));
    }

    @Test
    void getAllBookingsWithState_ReturnException() {
        Long userId = 1L;
        String state = "ALL";
        assertThrows(ResponseStatusException.class, () -> bookingService.getAllBookingsWithState(userId, state, 0, 0));
    }

    @Test
    void getBookingByOwner_WithPages_StateAll() {
        //Создаем сущность.
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime start = now.minusHours(1L);
        LocalDateTime end = now.plusHours(1L);
        Long userId = 1L;
        String state = "ALL";
        Item item = new Item(1L, "Fork", "Kitchen thing", true, new User(), null);
        User booker = new User();
        UserDto userDto = new UserDto();
        booker.setId(userId);
        Booking booking = new Booking(1L, start, end, item, booker, Status.APPROVED);
        List<Booking> bookings = new ArrayList<>();
        bookings.add(booking);
        Page<Booking> page = new PageImpl<>(bookings);
        List<BookingDto> bookingDtoList = bookings.stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());

        when(userService.findUserById(userId)).thenReturn(userDto);
        when(bookingRepository.findAllByItemOwnerIdOrderByStartDesc(userId, PageRequest.of(0, 10)))
                .thenReturn(page);

        //Тестируем.
        List<BookingDto> result = bookingService.getBookingByOwner(userId, state, 0, 10);

        //Проверяем.
        assertEquals(bookingDtoList, result);
        verify(bookingRepository, times(1)).findAllByItemOwnerIdOrderByStartDesc(userId, PageRequest.of(0, 10));
        verifyNoMoreInteractions(bookingRepository);
    }

    @Test
    void getBookingByOwner_WithPages_StateWaiting() {
        //Создаем сущность.
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime start = now.minusHours(1L);
        LocalDateTime end = now.plusHours(1L);
        Long userId = 1L;
        String state = "WAITING";
        Item item = new Item(1L, "Fork", "Kitchen thing", true, new User(), null);
        User booker = new User();
        UserDto userDto = new UserDto();
        booker.setId(userId);
        Booking booking = new Booking(1L, start, end, item, booker, Status.WAITING);
        List<Booking> bookings = new ArrayList<>();
        bookings.add(booking);
        Page<Booking> page = new PageImpl<>(bookings);
        List<BookingDto> bookingDtoList = bookings.stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());

        when(userService.findUserById(userId)).thenReturn(userDto);
        when(bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(userId, Status.WAITING, PageRequest.of(0, 10)))
                .thenReturn(page);

        //Тестируем.
        List<BookingDto> result = bookingService.getBookingByOwner(userId, state, 0, 10);

        //Проверяем.
        assertEquals(bookingDtoList, result);
        verify(bookingRepository, times(1)).findAllByItemOwnerIdAndStatusOrderByStartDesc(userId, Status.WAITING, PageRequest.of(0, 10));
        verifyNoMoreInteractions(bookingRepository);
    }

    @Test
    void getBookingByOwner_WithPages_StateCurrent() {
        //Создаем сущность.
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime start = now.minusHours(1L);
        LocalDateTime end = now.plusHours(1L);
        Long userId = 1L;
        String state = "CURRENT";
        Item item = new Item(1L, "Fork", "Kitchen thing", true, new User(), null);
        User booker = new User();
        UserDto userDto = new UserDto();
        booker.setId(userId);
        Booking booking = new Booking(1L, start, end, item, booker, Status.APPROVED);
        List<Booking> bookings = new ArrayList<>();
        bookings.add(booking);
        Page<Booking> page = new PageImpl<>(bookings);
        List<BookingDto> bookingDtoList = bookings.stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());

        when(userService.findUserById(userId)).thenReturn(userDto);
        when(bookingRepository.findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByIdAsc(userId, now, now, PageRequest.of(0, 10)))
                .thenReturn(page);

        //Тестируем.
        List<BookingDto> result = bookingService.getBookingByOwner(userId, state, 0, 10);

        //Проверяем.
        assertEquals(bookingDtoList, result);
        verify(bookingRepository, times(1)).findAllByItemOwnerIdAndStartBeforeAndEndAfterOrderByIdAsc(userId, now, now, PageRequest.of(0, 10));
        verifyNoMoreInteractions(bookingRepository);
    }

    @Test
    void getBookingByOwner_WithPages_StateRejected() {
        //Создаем сущность.
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime start = now.minusHours(1L);
        LocalDateTime end = now.plusHours(1L);
        Long userId = 1L;
        String state = "REJECTED";
        Item item = new Item(1L, "Fork", "Kitchen thing", true, new User(), null);
        User booker = new User();
        UserDto userDto = new UserDto();
        booker.setId(userId);
        Booking booking = new Booking(1L, start, end, item, booker, Status.REJECTED);
        List<Booking> bookings = new ArrayList<>();
        bookings.add(booking);
        Page<Booking> page = new PageImpl<>(bookings);
        List<BookingDto> bookingDtoList = bookings.stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());

        when(userService.findUserById(userId)).thenReturn(userDto);
        when(bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(userId, Status.REJECTED, PageRequest.of(0, 10)))
                .thenReturn(page);

        //Тестируем.
        List<BookingDto> result = bookingService.getBookingByOwner(userId, state, 0, 10);

        //Проверяем.
        assertEquals(bookingDtoList, result);
        verify(bookingRepository, times(1)).findAllByItemOwnerIdAndStatusOrderByStartDesc(userId, Status.REJECTED, PageRequest.of(0, 10));
        verifyNoMoreInteractions(bookingRepository);

    }

    @Test
    void getBookingByOwner_WithPages_StateCanceled() {
        //Создаем сущность.
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime start = now.minusHours(1L);
        LocalDateTime end = now.plusHours(1L);
        Long userId = 1L;
        String state = "CANCELED";
        Item item = new Item(1L, "Fork", "Kitchen thing", true, new User(), null);
        User booker = new User();
        UserDto userDto = new UserDto();
        booker.setId(userId);
        Booking booking = new Booking(1L, start, end, item, booker, Status.CANCELED);
        List<Booking> bookings = new ArrayList<>();
        bookings.add(booking);
        Page<Booking> page = new PageImpl<>(bookings);
        List<BookingDto> bookingDtoList = bookings.stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());

        when(userService.findUserById(userId)).thenReturn(userDto);
        when(bookingRepository.findAllByItemOwnerIdAndStatusOrderByStartDesc(userId, Status.CANCELED, PageRequest.of(0, 10)))
                .thenReturn(page);

        //Тестируем.
        List<BookingDto> result = bookingService.getBookingByOwner(userId, state, 0, 10);

        //Проверяем.
        assertEquals(bookingDtoList, result);
        verify(bookingRepository, times(1)).findAllByItemOwnerIdAndStatusOrderByStartDesc(userId, Status.CANCELED, PageRequest.of(0, 10));
        verifyNoMoreInteractions(bookingRepository);
    }

    @Test
    void getBookingByOwner_WithPages_StateFuture() {
        //Создаем сущность.
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime start = now.minusHours(1L);
        LocalDateTime end = now.plusHours(1L);
        Long userId = 1L;
        String state = "FUTURE";
        Item item = new Item(1L, "Fork", "Kitchen thing", true, new User(), null);
        User booker = new User();
        UserDto userDto = new UserDto();
        booker.setId(userId);
        Booking booking = new Booking(1L, start, end, item, booker, Status.APPROVED);
        List<Booking> bookings = new ArrayList<>();
        bookings.add(booking);
        Page<Booking> page = new PageImpl<>(bookings);
        List<BookingDto> bookingDtoList = bookings.stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());

        when(userService.findUserById(userId)).thenReturn(userDto);
        when(bookingRepository.findAllByItemOwnerIdAndStartAfterOrderByStartDesc(userId, now, PageRequest.of(0, 10)))
                .thenReturn(page);

        //Тестируем.
        List<BookingDto> result = bookingService.getBookingByOwner(userId, state, 0, 10);

        //Проверяем.
        assertEquals(bookingDtoList, result);
        verify(bookingRepository, times(1)).findAllByItemOwnerIdAndStartAfterOrderByStartDesc(userId, now, PageRequest.of(0, 10));
        verifyNoMoreInteractions(bookingRepository);
    }

    @Test
    void getBookingByOwner_WithPages_StatePast() {
        //Создаем сущность.
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime start = now.minusHours(1L);
        LocalDateTime end = now.plusHours(1L);
        Long userId = 1L;
        String state = "PAST";
        Item item = new Item(1L, "Fork", "Kitchen thing", true, new User(), null);
        User booker = new User();
        UserDto userDto = new UserDto();
        booker.setId(userId);
        Booking booking = new Booking(1L, start, end, item, booker, Status.APPROVED);
        List<Booking> bookings = new ArrayList<>();
        bookings.add(booking);
        Page<Booking> page = new PageImpl<>(bookings);
        List<BookingDto> bookingDtoList = bookings.stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());

        when(userService.findUserById(userId)).thenReturn(userDto);
        when(bookingRepository.findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(userId, now, PageRequest.of(0, 10)))
                .thenReturn(page);

        //Тестируем.
        List<BookingDto> result = bookingService.getBookingByOwner(userId, state, 0, 10);

        //Проверяем.
        assertEquals(bookingDtoList, result);
        verify(bookingRepository, times(1)).findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(userId, now, PageRequest.of(0, 10));
        verifyNoMoreInteractions(bookingRepository);
    }

    @Test
    void getBookingByOwner_WithPages_StateUnsupportedState() {
        //Создаем сущность.
        Long userId = 1L;
        String state = "UNSUPPORTED_STATUS";
        Integer from = 0;
        Integer size = 10;

        //Тестируем и проверяем.
        assertThrows(UnsupportedStateException.class, () -> bookingService.getBookingByOwner(userId, state, from, size));
    }

    @Test
    void getBookingByOwner_ReturnException() {
        Long userId = 1L;
        String state = "ALL";
        assertThrows(ResponseStatusException.class, () -> bookingService.getBookingByOwner(userId, state, 0, 0));
    }

    @Test
    void getBookingByOwner_WithoutPages_StateUnsupportedState() {
        //Создаем сущность.
        Long userId = 1L;
        String state = "UNSUPPORTED_STATUS";
        Integer from = null;
        Integer size = null;

        //Тестируем и проверяем.
        assertThrows(UnsupportedStateException.class, () -> bookingService.getBookingByOwner(userId, state, from, size));
    }

    @Test
    void addBooking() {
        //Создаем сущности.
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime start = now.minusHours(1L);
        LocalDateTime end = now.plusHours(1L);
        Long bookerId = 1L;
        Long itemId = 1L;
        String status = "WAITING";

        User booker = new User(1L, "Ivan", "ivan@bik.com");
        UserDto bookerDto = UserMapper.toUserDto(booker);
        User owner = new User(2L, "Svetlana", "sveta@mail.com");
        Item item = new Item(1L, "Fork", "Thing for food.", true, owner, null);
        BookingRequestDto bookingRequestDto = new BookingRequestDto(1L, start, end, bookerId, itemId, status);

        when(userService.findUserById(anyLong())).thenReturn(bookerDto);
        when(itemService.findItem(anyLong())).thenReturn(item);
        when(itemService.findItem(bookingRequestDto.getItemId())).thenReturn(item);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        //Тестируем.
        BookingDto result = bookingService.addBooking(bookerId, bookingRequestDto);

        //Проверяем.
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(bookingRequestDto.getStart(), result.getStart());
        assertEquals(bookingRequestDto.getEnd(), result.getEnd());
        assertEquals(item.getId(), result.getItem().getId());
        assertEquals(booker.getId(), result.getBooker().getId());
        assertEquals(Status.WAITING.toString(), result.getStatus());

        verify(userService, times(1)).findUserById(bookerId);
        verify(itemService, times(1)).findItem(itemId);
        verify(bookingRepository, times(1)).save(any(Booking.class));
    }

    @Test
    void addBooking_ErrorResponse_ItemFalse() {
        //Создаем сущности.
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime start = now.minusHours(1L);
        LocalDateTime end = now.plusHours(1L);
        Long bookerId = 1L;
        String status = "WAITING";

        User booker = new User(1L, "Ivan", "ivan@bik.com");
        UserDto bookerDto = UserMapper.toUserDto(booker);
        User owner = new User(2L, "Svetlana", "sveta@mail.com");
        Item item = new Item(1L, "Fork", "Thing for food.", false, owner, null);
        ItemDto itemDto = ItemMapper.toItemDto(item);
        BookingDto bookingDto = new BookingDto();
        bookingDto.setId(1L);
        bookingDto.setStart(start);
        bookingDto.setEnd(end);
        bookingDto.setBooker(bookerDto);
        bookingDto.setItem(itemDto);
        bookingDto.setStatus(status);

        BookingRequestDto bookingRequestDto = BookingMapper.toBookingRequestDto(BookingMapper.toBooking(bookingDto, item, booker));
        //new BookingRequestDto(1L, start, end, bookerId, itemId, status);
        when(userService.findUserById(anyLong())).thenReturn(bookerDto);
        when(itemService.findItem(anyLong())).thenReturn(item);
        when(itemService.findItem(bookingRequestDto.getItemId())).thenReturn(item);

        //Тестируем и проверяем.
        assertThrows(ResponseStatusException.class, () -> bookingService.addBooking(bookerId, bookingRequestDto));
    }

    @Test
    void addBooking_ErrorResponse_Time() {
        //Создаем сущности.
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime start = now.minusHours(1L);
        LocalDateTime end = now.minusHours(1L);
        Long bookerId = 1L;
        Long itemId = 1L;
        String status = "WAITING";

        User booker = new User(1L, "Ivan", "ivan@bik.com");
        UserDto bookerDto = UserMapper.toUserDto(booker);
        User owner = new User(2L, "Svetlana", "sveta@mail.com");
        Item item = new Item(1L, "Fork", "Thing for food.", true, owner, null);
        BookingRequestDto bookingRequestDto = new BookingRequestDto(1L, start, end, bookerId, itemId, status);

        when(userService.findUserById(anyLong())).thenReturn(bookerDto);
        when(itemService.findItem(anyLong())).thenReturn(item);
        when(itemService.findItem(bookingRequestDto.getItemId())).thenReturn(item);

        //Тестируем и проверяем.
        assertThrows(ResponseStatusException.class, () -> bookingService.addBooking(bookerId, bookingRequestDto));
    }

    @Test
    void addBooking_ErrorResponse_OwnerIsBooker() {
        //Создаем сущности.
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime start = now.minusHours(1L);
        LocalDateTime end = now.plusHours(1L);
        Long bookerId = 1L;
        Long itemId = 1L;
        String status = "WAITING";

        User booker = new User(1L, "Ivan", "ivan@bik.com");
        UserDto bookerDto = UserMapper.toUserDto(booker);
        Item item = new Item(1L, "Fork", "Thing for food.", true, booker, null);
        BookingRequestDto bookingRequestDto = new BookingRequestDto(1L, start, end, bookerId, itemId, status);

        when(userService.findUserById(anyLong())).thenReturn(bookerDto);
        when(itemService.findItem(anyLong())).thenReturn(item);
        when(itemService.findItem(bookingRequestDto.getItemId())).thenReturn(item);

        //Тестируем и проверяем.
        assertThrows(ResponseStatusException.class, () -> bookingService.addBooking(bookerId, bookingRequestDto));
    }

    @Test
    void addBooking_ErrorResponse_TimeIsNull() {
        //Создаем сущности.
        Long bookerId = 1L;
        Long itemId = 1L;
        String status = "WAITING";

        User booker = new User(1L, "Ivan", "ivan@bik.com");
        UserDto bookerDto = UserMapper.toUserDto(booker);
        Item item = new Item(1L, "Fork", "Thing for food.", true, booker, null);
        BookingRequestDto bookingRequestDto = new BookingRequestDto(1L, null, null, bookerId, itemId, status);

        when(userService.findUserById(anyLong())).thenReturn(bookerDto);
        when(itemService.findItem(anyLong())).thenReturn(item);
        when(itemService.findItem(bookingRequestDto.getItemId())).thenReturn(item);

        //Тестируем и проверяем.
        assertThrows(ResponseStatusException.class, () -> bookingService.addBooking(bookerId, bookingRequestDto));
    }

    @Test
    void findBookingById() {
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime start = now.minusHours(1L);
        LocalDateTime end = now.plusHours(1L);
        Long userId = 1L;
        Long bookingId = 1L;
        User booker = new User(1L, "Ivan", "ivan@bik.com");
        UserDto userDto = UserMapper.toUserDto(booker);
        User owner = new User(2L, "Svetlana", "sveta@mail.com");
        Item item = new Item(1L, "Fork", "Thing for food.", true, owner, null);
        Booking booking = new Booking(1L, start, end, item, booker, Status.APPROVED);
        BookingDto bookingDto = BookingMapper.toBookingDto(booking);

        when(userService.findUserById(anyLong())).thenReturn(userDto);
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        BookingDto result = bookingService.findBookingById(userId, bookingId);

        assertEquals(bookingDto, result);

        verify(userService, times(1)).findUserById(anyLong());
        verify(bookingRepository, times(1)).findById(anyLong());
    }

    @Test
    void findBookingById_ErrorResponse() {
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime start = now.minusHours(1L);
        LocalDateTime end = now.plusHours(1L);
        Long userId = 3L;
        Long bookingId = 1L;
        User booker = new User(1L, "Ivan", "ivan@bik.com");
        UserDto userDto = UserMapper.toUserDto(booker);
        User owner = new User(2L, "Svetlana", "sveta@mail.com");
        Item item = new Item(1L, "Fork", "Thing for food.", true, owner, null);
        Booking booking = new Booking(1L, start, end, item, booker, Status.APPROVED);

        when(userService.findUserById(anyLong())).thenReturn(userDto);
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        assertThrows(NotFoundBookingException.class, () -> bookingService.findBookingById(userId, bookingId));
    }

    @Test
    void bookingApprove_Approved() {
        boolean approved = true;
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime start = now.minusHours(1L);
        LocalDateTime end = now.plusHours(1L);
        Long userId = 1L;
        Long bookingId = 1L;
        User booker = new User(1L, "Ivan", "ivan@bik.com");
        UserDto userDto = UserMapper.toUserDto(booker);
        Item item = new Item(1L, "Fork", "Thing for food.", true, booker, null);
        Booking booking = new Booking(1L, start, end, item, booker, Status.WAITING);

        when(userService.findUserById(anyLong())).thenReturn(userDto);
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        doAnswer(invocation -> {
            Booking bookingArg = invocation.getArgument(0);
            bookingArg.setStatus(Status.APPROVED);
            return null;
        }).when(bookingRepository).save(any(Booking.class));

        bookingService.bookingApprove(userId, bookingId, approved);

        assertEquals(Status.APPROVED, booking.getStatus());

        verify(userService, times(1)).findUserById(userId);
        verify(bookingRepository, times(1)).findById(bookingId);
        verify(bookingRepository, times(1)).save(booking);
    }

    @Test
    void bookingApprove_Rejected() {
        boolean approved = false;
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime start = now.minusHours(1L);
        LocalDateTime end = now.plusHours(1L);
        Long userId = 1L;
        Long bookingId = 1L;
        User booker = new User(1L, "Ivan", "ivan@bik.com");
        UserDto userDto = UserMapper.toUserDto(booker);
        Item item = new Item(1L, "Fork", "Thing for food.", true, booker, null);
        Booking booking = new Booking(1L, start, end, item, booker, Status.WAITING);

        when(userService.findUserById(anyLong())).thenReturn(userDto);
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        doAnswer(invocation -> {
            Booking bookingArg = invocation.getArgument(0);
            bookingArg.setStatus(Status.REJECTED);
            return null;
        }).when(bookingRepository).save(any(Booking.class));

        bookingService.bookingApprove(userId, bookingId, approved);

        assertEquals(Status.REJECTED, booking.getStatus());

        verify(userService, times(1)).findUserById(userId);
        verify(bookingRepository, times(1)).findById(bookingId);
        verify(bookingRepository, times(1)).save(booking);
    }

    @Test
    void bookingApprove_NotOwner() {
        boolean approved = true;
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime start = now.minusHours(1L);
        LocalDateTime end = now.plusHours(1L);
        Long userId = 1L;
        Long bookingId = 1L;
        User booker = new User(1L, "Ivan", "ivan@bik.com");
        UserDto userDto = UserMapper.toUserDto(booker);
        User owner = new User(2L, "Svetlana", "sveta@mail.com");
        Item item = new Item(1L, "Fork", "Thing for food.", true, owner, null);
        Booking booking = new Booking(1L, start, end, item, booker, Status.WAITING);

        when(userService.findUserById(anyLong())).thenReturn(userDto);
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        assertThrows(ResponseStatusException.class, () -> bookingService.bookingApprove(userId, bookingId, approved));
    }

    @Test
    void bookingApprove_AlreadyApproved() {
        boolean approved = true;
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime start = now.minusHours(1L);
        LocalDateTime end = now.plusHours(1L);
        Long userId = 1L;
        Long bookingId = 1L;
        User booker = new User(1L, "Ivan", "ivan@bik.com");
        UserDto userDto = UserMapper.toUserDto(booker);
        Item item = new Item(1L, "Fork", "Thing for food.", true, booker, null);
        Booking booking = new Booking(1L, start, end, item, booker, Status.APPROVED);

        when(userService.findUserById(anyLong())).thenReturn(userDto);
        when(bookingRepository.findById(anyLong())).thenReturn(Optional.of(booking));

        assertThrows(ResponseStatusException.class, () -> bookingService.bookingApprove(userId, bookingId, approved));
    }
}