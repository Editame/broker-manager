package com.editame.brokermanager.controller;

import com.editame.brokermanager.domain.dto.BrokerQueuesResponse;
import com.editame.brokermanager.service.BrokerAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
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
}
