package ovh.equino.actracker.repository.jpa.dashboard;

import ovh.equino.actracker.repository.jpa.IntegrationTestPostgresDataBase;
import ovh.equino.actracker.repository.jpa.IntegrationTestRelationalDataBase;

class PostgresJpaDashboardRepositoryIntegrationTest extends JpaDashboardRepositoryIntegrationTest {


    @Override
    public IntegrationTestRelationalDataBase database() {
        return IntegrationTestPostgresDataBase.INSTANCE;
    }
}
