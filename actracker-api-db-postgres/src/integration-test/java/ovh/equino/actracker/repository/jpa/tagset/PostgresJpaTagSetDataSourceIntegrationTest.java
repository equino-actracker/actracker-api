package ovh.equino.actracker.repository.jpa.tagset;

import ovh.equino.actracker.repository.jpa.IntegrationTestRelationalDataBase;
import ovh.equino.actracker.repository.jpa.PostgresJpaIntegrationTest;

class PostgresJpaTagSetDataSourceIntegrationTest
        extends JpaTagSetDataSourceIntegrationTest
        implements PostgresJpaIntegrationTest {

    @Override
    public IntegrationTestRelationalDataBase database() {
        return PostgresJpaIntegrationTest.super.database();
    }
}
