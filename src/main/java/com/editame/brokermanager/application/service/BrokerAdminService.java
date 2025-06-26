package com.editame.brokermanager.application.service;

import com.editame.brokermanager.domain.dto.MessageInfo;
import com.editame.brokermanager.infrastructure.adapter.in.web.dto.BrokerQueuesResponse;
import com.editame.brokermanager.infrastructure.adapter.in.web.dto.SendMessageRequest;

import java.util.List;

public interface BrokerAdminService {
    BrokerQueuesResponse getAllQueuesInfo();
    List<MessageInfo> getQueueMessages(String queueName);
    void sendMessage(String queueName, SendMessageRequest messageRequest);
}
