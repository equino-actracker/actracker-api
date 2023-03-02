package ovh.equino.actracker.main.springboot.configuration.messaging;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import ovh.equino.actracker.notification.outbox.NotificationPublisher;
import ovh.equino.actracker.publisher.rabbitmq.RabbitMqChannelFactory;
import ovh.equino.actracker.publisher.rabbitmq.RabbitMqConnectionProperties;

import static java.lang.Integer.parseInt;

@Configuration
@Profile("actracker-api-publisher-rabbitmq")
@ComponentScan(
        basePackages = "ovh.equino.actracker.publisher.rabbitmq",
        includeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        NotificationPublisher.class
                }
        )
)
class RabbitMqConfiguration {

    @Value("${actracker-api-publisher-rabbitmq.host:localhost}")
    private String host;

    @Value("${actracker-api-publisher-rabbitmq.port:5672}")
    private String port;

    @Value("${actracker-api-publisher-rabbitmq.vhost:/}")
    private String vhost;

    @Value("${actracker-api-publisher-rabbitmq.username:guest}")
    private String username;

    @Value("${actracker-api-publisher-rabbitmq.password:guest}")
    private String password;

    @Bean
    RabbitMqChannelFactory rabbitMqChannelFactory() {
        RabbitMqConnectionProperties connectionProperties =
                new RabbitMqConnectionProperties(host, parseInt(port), vhost, username, password);
        return new RabbitMqChannelFactory(connectionProperties);
    }
}
