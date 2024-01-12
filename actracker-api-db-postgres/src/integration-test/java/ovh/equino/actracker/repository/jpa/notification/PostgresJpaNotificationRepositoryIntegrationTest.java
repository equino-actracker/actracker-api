package ovh.equino.actracker.repository.jpa.notification;

import ovh.equino.actracker.repository.jpa.IntegrationTestPostgresDataBase;
import ovh.equino.actracker.jpa.IntegrationTestRelationalDataBase;

class PostgresJpaNotificationRepositoryIntegrationTest extends JpaNotificationRepositoryIntegrationTest {
    @Override
    public IntegrationTestRelationalDataBase database() {
        return IntegrationTestPostgresDataBase.INSTANCE;
    }
}
