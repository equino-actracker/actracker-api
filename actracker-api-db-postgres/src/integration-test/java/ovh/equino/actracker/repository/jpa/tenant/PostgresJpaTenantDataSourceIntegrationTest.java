package ovh.equino.actracker.repository.jpa.tenant;

import ovh.equino.actracker.repository.jpa.IntegrationTestPostgresDataBase;
import ovh.equino.actracker.jpa.IntegrationTestRelationalDataBase;

class PostgresJpaTenantDataSourceIntegrationTest extends JpaTenantDataSourceIntegrationTest {

    @Override
    public IntegrationTestRelationalDataBase database() {
        return IntegrationTestPostgresDataBase.INSTANCE;
    }
}
