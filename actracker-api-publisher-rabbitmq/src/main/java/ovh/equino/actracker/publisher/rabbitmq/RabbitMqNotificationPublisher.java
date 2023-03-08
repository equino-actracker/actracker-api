package ovh.equino.actracker.publisher.rabbitmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;
import ovh.equino.actracker.domain.activity.ActivityChangedNotification;
import ovh.equino.actracker.notification.outbox.NotificationPublisher;

import java.io.IOException;

import static com.rabbitmq.client.BuiltinExchangeType.TOPIC;

class RabbitMqNotificationPublisher implements NotificationPublisher {

    public static final String EXCHANGE_NAME = "notification.X.topic";
    private final Channel channel;
    private final ObjectMapper objectMapper;

    RabbitMqNotificationPublisher(RabbitMqChannelFactory channelFactory) {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.channel = channelFactory.createChannel();
        try {
            boolean durable = true;
            this.channel.exchangeDeclare(EXCHANGE_NAME, TOPIC, durable);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void publishNotification(ActivityChangedNotification changedNotification) {
        try {
            String message = objectMapper.writeValueAsString(changedNotification);
            channel.basicPublish(
                    EXCHANGE_NAME,
                    ActivityChangedNotification.class.getSimpleName(),
                    MessageProperties.PERSISTENT_TEXT_PLAIN,
                    message.getBytes()
            );
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
