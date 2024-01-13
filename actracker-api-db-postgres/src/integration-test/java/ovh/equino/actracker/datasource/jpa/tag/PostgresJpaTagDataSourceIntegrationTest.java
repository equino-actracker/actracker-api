package ovh.equino.actracker.datasource.jpa.tag;

import ovh.equino.actracker.jpa.IntegrationTestPostgresDataBase;
import ovh.equino.actracker.jpa.IntegrationTestRelationalDataBase;

class PostgresJpaTagDataSourceIntegrationTest extends JpaTagDataSourceIntegrationTest {
    @Override
    public IntegrationTestRelationalDataBase database() {
        return IntegrationTestPostgresDataBase.INSTANCE;
    }

}
