package ovh.equino.actracker.datasource.jpa.dashboard;

import ovh.equino.actracker.jpa.IntegrationTestPostgresDataBase;
import ovh.equino.actracker.jpa.IntegrationTestRelationalDataBase;

class PostgresJpaDashboardDataSourceIntegrationTest extends JpaDashboardDataSourceIntegrationTest {
    @Override
    protected IntegrationTestRelationalDataBase database() {
        return IntegrationTestPostgresDataBase.INSTANCE;
    }
}
