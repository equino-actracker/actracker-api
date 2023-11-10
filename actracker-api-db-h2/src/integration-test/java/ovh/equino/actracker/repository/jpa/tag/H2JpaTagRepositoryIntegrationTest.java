package ovh.equino.actracker.repository.jpa.tag;

import ovh.equino.actracker.repository.jpa.IntegrationTestH2DataBase;
import ovh.equino.actracker.repository.jpa.IntegrationTestRelationalDataBase;

public class H2JpaTagRepositoryIntegrationTest extends JpaTagRepositoryIntegrationTest {
    @Override
    protected IntegrationTestRelationalDataBase database() {
        return IntegrationTestH2DataBase.INSTANCE;
    }
}
