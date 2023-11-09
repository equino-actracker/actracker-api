package ovh.equino.actracker.repository.jpa.tag;

import ovh.equino.actracker.repository.jpa.IntegrationTestRelationalDataBase;
import ovh.equino.actracker.repository.jpa.PostgresJpaIntegrationTest;

class PostgresJpaTagRepositoryIntegrationTest
        extends JpaTagRepositoryIntegrationTest
        implements PostgresJpaIntegrationTest {

    @Override
    public IntegrationTestRelationalDataBase database() {
        return PostgresJpaIntegrationTest.super.database();
    }
}
