package com.editame.brokermanager.domain.dto;

import java.util.List;

public record BrokerQueuesResponse(
    List<QueueInfo> queues
) {}
