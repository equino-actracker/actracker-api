package ovh.equino.actracker.repository.jpa.tag;

import ovh.equino.actracker.jpa.IntegrationTestRelationalDataBase;
import ovh.equino.actracker.repository.jpa.IntegrationTestPostgresDataBase;

class PostgresJpaTagDataSourceIntegrationTest extends JpaTagDataSourceIntegrationTest {
    @Override
    public IntegrationTestRelationalDataBase database() {
        return IntegrationTestPostgresDataBase.INSTANCE;
    }

}
