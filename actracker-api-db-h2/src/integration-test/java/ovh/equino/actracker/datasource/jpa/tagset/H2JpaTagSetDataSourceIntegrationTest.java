package ovh.equino.actracker.datasource.jpa.tagset;

import ovh.equino.actracker.jpa.IntegrationTestH2DataBase;
import ovh.equino.actracker.jpa.IntegrationTestRelationalDataBase;

class H2JpaTagSetDataSourceIntegrationTest extends JpaTagSetDataSourceIntegrationTest {
    @Override
    protected IntegrationTestRelationalDataBase database() {
        return IntegrationTestH2DataBase.INSTANCE;
    }
}
