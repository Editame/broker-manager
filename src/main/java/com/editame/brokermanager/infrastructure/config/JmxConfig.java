package com.editame.brokermanager.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class JmxConfig {

    @Bean
    public MBeanServerConnection mBeanServerConnection() throws Exception {
        String jmxUrl = "service:jmx:rmi:///jndi/rmi://localhost:1099/jmxrmi";
        Map<String, Object> env = new HashMap<>();
        String[] creds = {"admin", "admin"};
        env.put(JMXConnector.CREDENTIALS, creds);

        JMXConnector connector = JMXConnectorFactory.connect(new JMXServiceURL(jmxUrl), env);
        return connector.getMBeanServerConnection();
    }
}
