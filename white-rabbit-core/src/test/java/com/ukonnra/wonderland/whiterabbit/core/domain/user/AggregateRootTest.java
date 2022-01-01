package com.ukonnra.wonderland.whiterabbit.core.domain.user;

import com.ukonnra.wonderland.whiterabbit.core.AbstractTest;
import com.ukonnra.wonderland.whiterabbit.core.CoreConfiguration;
import com.ukonnra.wonderland.whiterabbit.core.domain.user.valobj.Identifier;
import com.ukonnra.wonderland.whiterabbit.core.infrastructure.CoreException;
import com.ukonnra.wonderland.whiterabbit.core.infrastructure.TestExpect;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@Slf4j
class AggregateRootTest extends AbstractTest {
  @Autowired
  public AggregateRootTest(UserService userService, UserRepository userRepository) {
    super(userService, userRepository);
  }

  @SpringBootConfiguration
  @EnableAutoConfiguration
  @Import({CoreConfiguration.class})
  static class Configuration {}

  @ParameterizedTest
  @MethodSource
  void testCreate(final String jwtSubject, final TestExpect<UserCommand.Create> expect) {
    this.cleanUp();
    this.prepareData();

    var token =
        new JwtAuthenticationToken(
            Jwt.withTokenValue(UUID.randomUUID().toString())
                .issuer(Identifier.Type.AUTHING.getIssuer())
                .subject(jwtSubject)
                .header("header", "header-value")
                .build());

    var operator =
        this.userRepository
            .findOne(QUser.user.identifiers.contains(new Identifier(token.getToken())))
            .map(User::toPresentationModel)
            .orElse(null);

    var user = new User();
    var command = expect.command().get();

    if (expect instanceof TestExpect.Success) {
      user.create(operator, token, command.name(), command.role(), command.identifiers());
      ((TestExpect.Success<UserCommand.Create, User>) expect).handler().accept(user);
    } else if (expect instanceof TestExpect.Failure<?, ?> handler) {
      Assertions.assertThatThrownBy(
              () ->
                  user.create(
                      operator, token, command.name(), command.role(), command.identifiers()))
          .isInstanceOf(handler.exceptionClass());
    } else {
      Assertions.fail("Invalid TestExpect: " + expect);
    }
  }

  private static Stream<Arguments> testCreate() {
    return Stream.of(
        Arguments.of(
            "authing|null",
            new TestExpect.Success<UserCommand.Create, User>(
                new UserCommand.Create(
                    "new null",
                    User.Role.USER,
                    Set.of(new Identifier(Identifier.Type.AUTHING, "authing|null"))),
                (user) ->
                    Assertions.assertThat(user.getIdentifiers())
                        .contains(new Identifier(Identifier.Type.AUTHING, "authing|null")))),
        Arguments.of(
            "authing|null",
            new TestExpect.Failure<>(
                new UserCommand.Create(
                    "new null",
                    User.Role.ADMIN,
                    Set.of(new Identifier(Identifier.Type.AUTHING, "authing|null"))),
                CoreException.NullOperatorCannotCreateUserRoleNotEqualsUser.class)),
        Arguments.of(
            "authing|null",
            new TestExpect.Failure<>(
                new UserCommand.Create("new null", User.Role.USER, Set.of()),
                CoreException.NullOperatorCannotCreateUserNotSingleIdentifier.class)),
        Arguments.of(
            "authing|null",
            new TestExpect.Failure<>(
                new UserCommand.Create(
                    "new null",
                    User.Role.USER,
                    Set.of(
                        new Identifier(Identifier.Type.AUTHING, "authing|null"),
                        new Identifier(Identifier.Type.AUTHING, "authing|null2"))),
                CoreException.NullOperatorCannotCreateUserNotSingleIdentifier.class)),
        Arguments.of(
            "authing|null",
            new TestExpect.Failure<>(
                new UserCommand.Create(
                    "new null",
                    User.Role.USER,
                    Set.of(new Identifier(Identifier.Type.AUTHING, "authing|null2"))),
                CoreException.NullOperatorCannotCreateUserIdentifierNotMatch.class)),
        Arguments.of(
            "authing|user",
            new TestExpect.Failure<>(
                new UserCommand.Create(
                    "new user",
                    User.Role.USER,
                    Set.of(new Identifier(Identifier.Type.AUTHING, "authing|user2"))),
                CoreException.UserOperatorCannotCreateUser.class)),
        Arguments.of(
            "authing|admin",
            new TestExpect.Success<>(
                new UserCommand.Create(
                    "new user",
                    User.Role.USER,
                    Set.of(new Identifier(Identifier.Type.AUTHING, "authing|new user"))),
                (user) -> {})),
        Arguments.of(
            "authing|admin",
            new TestExpect.Failure<>(
                new UserCommand.Create(
                    "new admin",
                    User.Role.ADMIN,
                    Set.of(new Identifier(Identifier.Type.AUTHING, "authing|new admin"))),
                CoreException.AdminOperatorCannotCreateUserRoleNotEqualsUser.class)),
        Arguments.of(
            "authing|owner",
            new TestExpect.Success<>(
                new UserCommand.Create(
                    "new owner",
                    User.Role.OWNER,
                    Set.of(new Identifier(Identifier.Type.AUTHING, "authing|new owner"))),
                (user) -> {})),
        Arguments.of(
            "authing|owner",
            new TestExpect.Failure<>(
                new UserCommand.Create("new admin", User.Role.ADMIN, Set.of()),
                CoreException.UserIdentifierCannotEmpty.class)));
  }

  @ParameterizedTest
  @MethodSource
  void testUpdate(final String jwtSubject, final TestExpect<UserCommand.Update> expect) {
    this.cleanUp();
    this.prepareData();

    var token =
        new JwtAuthenticationToken(
            Jwt.withTokenValue(UUID.randomUUID().toString())
                .issuer(Identifier.Type.AUTHING.getIssuer())
                .subject(jwtSubject)
                .header("header", "header-value")
                .build());

    var operator =
        this.userRepository
            .findOne(QUser.user.identifiers.contains(new Identifier(token.getToken())))
            .map(User::toPresentationModel)
            .orElse(null);

    var command = expect.command().get();
    var user = this.userRepository.findById(command.id()).orElseThrow();

    if (expect instanceof TestExpect.Success) {
      user.update(operator, command.name(), command.role(), command.identifiers());
      ((TestExpect.Success<UserCommand.Update, User>) expect).handler().accept(user);
    } else if (expect instanceof TestExpect.Failure<?, ?> handler) {
      Assertions.assertThatThrownBy(
              () -> user.update(operator, command.name(), command.role(), command.identifiers()))
          .isInstanceOf(handler.exceptionClass());
    } else {
      Assertions.fail("Invalid TestExpect: " + expect);
    }
  }

  private static Stream<Arguments> testUpdate() {
    return Stream.of(
        Arguments.of(
            "authing|user",
            new TestExpect.Failure<>(
                () ->
                    new UserCommand.Update(
                        USERS.get(User.Role.USER).get(0).getId(), null, null, null),
                CoreException.EmptyUpdateOperation.class)),
        Arguments.of(
            "authing|user",
            new TestExpect.Failure<>(
                () ->
                    new UserCommand.Update(
                        USERS.get(User.Role.USER).get(1).getId(), "new user 1", null, null),
                CoreException.OperatorNotWriteable.class)),
        Arguments.of(
            "authing|admin",
            new TestExpect.Failure<>(
                () ->
                    new UserCommand.Update(
                        USERS.get(User.Role.OWNER).get(0).getId(), "new owner", null, null),
                CoreException.OperatorNotWriteable.class)),
        Arguments.of(
            "authing|admin",
            new TestExpect.Failure<>(
                () ->
                    new UserCommand.Update(
                        USERS.get(User.Role.ADMIN).get(0).getId(), null, User.Role.OWNER, null),
                CoreException.AccessDeniedOnEntityField.class)),
        Arguments.of(
            "authing|user",
            new TestExpect.Failure<>(
                () ->
                    new UserCommand.Update(
                        USERS.get(User.Role.USER).get(0).getId(), "new user", null, Set.of()),
                CoreException.UserIdentifierCannotEmpty.class)),
        Arguments.of(
            "authing|user",
            new TestExpect.Success<UserCommand.Update, User>(
                () ->
                    new UserCommand.Update(
                        USERS.get(User.Role.USER).get(0).getId(), "new user", null, null),
                (user) -> Assertions.assertThat(user.getName()).isEqualTo("new user"))),
        Arguments.of(
            "authing|admin",
            new TestExpect.Success<UserCommand.Update, User>(
                () ->
                    new UserCommand.Update(
                        USERS.get(User.Role.USER).get(0).getId(), "new user", null, null),
                (user) -> Assertions.assertThat(user.getName()).isEqualTo("new user"))),
        Arguments.of(
            "authing|owner",
            new TestExpect.Success<UserCommand.Update, User>(
                () ->
                    new UserCommand.Update(
                        USERS.get(User.Role.ADMIN).get(0).getId(), "new admin", null, null),
                (user) -> Assertions.assertThat(user.getName()).isEqualTo("new admin"))),
        Arguments.of(
            "authing|owner",
            new TestExpect.Success<UserCommand.Update, User>(
                () ->
                    new UserCommand.Update(
                        USERS.get(User.Role.USER).get(0).getId(),
                        "new admin",
                        User.Role.ADMIN,
                        null),
                (user) -> Assertions.assertThat(user.getName()).isEqualTo("new admin"))));
  }
}
