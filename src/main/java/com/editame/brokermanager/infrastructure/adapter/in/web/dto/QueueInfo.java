package com.editame.brokermanager.infrastructure.adapter.in.web.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record QueueInfo(
        String name,
        long queueSize,
        long enqueueCount,
        long dequeueCount,
        int consumerCount,
        List<com.editame.brokermanager.domain.dto.MessageGroupInfo> messageGroups// Puede ser null o empty si no se usa aún
) {}
