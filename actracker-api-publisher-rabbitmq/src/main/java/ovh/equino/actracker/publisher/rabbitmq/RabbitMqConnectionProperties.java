package ovh.equino.actracker.publisher.rabbitmq;

public record RabbitMqConnectionProperties(
        String host,
        int port,
        String vhost,
        String username,
        String password) {
}
