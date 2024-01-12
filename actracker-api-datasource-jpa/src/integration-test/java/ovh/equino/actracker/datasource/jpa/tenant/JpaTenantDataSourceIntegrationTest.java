package ovh.equino.actracker.datasource.jpa.tenant;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ovh.equino.actracker.domain.tenant.TenantDto;
import ovh.equino.actracker.jpa.IntegrationTestConfiguration;
import ovh.equino.actracker.jpa.JpaIntegrationTest;

import java.sql.SQLException;
import java.util.Optional;
import java.util.stream.Stream;

abstract class JpaTenantDataSourceIntegrationTest extends JpaIntegrationTest {

    private static final IntegrationTestConfiguration testConfiguration = new IntegrationTestConfiguration();
    private JpaTenantDataSource dataSource;

    @BeforeEach
    void init() throws SQLException {
        this.dataSource = new JpaTenantDataSource(entityManager);
        testConfiguration.persistIn(database());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("existingUsers")
    void shouldFindExistingTenantByUsername(String username, TenantDto expectedUser) {
        inTransaction(() -> {
            Optional<TenantDto> foundUser = dataSource.findByUsername(username);
            Assertions.assertThat(foundUser).isPresent();
            Assertions.assertThat(foundUser.get()).usingRecursiveComparison().isEqualTo(expectedUser);
        });
    }

    private static Stream<Arguments> existingUsers() {
        return testConfiguration.existingUsers()
                .stream()
                .map(user -> Arguments.of(user.username(), user));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("nonExistingUsers")
    void shouldNotFindNonExistingTenantByUsername(String username) {
        inTransaction(() -> {
            Optional<TenantDto> foundUser = dataSource.findByUsername(username);
            Assertions.assertThat(foundUser).isNotPresent();
        });
    }

    private static Stream<Arguments> nonExistingUsers() {
        return testConfiguration.nonExistingUsers()
                .stream()
                .map(user -> Arguments.of(user.username()));
    }

    @BeforeAll
    static void setUp() {
        testConfiguration.addUser(JpaIntegrationTest.newUser().named("existingUser1").build());
        testConfiguration.addUser(JpaIntegrationTest.newUser().named("existingUser2").build());
        testConfiguration.addTransientUser(JpaIntegrationTest.newUser().named("nonExistingUser").build());
    }
}
