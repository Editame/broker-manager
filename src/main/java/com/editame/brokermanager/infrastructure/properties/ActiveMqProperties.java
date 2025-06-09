package com.editame.brokermanager.infrastructure.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "spring.activemq")
public class ActiveMqProperties {
    private String brokerUrl;
    private String user;
    private String password;
}
