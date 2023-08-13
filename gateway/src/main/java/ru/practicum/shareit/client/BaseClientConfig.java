package ru.practicum.shareit.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.shareit.booking.BookingClient;
import ru.practicum.shareit.item.ItemClient;
import ru.practicum.shareit.request.ItemRequestClient;
import ru.practicum.shareit.user.UserClient;

@Configuration
public class BaseClientConfig {
    @Value("${shareit-server.url}")
    private String serverUrl;

    @Bean
    public BookingClient bookingClient(RestTemplateBuilder builder) {
        return new BookingClient(serverUrl, builder);
    }

    @Bean
    public ItemClient itemClient(RestTemplateBuilder builder) {
        return new ItemClient(serverUrl, builder);
    }

    @Bean
    public ItemRequestClient itemRequestClient(RestTemplateBuilder builder) {
        return new ItemRequestClient(serverUrl, builder);
    }

    @Bean
    public UserClient userClient(RestTemplateBuilder builder) {
        return new UserClient(serverUrl, builder);
    }


}