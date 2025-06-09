package com.editame.brokermanager.controller;

import com.editame.brokermanager.service.ActiveMqService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/queues")
@RequiredArgsConstructor
public class QueueController {

    private final ActiveMqService activeMqService;

    @GetMapping
    public ResponseEntity<List<String>> listQueues() {
        List<String> queues = activeMqService.listQueues();
        return ResponseEntity.ok(queues);
    }
}
