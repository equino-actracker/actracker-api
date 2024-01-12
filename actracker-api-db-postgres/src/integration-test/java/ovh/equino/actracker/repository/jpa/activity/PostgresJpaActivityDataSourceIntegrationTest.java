package ovh.equino.actracker.repository.jpa.activity;

import ovh.equino.actracker.repository.jpa.IntegrationTestPostgresDataBase;
import ovh.equino.actracker.jpa.IntegrationTestRelationalDataBase;

class PostgresJpaActivityDataSourceIntegrationTest extends JpaActivityDataSourceIntegrationTest {

    @Override
    public IntegrationTestRelationalDataBase database() {
        return IntegrationTestPostgresDataBase.INSTANCE;
    }
}
