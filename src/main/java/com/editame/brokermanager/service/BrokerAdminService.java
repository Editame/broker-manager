package com.editame.brokermanager.service;

import com.editame.brokermanager.domain.dto.BrokerQueuesResponse;
import com.editame.brokermanager.domain.dto.MessageInfo;
import com.editame.brokermanager.domain.dto.SendMessageRequest;

import java.util.List;

public interface BrokerAdminService {
    BrokerQueuesResponse getAllQueuesInfo();
    List<MessageInfo> getQueueMessages(String queueName);
    void sendMessage(String queueName, SendMessageRequest messageRequest);
}
