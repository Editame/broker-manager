package com.editame.brokermanager.service;

import com.editame.brokermanager.domain.exception.ActiveMqException;

import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;
import jakarta.jms.QueueBrowser;
import jakarta.jms.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.activemq.Message;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

@Log4j2
@Service
@RequiredArgsConstructor
public class ActiveMqService {

    private final ConnectionFactory connectionFactory;

    public List<String> listQueues() {
        List<String> colas = new ArrayList<>();
        try (Connection connection = connectionFactory.createConnection()) {
            connection.start();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            QueueBrowser browser = session.createBrowser(null);
            Enumeration<?> queues = browser.getEnumeration();
            while (queues.hasMoreElements()) {
                Message msg = (Message) queues.nextElement();
                colas.add(msg.getJMSDestination().toString());
            }
        } catch (JMSException e) {
            log.error("Error al listar colas", e);
            throw new ActiveMqException("Error al list queues", e);
        }
        return colas;
    }
}
