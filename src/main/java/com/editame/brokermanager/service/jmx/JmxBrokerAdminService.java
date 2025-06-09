package com.editame.brokermanager.service.jmx;

import com.editame.brokermanager.service.BrokerAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class JmxBrokerAdminService implements BrokerAdminService {

    private final MBeanServerConnection mBeanServerConnection;

    @Override
    public List<String> listQueues() {
        List<String> queues = new ArrayList<>();
        try {
            Set<ObjectName> mbeans = mBeanServerConnection.queryNames(
                    new ObjectName("org.apache.activemq:type=Broker,brokerName=*,destinationType=Queue,destinationName=*"),
                    null
            );

            for (ObjectName name : mbeans) {
                queues.add(name.getKeyProperty("destinationName"));
            }

        } catch (Exception e) {
            throw new RuntimeException("Error listing queues via JMX", e);
        }

        return queues;
    }
}
