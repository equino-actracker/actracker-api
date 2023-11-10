package ovh.equino.actracker.repository.jpa;

import org.h2.jdbcx.JdbcDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public final class IntegrationTestH2DataBase extends IntegrationTestRelationalDataBase {

    public static final IntegrationTestH2DataBase INSTANCE = new IntegrationTestH2DataBase();

    private final JdbcDataSource dataSource;

    private IntegrationTestH2DataBase() {
        dataSource = new JdbcDataSource();
        dataSource.setUrl(jdbcUrl());
        dataSource.setUser(username());
        dataSource.setPassword(password());
        try (PreparedStatement preparedStatement = dataSource.getConnection().prepareStatement("runscript from 'classpath:h2Schema.sql'")) {
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public String jdbcUrl() {
        return "jdbc:h2:mem:test";
    }

    @Override
    public String username() {
        return "sa";
    }

    @Override
    public String password() {
        return "sa";
    }

    @Override
    public String driverClassName() {
        return "org.h2.Driver";
    }
}
