package ru.practicum.shareit;

import java.util.List;
import java.util.Optional;

import javax.persistence.TypedQuery;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserDto;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.UserService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureTestEntityManager
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserServiceIT {

    public static final User USER_1 = new User(1, "user1", "user1@mail.ru");
    public static final User USER_1_WITH_UPDATE = new User(1, "Lisa", "user1@mail.ru");
    public static final User USER_2 = new User(2, "user2", "user2@mail.ru");
    public static final UserDto USER_1_DTO = new UserDto(1, "user1", "user1@mail.ru");
    public static final UserDto USER_1_WITH_UPDATE_DTO = new UserDto(1, "Lisa", "user1@mail.ru");
    public static final UserDto USER_2_DTO = new UserDto(2, "user2", "user2@mail.ru");
    private final TestEntityManager em;
    private final UserService userService;
    private final UserRepository userRepository;

    @Test
    void addUser() {
        String name = "user1";
        userService.addUser(USER_1_DTO);

        TypedQuery<User> query = em.getEntityManager().createQuery("Select u from User as u where u.name = :name",
                User.class);
        User user = query.setParameter("name", name)
                .getSingleResult();

        assertThat(user, equalTo(USER_1));
    }

    @Test
    void updateUser() {
        int userId = 1;
        userRepository.save(USER_1);
        userService.updateUser(userId, USER_1_WITH_UPDATE_DTO);

        TypedQuery<User> query = em.getEntityManager().createQuery("Select u from User as u where u.id = :userId",
                User.class);
        User user = query.setParameter("userId", userId)
                .getSingleResult();

        assertThat(user, equalTo(USER_1_WITH_UPDATE));
    }

    @Test
    void getUserById() {
        int userId = 1;
        userRepository.save(USER_1);

        UserDto actual = userService.getUserById(userId);

        assertThat(actual, equalTo(USER_1_DTO));
    }

    @Test
    void deleteUserById() {
        int userId = 1;
        userRepository.save(USER_1);
        userService.deleteUserById(userId);

        Optional<User> user = userRepository.findById(userId);

        assertThat(user, equalTo(Optional.empty()));
    }

    @Test
    void getAllUsers() {
        userRepository.save(USER_1);
        userRepository.save(USER_2);

        List<UserDto> actual = userService.getAllUsers();

        assertThat(actual, equalTo(List.of(USER_1_DTO, USER_2_DTO)));
    }
}
