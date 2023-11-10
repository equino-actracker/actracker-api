package ovh.equino.actracker.repository.jpa.activity;

import ovh.equino.actracker.repository.jpa.IntegrationTestPostgresDataBase;
import ovh.equino.actracker.repository.jpa.IntegrationTestRelationalDataBase;

class PostgresJpaActivityRepositoryIntegrationTest extends JpaActivityRepositoryIntegrationTest {

    @Override
    public IntegrationTestRelationalDataBase database() {
        return IntegrationTestPostgresDataBase.INSTANCE;
    }
}
