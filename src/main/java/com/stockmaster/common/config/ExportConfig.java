package com.stockmaster.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "export")
public class ExportConfig {

    private String tempPath = "/tmp/export";
    private Integer maxRows = 100000;
    private Integer batchSize = 1000;
    private String dateFormat = "yyyy-MM-dd";
    private String dateTimeFormat = "yyyy-MM-dd HH:mm:ss";
}
