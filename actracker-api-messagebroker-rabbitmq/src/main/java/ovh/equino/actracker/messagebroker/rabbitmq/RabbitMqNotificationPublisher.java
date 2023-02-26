package ovh.equino.actracker.messagebroker.rabbitmq;

import com.rabbitmq.client.Channel;
import ovh.equino.actracker.notification.outbox.Notification;
import ovh.equino.actracker.notification.outbox.NotificationPublisher;

import java.io.IOException;

class RabbitMqNotificationPublisher implements NotificationPublisher {

    private final Channel channel;

    RabbitMqNotificationPublisher(RabbitMqChannelFactory channelFactory) {
        this.channel = channelFactory.createChannel();
        try {
            this.channel.queueDeclare("notification.Q", false, false, false, null);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void publishNotification(Notification notification) {
        try {
            String message = "Publishing notification with ID=%s to RabbitMQ%n".formatted(notification.id().toString());
            channel.basicPublish("", "notification.Q", null, message.getBytes());
            System.out.printf("Publishing notification with ID=%s to RabbitMQ%n", notification.id());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
