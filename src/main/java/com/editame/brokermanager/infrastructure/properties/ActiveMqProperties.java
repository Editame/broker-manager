package com.editame.brokermanager.infrastructure.properties;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@ConfigurationProperties(prefix = "spring.activemq")
@Component
public class ActiveMqProperties {
    private String brokerUrl;
    private String user;
    private String password;
}
