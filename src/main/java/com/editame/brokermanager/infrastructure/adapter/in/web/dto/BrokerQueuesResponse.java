package com.editame.brokermanager.infrastructure.adapter.in.web.dto;

import java.util.List;

public record BrokerQueuesResponse(
    List<QueueInfo> queues
) {}
