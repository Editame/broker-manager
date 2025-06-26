package com.editame.brokermanager.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageInfo {
    private String id;
    private String timestamp;
    private String body;
    private Map<String, Object> headers;
    private String type;
    private Integer priority;
    private Long expiration;
}
