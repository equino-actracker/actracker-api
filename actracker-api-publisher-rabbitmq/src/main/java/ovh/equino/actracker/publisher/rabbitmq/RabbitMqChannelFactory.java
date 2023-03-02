package ovh.equino.actracker.publisher.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;

public class RabbitMqChannelFactory {

    private final Connection connection;

    public RabbitMqChannelFactory(RabbitMqConnectionProperties connectionProperties) {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(connectionProperties.host());
        connectionFactory.setVirtualHost(connectionProperties.vhost());
        connectionFactory.setPort(connectionProperties.port());
        connectionFactory.setUsername(connectionProperties.username());
        connectionFactory.setPassword(connectionProperties.password());

        try {
            this.connection = connectionFactory.newConnection();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    Channel createChannel() {
        try {
            return this.connection.createChannel();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
