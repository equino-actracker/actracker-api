package ovh.equino.actracker.messagebroker.rabbitmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rabbitmq.client.Channel;
import ovh.equino.actracker.notification.outbox.Notification;
import ovh.equino.actracker.notification.outbox.NotificationPublisher;

import java.io.IOException;

class RabbitMqNotificationPublisher implements NotificationPublisher {

    private final Channel channel;
    private final ObjectMapper objectMapper;

    RabbitMqNotificationPublisher(RabbitMqChannelFactory channelFactory) {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
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
            String message = objectMapper.writeValueAsString(notification.entity());
            channel.basicPublish("", "notification.Q", null, message.getBytes());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
