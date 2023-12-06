# Веб-приложение ShareIt 

Приложение для аренды вещей. 

Пользователь может:

•	добавлять вещи, которыми готов поделиться;

•	находить нужную вещь и бронировать ее на какое-то время;

•	оставлять запрос на вещь, которой не оказалось в базе;

•	добавить вещь в ответ на запрос;

•	оставлять отзывы вещам, которые бронировал.

Микросервисный RESTful проект, который состоит из модуля gateway (где происходит валидация запросов) и основного модуля.

**Стек:** Java 11, Spring Boot, PostgreSQL , Docker, Spring Data JPA , Lombok.

**Тестирование:** Unit-тесты, интеграционные с использованием MockMVC , @DataJpaTest, Postman тесты.
