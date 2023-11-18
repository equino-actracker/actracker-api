package ovh.equino.actracker.repository.jpa.dashboard;

import org.junit.jupiter.api.Test;
import ovh.equino.actracker.repository.jpa.JpaIntegrationTest;

import static org.junit.jupiter.api.Assertions.fail;

abstract class JpaDashboardDataSourceIntegrationTest extends JpaIntegrationTest {

    @Test
    void shouldFindAccessibleDashboard() {
        fail();
    }

    @Test
    void shouldNotFindInaccessibleDashboard() {
        fail();
    }
}
