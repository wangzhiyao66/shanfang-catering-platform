package com.ordering.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ordering.admin")
public class AdminProperties {
    private String username;
    private String password;
}
