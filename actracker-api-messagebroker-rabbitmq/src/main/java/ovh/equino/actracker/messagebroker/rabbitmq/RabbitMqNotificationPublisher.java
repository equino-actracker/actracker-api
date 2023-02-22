package ovh.equino.actracker.messagebroker.rabbitmq;

import ovh.equino.actracker.notification.outbox.Notification;
import ovh.equino.actracker.notification.outbox.NotificationPublisher;

class RabbitMqNotificationPublisher implements NotificationPublisher {

    @Override
    public void publishNotification(Notification notification) {
        System.out.printf("Publishing notification with ID=%s%n", notification.id());
    }
}
