package com.editame.brokermanager.controller;

import com.editame.brokermanager.domain.dto.BrokerQueuesResponse;
import com.editame.brokermanager.domain.dto.MessageInfo;
import com.editame.brokermanager.domain.dto.SendMessageRequest;
import com.editame.brokermanager.service.BrokerAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Log4j2
@RestController
@RequestMapping("api/queues")
@RequiredArgsConstructor
public class QueueController {

    private final BrokerAdminService brokerAdminService;
    
    @GetMapping
    public ResponseEntity<BrokerQueuesResponse> listQueues() {
        log.info("Listing queues");
        BrokerQueuesResponse brokerQueuesResponse = brokerAdminService.getAllQueuesInfo();
        return ResponseEntity.ok(brokerQueuesResponse);
    }
    
    @GetMapping("/{queueName}/messages")
    public ResponseEntity<List<MessageInfo>> getQueueMessages(@PathVariable String queueName) {
        log.info("Getting messages for queue: {}", queueName);
        List<MessageInfo> messages = brokerAdminService.getQueueMessages(queueName);
        return ResponseEntity.ok(messages);
    }
    
    @PostMapping("/{queueName}/messages")
    public ResponseEntity<String> sendMessage(@PathVariable String queueName, @RequestBody SendMessageRequest messageRequest) {
        log.info("Sending message to queue: {}", queueName);
        brokerAdminService.sendMessage(queueName, messageRequest);
        return ResponseEntity.ok("Message sent successfully to queue: " + queueName);
    }
}
