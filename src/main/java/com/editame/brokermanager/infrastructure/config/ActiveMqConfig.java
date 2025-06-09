package com.editame.brokermanager.infrastructure.config;

import com.editame.brokermanager.infrastructure.properties.ActiveMqProperties;
import jakarta.jms.ConnectionFactory;
import lombok.RequiredArgsConstructor;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class ActiveMqConfig {

    private final ActiveMqProperties activeMqProperties;

    @Bean
    public ConnectionFactory connectionFactory() {
        return new ActiveMQConnectionFactory(
                activeMqProperties.getUser(),
                activeMqProperties.getPassword(),
                activeMqProperties.getBrokerUrl()
        );
    }
}
