package ovh.equino.actracker.repository.jpa.tagset;

import ovh.equino.actracker.repository.jpa.IntegrationTestPostgresDataBase;
import ovh.equino.actracker.repository.jpa.IntegrationTestRelationalDataBase;

class PostgresJpaTagSetDataSourceIntegrationTest extends JpaTagSetDataSourceIntegrationTest {

    @Override
    public IntegrationTestRelationalDataBase database() {
        return IntegrationTestPostgresDataBase.INSTANCE;
    }
}
