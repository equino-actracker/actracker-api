package ovh.equino.actracker.datasource.jpa.tagset;

import ovh.equino.actracker.jpa.IntegrationTestPostgresDataBase;
import ovh.equino.actracker.jpa.IntegrationTestRelationalDataBase;

class PostgresJpaTagSetDataSourceIntegrationTest extends JpaTagSetDataSourceIntegrationTest {

    @Override
    public IntegrationTestRelationalDataBase database() {
        return IntegrationTestPostgresDataBase.INSTANCE;
    }
}
