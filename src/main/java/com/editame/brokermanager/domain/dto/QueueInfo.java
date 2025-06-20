package com.editame.brokermanager.domain.dto;

import lombok.Builder;
import java.util.List;

@Builder
public record QueueInfo(
    String name,
    long queueSize,
    long enqueueCount,
    long dequeueCount,
    int consumerCount,
    List<MessageGroupInfo> messageGroups // Puede ser null o empty si no se usa aún
) {}
