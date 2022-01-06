package com.ukonnra.wonderland.whiterabbit.core;

import com.ukonnra.wonderland.whiterabbit.core.domain.journal.AccountRepository;
import com.ukonnra.wonderland.whiterabbit.core.domain.journal.FinRecordItemRepository;
import com.ukonnra.wonderland.whiterabbit.core.domain.journal.Journal;
import com.ukonnra.wonderland.whiterabbit.core.domain.journal.JournalRepository;
import com.ukonnra.wonderland.whiterabbit.core.domain.journal.JournalService;
import com.ukonnra.wonderland.whiterabbit.core.domain.journal.entity.Account;
import com.ukonnra.wonderland.whiterabbit.core.domain.journal.entity.FinRecord;
import com.ukonnra.wonderland.whiterabbit.core.domain.journal.entity.FinRecordItem;
import com.ukonnra.wonderland.whiterabbit.core.domain.user.User;
import com.ukonnra.wonderland.whiterabbit.core.domain.user.UserRepository;
import com.ukonnra.wonderland.whiterabbit.core.domain.user.UserService;
import com.ukonnra.wonderland.whiterabbit.core.domain.user.valobj.Identifier;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
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

  public AbstractTest(
      UserService userService,
      UserRepository userRepository,
      JournalService journalService,
      JournalRepository journalRepository,
      AccountRepository accountRepository,
      FinRecordItemRepository finRecordItemRepository) {
    this.userService = userService;
    this.userRepository = userRepository;
    this.journalService = journalService;
    this.journalRepository = journalRepository;
    this.accountRepository = accountRepository;
    this.finRecordItemRepository = finRecordItemRepository;
  }

  protected final UserService userService;

  protected final UserRepository userRepository;

  protected final JournalService journalService;

  protected final JournalRepository journalRepository;

  protected final AccountRepository accountRepository;

  protected final FinRecordItemRepository finRecordItemRepository;

  protected static Map<User.Role, List<User>> USERS;

  protected static List<Journal> JOURNALS;

  protected static Map<Journal, Map<Account.Type, Account>> ACCOUNTS;

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

    var userDeleted = new User();
    userDeleted.setName("User deleted");
    userDeleted.setRole(User.Role.USER);
    userDeleted.setDeleted(true);
    userDeleted.setIdentifiers(
        Set.of(new Identifier(Identifier.Type.AUTHING, "authing|userDeleted")));

    USERS =
        Map.of(
            User.Role.OWNER,
            List.of(owner, owner1),
            User.Role.ADMIN,
            List.of(admin, admin1),
            User.Role.USER,
            List.of(user, user1, userDeleted));
    this.userRepository.saveAll(USERS.values().stream().flatMap(Collection::stream).toList());

    var ownerJournal = new Journal();
    ownerJournal.setName("Owner Journal");
    ownerJournal.setAdmins(Set.of(owner));
    ownerJournal.setMembers(Set.of(admin, user));
    ownerJournal.setAccounts(
        Set.of(
            new Account(
                "Owner Journal Account Asset",
                "Description about Owner Journal Account Asset",
                Account.Type.ASSET,
                "USD",
                Account.InventoryType.AVERAGE,
                ownerJournal),
            new Account(
                "Owner Journal Account Liability",
                "Description about Owner Journal Account Liability",
                Account.Type.LIABILITY,
                "USD",
                Account.InventoryType.AVERAGE,
                ownerJournal)));
    ownerJournal.setFinRecords(
        Set.of(
            new FinRecord(
                ownerJournal,
                "Owner Account Asset: Record 1",
                "Description about Owner Account Asset: Record 1",
                Instant.EPOCH,
                true,
                Set.of(
                    new FinRecordItem(
                        ownerJournal.getAccounts().stream()
                            .filter(a -> a.getType() == Account.Type.ASSET)
                            .findFirst()
                            .get(),
                        null,
                        BigDecimal.valueOf(100),
                        null,
                        null,
                        null),
                    new FinRecordItem(
                        ownerJournal.getAccounts().stream()
                            .filter(a -> a.getType() == Account.Type.LIABILITY)
                            .findFirst()
                            .get(),
                        null,
                        BigDecimal.valueOf(100),
                        null,
                        null,
                        null)),
                Set.of("record:tag"))));

    var adminJournal = new Journal();
    adminJournal.setName("Admin Journal");
    adminJournal.setAdmins(Set.of(admin));
    adminJournal.setMembers(Set.of(user));
    adminJournal.setAccounts(
        Set.of(
            new Account(
                "Admin Journal Account Equity",
                "Description about Admin Journal Account Equity",
                Account.Type.EQUITY,
                "RMB",
                Account.InventoryType.FIFO,
                adminJournal)));

    var userJournal = new Journal();
    userJournal.setName("User Journal");
    userJournal.setAdmins(Set.of(user));

    var deletedJournal = new Journal();
    deletedJournal.setName("Deleted Journal");
    deletedJournal.setAdmins(Set.of(user));
    deletedJournal.setDeleted(true);

    JOURNALS = List.of(ownerJournal, adminJournal, userJournal, deletedJournal);
    ACCOUNTS =
        JOURNALS.stream()
            .collect(
                Collectors.toMap(
                    Function.identity(),
                    j ->
                        j.getAccounts().stream()
                            .collect(Collectors.toMap(Account::getType, Function.identity()))));

    this.journalRepository.saveAll(JOURNALS);
  }

  protected void cleanUp() {
    this.journalRepository.deleteAll();
    this.userRepository.deleteAll();
  }
}
