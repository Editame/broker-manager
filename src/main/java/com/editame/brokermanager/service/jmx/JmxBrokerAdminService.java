package com.editame.brokermanager.service.jmx;

import com.editame.brokermanager.domain.dto.BrokerQueuesResponse;
import com.editame.brokermanager.domain.dto.QueueInfo;
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
    public BrokerQueuesResponse getAllQueuesInfo() {
        List<QueueInfo> queueInfos = new ArrayList<>();

        try {
            Set<ObjectName> queueMBeans = mBeanServerConnection.queryNames(
                    new ObjectName("org.apache.activemq:type=Broker,brokerName=*,destinationType=Queue,destinationName=*"),
                    null
            );

            for (ObjectName objectName : queueMBeans) {
                String name = (String) mBeanServerConnection.getAttribute(objectName, "Name");
                long queueSize = (Long) mBeanServerConnection.getAttribute(objectName, "QueueSize");
                long enqueueCount = (Long) mBeanServerConnection.getAttribute(objectName, "EnqueueCount");
                long dequeueCount = (Long) mBeanServerConnection.getAttribute(objectName, "DequeueCount");
                int consumerCount = ((Long) mBeanServerConnection.getAttribute(objectName, "ConsumerCount")).intValue();

                // En esta etapa aún no traemos los grupos
                QueueInfo queueInfo = QueueInfo.builder()
                        .name(name)
                        .queueSize(queueSize)
                        .enqueueCount(enqueueCount)
                        .dequeueCount(dequeueCount)
                        .consumerCount(consumerCount)
                        .build();

                queueInfos.add(queueInfo);
            }

        } catch (Exception e) {
            throw new RuntimeException("Error retrieving queues via JMX", e);
        }

        return new BrokerQueuesResponse(queueInfos);
    }
}
