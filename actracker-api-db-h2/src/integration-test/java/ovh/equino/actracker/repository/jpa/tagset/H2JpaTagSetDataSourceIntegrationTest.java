package ovh.equino.actracker.repository.jpa.tagset;

import ovh.equino.actracker.repository.jpa.IntegrationTestH2DataBase;
import ovh.equino.actracker.repository.jpa.IntegrationTestRelationalDataBase;

public class H2JpaTagSetDataSourceIntegrationTest extends JpaTagSetRepositoryIntegrationTest {
    @Override
    protected IntegrationTestRelationalDataBase database() {
        return IntegrationTestH2DataBase.INSTANCE;
    }
}
