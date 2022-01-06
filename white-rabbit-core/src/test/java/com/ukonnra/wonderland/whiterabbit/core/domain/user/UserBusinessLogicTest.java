package com.ukonnra.wonderland.whiterabbit.core.domain.user;

import com.ukonnra.wonderland.whiterabbit.core.AbstractTest;
import com.ukonnra.wonderland.whiterabbit.core.CoreConfiguration;
import com.ukonnra.wonderland.whiterabbit.core.domain.journal.AccountRepository;
import com.ukonnra.wonderland.whiterabbit.core.domain.journal.FinRecordItemRepository;
import com.ukonnra.wonderland.whiterabbit.core.domain.journal.JournalRepository;
import com.ukonnra.wonderland.whiterabbit.core.domain.journal.JournalService;
import com.ukonnra.wonderland.whiterabbit.core.domain.user.valobj.Identifier;
import com.ukonnra.wonderland.whiterabbit.core.infrastructure.CoreException;
import com.ukonnra.wonderland.whiterabbit.core.infrastructure.TestExpect;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import javax.transaction.Transactional;
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
@Transactional
class UserBusinessLogicTest extends AbstractTest {
  @Autowired
  public UserBusinessLogicTest(
      UserService userService,
      UserRepository userRepository,
      JournalService journalService,
      JournalRepository journalRepository,
      AccountRepository accountRepository,
      FinRecordItemRepository finRecordItemRepository) {
    super(
        userService,
        userRepository,
        journalService,
        journalRepository,
        accountRepository,
        finRecordItemRepository);
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
            .orElse(null);

    var command = expect.command().get();
    var user =
        Optional.ofNullable(command.id()).flatMap(this.userRepository::findById).orElse(new User());

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
            null,
            new TestExpect.Failure<>(
                () ->
                    new UserCommand.Create(
                        USERS.get(User.Role.USER).get(0).getId(),
                        "new user",
                        User.Role.USER,
                        Set.of(new Identifier(Identifier.Type.AUTHING, "authing|user"))),
                CoreException.AlreadyInitialized.class)),
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
            null,
            new TestExpect.Failure<>(
                () ->
                    new UserCommand.Update(
                        USERS.get(User.Role.USER).get(1).getId(), "new user 1", null, null),
                CoreException.OperatorNotWriteable.class)),
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
            "authing|owner",
            new TestExpect.Failure<>(
                () ->
                    new UserCommand.Update(
                        USERS.get(User.Role.USER).get(2).getId(), "new user", null, null),
                CoreException.NotFound.class)),
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
                        Set.of(new Identifier(Identifier.Type.AUTHING, "authing|newAdmin"))),
                (user) -> {
                  Assertions.assertThat(user.getName()).isEqualTo("new admin");
                  Assertions.assertThat(user.getIdentifiers())
                      .contains(new Identifier(Identifier.Type.AUTHING, "authing|newAdmin"));
                })));
  }

  @ParameterizedTest
  @MethodSource
  void testDelete(final String jwtSubject, final TestExpect<UserCommand.Delete> expect) {
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
            .orElse(null);

    var command = expect.command().get();
    var user = this.userRepository.findById(command.id()).orElseThrow();

    if (expect instanceof TestExpect.Success) {
      var identifiers = user.getIdentifiers();
      Assertions.assertThat(identifiers).isNotEmpty();
      var ident = identifiers.stream().findFirst().get();
      user.delete(operator);
      ((TestExpect.Success<UserCommand.Delete, User>) expect).handler().accept(user);
      this.userRepository.save(user);
      Assertions.assertThat(
              this.userRepository.findOne(
                  QUser.user.id.eq(user.getId()).and(QUser.user.deleted.isFalse())))
          .isEmpty();
      Assertions.assertThat(this.userRepository.findOne(QUser.user.identifiers.contains(ident)))
          .isEmpty();
    } else if (expect instanceof TestExpect.Failure<?, ?> handler) {
      Assertions.assertThatThrownBy(() -> user.delete(operator))
          .isInstanceOf(handler.exceptionClass());
    } else {
      Assertions.fail("Invalid TestExpect: " + expect);
    }
  }

  private static Stream<Arguments> testDelete() {
    return Stream.of(
        Arguments.of(
            null,
            new TestExpect.Failure<>(
                () -> new UserCommand.Delete(USERS.get(User.Role.USER).get(1).getId()),
                CoreException.OperatorNotWriteable.class)),
        Arguments.of(
            "authing|user",
            new TestExpect.Failure<>(
                () -> new UserCommand.Delete(USERS.get(User.Role.USER).get(1).getId()),
                CoreException.OperatorNotWriteable.class)),
        Arguments.of(
            "authing|userDeleted",
            new TestExpect.Failure<>(
                () -> new UserCommand.Delete(USERS.get(User.Role.USER).get(2).getId()),
                CoreException.NotFound.class)),
        Arguments.of(
            "authing|owner",
            new TestExpect.Success<UserCommand.Delete, User>(
                () -> new UserCommand.Delete(USERS.get(User.Role.USER).get(0).getId()),
                (user) -> {})),
        Arguments.of(
            "authing|user",
            new TestExpect.Success<UserCommand.Delete, User>(
                () -> new UserCommand.Delete(USERS.get(User.Role.USER).get(0).getId()),
                (user) -> {})));
  }
}
