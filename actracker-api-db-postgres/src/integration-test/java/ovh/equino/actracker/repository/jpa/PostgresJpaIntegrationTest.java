package ovh.equino.actracker.repository.jpa;

public interface PostgresJpaIntegrationTest {

    IntegrationTestPostgres DATABASE = new IntegrationTestPostgres();

    default IntegrationTestRelationalDataBase database() {
        return DATABASE;
    }
}
