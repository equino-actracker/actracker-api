package ovh.equino.actracker.publisher.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;
import ovh.equino.actracker.domain.Notification;
import ovh.equino.actracker.domain.exception.ParseException;
import ovh.equino.actracker.notification.outbox.NotificationPublisher;

import java.io.IOException;

import static com.rabbitmq.client.BuiltinExchangeType.TOPIC;

class RabbitMqNotificationPublisher implements NotificationPublisher {

    public static final String EXCHANGE_NAME = "notification.X.topic";
    private final Channel channel;

    RabbitMqNotificationPublisher(RabbitMqChannelFactory channelFactory) {
        this.channel = channelFactory.createChannel();
        try {
            boolean durable = true;
            this.channel.exchangeDeclare(EXCHANGE_NAME, TOPIC, durable);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void publishNotification(Notification<?> notification) {
        try {
            String message = notification.toJson();
            channel.basicPublish(
                    EXCHANGE_NAME,
                    notification.notificationType().getCanonicalName(),
                    MessageProperties.PERSISTENT_TEXT_PLAIN,
                    message.getBytes()
            );
        } catch (IOException | ParseException e) {
            throw new IllegalStateException(e);
        }
    }
}
