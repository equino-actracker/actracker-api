package ovh.equino.actracker.repository.jpa.notification;

import ovh.equino.actracker.repository.jpa.IntegrationTestPostgresDataBase;
import ovh.equino.actracker.repository.jpa.IntegrationTestRelationalDataBase;

class PostgresJpaNotificationDataSourceIntegrationTest extends JpaNotificationDataSourceIntegrationTest {
    @Override
    protected IntegrationTestRelationalDataBase database() {
        return IntegrationTestPostgresDataBase.INSTANCE;
    }
}
