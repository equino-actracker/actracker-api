package ovh.equino.actracker.repository.jpa.dashboard;

import ovh.equino.actracker.repository.jpa.IntegrationTestRelationalDataBase;
import ovh.equino.actracker.repository.jpa.PostgresJpaIntegrationTest;

class PostgresJpaDashboardRepositoryIntegrationTest
        extends JpaDashboardRepositoryIntegrationTest
        implements PostgresJpaIntegrationTest {


    @Override
    public IntegrationTestRelationalDataBase database() {
        return PostgresJpaIntegrationTest.super.database();
    }
}
