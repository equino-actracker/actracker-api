package ovh.equino.actracker.messagebroker.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;

public class RabbitMqChannelFactory {

    private final Connection connection;

    public RabbitMqChannelFactory() {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");
        connectionFactory.setPort(5672);
        connectionFactory.setUsername("guest");
        connectionFactory.setPassword("guest");
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
