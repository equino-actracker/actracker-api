package ovh.equino.actracker.repository.jpa.tenant;

import org.junit.jupiter.api.Test;
import ovh.equino.actracker.repository.jpa.JpaIntegrationTest;

import static org.junit.jupiter.api.Assertions.fail;

abstract class JpaTenantDataSourceIntegrationTest extends JpaIntegrationTest {

    @Test
    void shouldFindExistingTenantByUsername() {
        fail();
    }

    @Test
    void shouldNotFindNonExistingTenantByUsername() {
        fail();
    }
}
