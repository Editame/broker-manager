package com.editame.brokermanager.domain.dto;

import lombok.Builder;

@Builder
public record MessageGroupInfo(
    String groupId,
    int messageCount
) {}
