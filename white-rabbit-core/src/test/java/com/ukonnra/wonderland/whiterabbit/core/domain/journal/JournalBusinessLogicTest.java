package com.ukonnra.wonderland.whiterabbit.core.domain.journal;

import com.ukonnra.wonderland.whiterabbit.core.AbstractTest;
import com.ukonnra.wonderland.whiterabbit.core.CoreConfiguration;
import com.ukonnra.wonderland.whiterabbit.core.domain.journal.entity.Account;
import com.ukonnra.wonderland.whiterabbit.core.domain.journal.valobj.FinRecordItemInput;
import com.ukonnra.wonderland.whiterabbit.core.domain.user.QUser;
import com.ukonnra.wonderland.whiterabbit.core.domain.user.User;
import com.ukonnra.wonderland.whiterabbit.core.domain.user.UserRepository;
import com.ukonnra.wonderland.whiterabbit.core.domain.user.UserService;
import com.ukonnra.wonderland.whiterabbit.core.domain.user.valobj.Identifier;
import com.ukonnra.wonderland.whiterabbit.core.infrastructure.CoreException;
import com.ukonnra.wonderland.whiterabbit.core.infrastructure.TestExpect;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.domain.AbstractPersistable;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@Slf4j
@Transactional
class JournalBusinessLogicTest extends AbstractTest {
  @Autowired
  public JournalBusinessLogicTest(
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
  void testCreate(final String jwtSubject, final TestExpect<JournalCommand.Create> expect) {
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
            .orElseThrow();

    var command = expect.command().get();
    var journal =
        Optional.ofNullable(command.id())
            .flatMap(this.journalRepository::findById)
            .orElse(new Journal());
    var admins = new HashSet<>(this.userRepository.findAllById(command.adminIds()));
    var members = new HashSet<>(this.userRepository.findAllById(command.memberIds()));

    if (expect instanceof TestExpect.Success) {
      journal.create(operator, command.name(), admins, members);
      ((TestExpect.Success<JournalCommand.Create, Journal>) expect).handler().accept(journal);
    } else if (expect instanceof TestExpect.Failure<?, ?> handler) {
      Assertions.assertThatThrownBy(() -> journal.create(operator, command.name(), admins, members))
          .isInstanceOf(handler.exceptionClass());
    } else {
      Assertions.fail("Invalid TestExpect: " + expect);
    }
  }

  private static Stream<Arguments> testCreate() {
    return Stream.of(
        Arguments.of(
            "authing|user",
            new TestExpect.Failure<>(
                () ->
                    new JournalCommand.Create(
                        JOURNALS.get(0).getId(),
                        "new journal",
                        Stream.of(USERS.get(User.Role.ADMIN).get(0))
                            .map(AbstractPersistable::getId)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet()),
                        Stream.of(USERS.get(User.Role.USER).get(0))
                            .map(AbstractPersistable::getId)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet())),
                CoreException.AlreadyInitialized.class)),
        Arguments.of(
            "authing|user",
            new TestExpect.Success<JournalCommand.Create, Journal>(
                () ->
                    new JournalCommand.Create(
                        null,
                        "new journal",
                        Stream.of(USERS.get(User.Role.ADMIN).get(0))
                            .map(AbstractPersistable::getId)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet()),
                        Stream.of(USERS.get(User.Role.USER).get(0))
                            .map(AbstractPersistable::getId)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet())),
                (journal) -> {
                  Assertions.assertThat(journal.admins)
                      .contains(
                          USERS.get(User.Role.ADMIN).get(0), USERS.get(User.Role.USER).get(0));
                  Assertions.assertThat(journal.members).isEmpty();
                })));
  }

  @ParameterizedTest
  @MethodSource
  void testUpdate(final String jwtSubject, final TestExpect<JournalCommand.Update> expect) {
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
            .orElseThrow();

    var command = expect.command().get();
    var journal =
        Optional.ofNullable(command.id())
            .flatMap(this.journalRepository::findById)
            .orElse(new Journal());
    var admins =
        Optional.ofNullable(command.adminIds())
            .map(this.userRepository::findAllById)
            .map(HashSet::new)
            .orElse(null);
    var members =
        Optional.ofNullable(command.memberIds())
            .map(this.userRepository::findAllById)
            .map(HashSet::new)
            .orElse(null);

    if (expect instanceof TestExpect.Success) {
      journal.update(operator, command.name(), admins, members);
      ((TestExpect.Success<JournalCommand.Update, Journal>) expect).handler().accept(journal);
    } else if (expect instanceof TestExpect.Failure<?, ?> handler) {
      Assertions.assertThatThrownBy(() -> journal.update(operator, command.name(), admins, members))
          .isInstanceOf(handler.exceptionClass());
    } else {
      Assertions.fail("Invalid TestExpect: " + expect);
    }
  }

  private static Stream<Arguments> testUpdate() {
    return Stream.of(
        Arguments.of(
            "authing|owner",
            new TestExpect.Failure<>(
                () -> new JournalCommand.Update(JOURNALS.get(0).getId(), null, null, null),
                CoreException.EmptyUpdateOperation.class)),
        Arguments.of(
            "authing|user",
            new TestExpect.Failure<>(
                () -> new JournalCommand.Update(JOURNALS.get(0).getId(), "new journal", null, null),
                CoreException.OperatorNotWriteable.class)),
        Arguments.of(
            "authing|admin",
            new TestExpect.Success<JournalCommand.Update, Journal>(
                () -> new JournalCommand.Update(JOURNALS.get(0).getId(), "new journal", null, null),
                (journal) -> Assertions.assertThat(journal.getName()).isEqualTo("new journal"))),
        Arguments.of(
            "authing|owner",
            new TestExpect.Failure<>(
                () -> new JournalCommand.Update(JOURNALS.get(3).getId(), "new journal", null, null),
                CoreException.NotFound.class)));
  }

  @ParameterizedTest
  @MethodSource
  void testDelete(final String jwtSubject, final TestExpect<JournalCommand.Delete> expect) {
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
            .orElseThrow();

    var command = expect.command().get();
    var journal =
        Optional.ofNullable(command.id())
            .flatMap(this.journalRepository::findById)
            .orElse(new Journal());

    if (expect instanceof TestExpect.Success) {
      journal.delete(operator);
      ((TestExpect.Success<JournalCommand.Delete, Journal>) expect).handler().accept(journal);
      this.journalRepository.save(journal);
      Assertions.assertThat(
              this.journalRepository.findOne(
                  QJournal.journal.id.eq(journal.getId()).and(QJournal.journal.deleted.isFalse())))
          .isEmpty();
    } else if (expect instanceof TestExpect.Failure<?, ?> handler) {
      Assertions.assertThatThrownBy(() -> journal.delete(operator))
          .isInstanceOf(handler.exceptionClass());
    } else {
      Assertions.fail("Invalid TestExpect: " + expect);
    }
  }

  private static Stream<Arguments> testDelete() {
    return Stream.of(
        Arguments.of(
            "authing|owner",
            new TestExpect.Failure<>(
                () -> new JournalCommand.Delete(JOURNALS.get(3).getId()),
                CoreException.NotFound.class)),
        Arguments.of(
            "authing|owner",
            new TestExpect.Success<JournalCommand.Delete, Journal>(
                () -> new JournalCommand.Delete(JOURNALS.get(1).getId()), (journal) -> {})),
        Arguments.of(
            "authing|user",
            new TestExpect.Failure<>(
                () -> new JournalCommand.Delete(JOURNALS.get(0).getId()),
                CoreException.OperatorNotWriteable.class)));
  }

  @Test
  void testCreateAccount() {
    this.cleanUp();
    this.prepareData();

    var journal = JOURNALS.get(0);
    journal.createAccount(
        USERS.get(User.Role.OWNER).get(0),
        "new account",
        "new description",
        Account.Type.ASSET,
        "USD",
        Account.InventoryType.AVERAGE);

    this.journalRepository.save(journal);

    Assertions.assertThat(journal.accounts)
        .anySatisfy(
            a -> {
              Assertions.assertThat(a.getName()).isEqualTo("new account");
              Assertions.assertThat(a.getDescription()).isEqualTo("new description");
              Assertions.assertThat(a.getType()).isEqualTo(Account.Type.ASSET);
            });
  }

  @Test
  void testUpdateAccount() {
    this.cleanUp();
    this.prepareData();

    var journal = JOURNALS.get(0);
    var account = journal.getAccounts().stream().findFirst().orElseThrow();
    var oldType = account.getType();
    journal.updateAccount(
        USERS.get(User.Role.OWNER).get(0), account, "new account", "new desc", null, null, null);

    this.journalRepository.save(journal);

    Assertions.assertThat(journal.accounts)
        .anySatisfy(
            a -> {
              Assertions.assertThat(a.getName()).isEqualTo("new account");
              Assertions.assertThat(a.getDescription()).isEqualTo("new desc");
              Assertions.assertThat(a.getType()).isEqualTo(oldType);
            });
  }

  @Test
  void testCreateFinRecord() {
    this.cleanUp();
    this.prepareData();

    var journal = JOURNALS.get(0);
    var accountAssetId =
        journal.getAccounts().stream()
            .filter(a -> a.getType() == Account.Type.ASSET)
            .map(AbstractPersistable::getId)
            .findFirst()
            .orElseThrow();
    var accountLiabilityId =
        journal.getAccounts().stream()
            .filter(a -> a.getType() == Account.Type.LIABILITY)
            .map(AbstractPersistable::getId)
            .findFirst()
            .orElseThrow();

    journal.createFinRecord(
        USERS.get(User.Role.OWNER).get(0),
        "new record",
        "new description",
        Instant.EPOCH.plusSeconds(3600),
        false,
        Set.of(
            new FinRecordItemInput(
                accountAssetId,
                BigDecimal.valueOf(200),
                "NewBuyingUnit 1",
                BigDecimal.valueOf(300),
                "Note fore new record Asset"),
            new FinRecordItemInput(
                accountLiabilityId,
                BigDecimal.valueOf(400),
                "NewBuyingUnit 2",
                BigDecimal.valueOf(500),
                "Note fore new record Liability")),
        Set.of("tag1", "tag2"));

    this.journalRepository.save(journal);

    var finRecord = journal.getFinRecords().stream().findFirst().orElseThrow();
    var itemIds =
        finRecord.getItems().stream().map(AbstractPersistable::getId).collect(Collectors.toSet());
    Assertions.assertThat(journal.getFinRecords())
        .anySatisfy(
            record -> {
              Assertions.assertThat(record.getName()).isEqualTo("new record");
              Assertions.assertThat(record.getDescription()).isEqualTo("new description");
              Assertions.assertThat(record.getHappenedAt())
                  .isEqualTo(Instant.EPOCH.plusSeconds(3600));
              Assertions.assertThat(record.getItems())
                  .anySatisfy(
                      item -> {
                        Assertions.assertThat(item.getAccount().getId()).isEqualTo(accountAssetId);
                        Assertions.assertThat(item.getAmount()).isEqualTo(BigDecimal.valueOf(200));
                      });
              Assertions.assertThat(record.getItems())
                  .anySatisfy(
                      item -> {
                        Assertions.assertThat(item.getAccount().getId())
                            .isEqualTo(accountLiabilityId);
                        Assertions.assertThat(item.getAmount()).isEqualTo(BigDecimal.valueOf(400));
                      });
            });
    Assertions.assertThat(this.finRecordItemRepository.findAllById(itemIds)).isNotEmpty();

    journal.updateFinRecord(
        USERS.get(User.Role.OWNER).get(0), finRecord, null, null, null, null, Set.of(), null);
    this.journalRepository.save(journal);

    Assertions.assertThat(this.finRecordItemRepository.findAllById(itemIds)).isEmpty();
  }
}
