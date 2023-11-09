package ovh.equino.actracker.repository.jpa.activity;

import ovh.equino.actracker.repository.jpa.IntegrationTestRelationalDataBase;
import ovh.equino.actracker.repository.jpa.PostgresJpaIntegrationTest;

class PostgresJpaActivityRepositoryIntegrationTest
        extends JpaActivityRepositoryIntegrationTest
        implements PostgresJpaIntegrationTest {

    @Override
    public IntegrationTestRelationalDataBase database() {
        return PostgresJpaIntegrationTest.super.database();
    }
}
