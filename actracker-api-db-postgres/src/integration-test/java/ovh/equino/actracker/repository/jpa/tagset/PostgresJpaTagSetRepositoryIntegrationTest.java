package ovh.equino.actracker.repository.jpa.tagset;

import ovh.equino.actracker.repository.jpa.IntegrationTestRelationalDataBase;
import ovh.equino.actracker.repository.jpa.PostgresJpaIntegrationTest;

class PostgresJpaTagSetRepositoryIntegrationTest
        extends JpaTagSetRepositoryIntegrationTest
        implements PostgresJpaIntegrationTest {

    @Override
    public IntegrationTestRelationalDataBase database() {
        return PostgresJpaIntegrationTest.super.database();
    }
}
