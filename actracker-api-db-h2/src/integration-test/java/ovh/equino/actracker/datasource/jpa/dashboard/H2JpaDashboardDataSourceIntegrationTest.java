package ovh.equino.actracker.datasource.jpa.dashboard;

import ovh.equino.actracker.jpa.IntegrationTestH2DataBase;
import ovh.equino.actracker.jpa.IntegrationTestRelationalDataBase;

class H2JpaDashboardDataSourceIntegrationTest extends JpaDashboardDataSourceIntegrationTest {
    @Override
    protected IntegrationTestRelationalDataBase database() {
        return IntegrationTestH2DataBase.INSTANCE;
    }
}
