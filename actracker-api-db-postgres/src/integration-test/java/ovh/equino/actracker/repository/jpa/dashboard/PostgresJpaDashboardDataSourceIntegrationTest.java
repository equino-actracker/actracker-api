package ovh.equino.actracker.repository.jpa.dashboard;

import ovh.equino.actracker.repository.jpa.IntegrationTestPostgresDataBase;
import ovh.equino.actracker.repository.jpa.IntegrationTestRelationalDataBase;

class PostgresJpaDashboardDataSourceIntegrationTest extends JpaDashboardDataSourceIntegrationTest {
    @Override
    protected IntegrationTestRelationalDataBase database() {
        return IntegrationTestPostgresDataBase.INSTANCE;
    }
}
