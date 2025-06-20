package com.editame.brokermanager.infrastructure.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class DynamicJmxConfig {

    public MBeanServerConnection createConnection(String host, int port, String username, String password) throws Exception {
        String jmxUrl = String.format("service:jmx:rmi:///jndi/rmi://%s:%d/jmxrmi", host, port);
        Map<String, Object> env = new HashMap<>();
        String[] creds = {username, password};
        env.put(JMXConnector.CREDENTIALS, creds);

        JMXConnector connector = JMXConnectorFactory.connect(new JMXServiceURL(jmxUrl), env);
        return connector.getMBeanServerConnection();
    }
}