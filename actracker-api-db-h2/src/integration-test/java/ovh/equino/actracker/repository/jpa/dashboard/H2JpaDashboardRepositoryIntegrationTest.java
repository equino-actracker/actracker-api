package ovh.equino.actracker.repository.jpa.dashboard;

import ovh.equino.actracker.repository.jpa.IntegrationTestH2DataBase;
import ovh.equino.actracker.jpa.IntegrationTestRelationalDataBase;

class H2JpaDashboardRepositoryIntegrationTest extends JpaDashboardRepositoryIntegrationTest {
    @Override
    protected IntegrationTestRelationalDataBase database() {
        return IntegrationTestH2DataBase.INSTANCE;
    }
}
