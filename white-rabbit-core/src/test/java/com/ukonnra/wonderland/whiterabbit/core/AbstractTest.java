package com.ukonnra.wonderland.whiterabbit.core;

import com.ukonnra.wonderland.whiterabbit.core.domain.user.User;
import com.ukonnra.wonderland.whiterabbit.core.domain.user.UserRepository;
import com.ukonnra.wonderland.whiterabbit.core.domain.user.UserService;
import com.ukonnra.wonderland.whiterabbit.core.domain.user.valobj.Identifier;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
public abstract class AbstractTest {
  @Container
  private static final PostgreSQLContainer<?> postgreSQLContainer =
      new PostgreSQLContainer<>("postgres:14-alpine");

  @DynamicPropertySource
  private static void postgresqlProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
    registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
  }

  public AbstractTest(UserService userService, UserRepository userRepository) {
    this.userService = userService;
    this.userRepository = userRepository;
  }

  protected final UserService userService;

  protected final UserRepository userRepository;

  protected static Map<User.Role, List<User>> USERS;

  protected void prepareData() {
    var owner = new User();
    owner.setName("Owner");
    owner.setRole(User.Role.OWNER);
    owner.setIdentifiers(Set.of(new Identifier(Identifier.Type.AUTHING, "authing|owner")));

    var owner1 = new User();
    owner1.setName("Owner 1");
    owner1.setRole(User.Role.OWNER);
    owner1.setIdentifiers(Set.of(new Identifier(Identifier.Type.AUTHING, "authing|owner1")));

    var admin = new User();
    admin.setName("Admin");
    admin.setRole(User.Role.ADMIN);
    admin.setIdentifiers(Set.of(new Identifier(Identifier.Type.AUTHING, "authing|admin")));

    var admin1 = new User();
    admin1.setName("Admin 1");
    admin1.setRole(User.Role.ADMIN);
    admin1.setIdentifiers(Set.of(new Identifier(Identifier.Type.AUTHING, "authing|admin1")));

    var user = new User();
    user.setName("User");
    user.setRole(User.Role.USER);
    user.setIdentifiers(Set.of(new Identifier(Identifier.Type.AUTHING, "authing|user")));

    var user1 = new User();
    user1.setName("User 1");
    user1.setRole(User.Role.USER);
    user1.setIdentifiers(Set.of(new Identifier(Identifier.Type.AUTHING, "authing|user1")));

    USERS =
        Map.of(
            User.Role.OWNER,
            List.of(owner, owner1),
            User.Role.ADMIN,
            List.of(admin, admin1),
            User.Role.USER,
            List.of(user, user1));
    this.userRepository.saveAll(USERS.values().stream().flatMap(Collection::stream).toList());
  }

  protected void cleanUp() {
    this.userRepository.deleteAll();
  }
}
