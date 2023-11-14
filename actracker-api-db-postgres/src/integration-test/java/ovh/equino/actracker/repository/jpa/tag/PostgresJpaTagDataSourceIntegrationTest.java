package ovh.equino.actracker.repository.jpa.tag;

import ovh.equino.actracker.repository.jpa.IntegrationTestPostgresDataBase;
import ovh.equino.actracker.repository.jpa.IntegrationTestRelationalDataBase;

class PostgresJpaTagDataSourceIntegrationTest extends JpaTagDataSourceIntegrationTest {
    @Override
    public IntegrationTestRelationalDataBase database() {
        return IntegrationTestPostgresDataBase.INSTANCE;
    }

}
