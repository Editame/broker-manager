package com.editame.brokermanager.infrastructure.config;

import com.editame.brokermanager.infrastructure.properties.ActiveMqProperties;
import jakarta.jms.ConnectionFactory;
import lombok.RequiredArgsConstructor;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ActiveMqConfig {

    private final ActiveMqProperties activeMqProperties;

    @Bean
    @ConditionalOnProperty(name = "broker.connection.enabled", havingValue = "true")
    public ConnectionFactory connectionFactory() {
        return new ActiveMQConnectionFactory(
                activeMqProperties.getUser(),
                activeMqProperties.getPassword(),
                activeMqProperties.getBrokerUrl()
        );
    }
}
