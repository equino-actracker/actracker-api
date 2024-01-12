package ovh.equino.actracker.repository.jpa.tag;

import ovh.equino.actracker.repository.jpa.IntegrationTestPostgresDataBase;
import ovh.equino.actracker.jpa.IntegrationTestRelationalDataBase;

class PostgresJpaTagRepositoryIntegrationTest extends JpaTagRepositoryIntegrationTest {

    @Override
    public IntegrationTestRelationalDataBase database() {
        return IntegrationTestPostgresDataBase.INSTANCE;
    }
}
