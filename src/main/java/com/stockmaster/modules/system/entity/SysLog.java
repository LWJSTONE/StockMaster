package com.stockmaster.modules.system.entity;

import javax.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "sys_log")
public class SysLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "operation_type", length = 20)
    private String operationType;

    @Column(name = "module", length = 50)
    private String module;

    @Column(name = "description", length = 200)
    private String description;

    @Column(name = "method_name", length = 200)
    private String methodName;

    @Column(name = "request_method", length = 10)
    private String requestMethod;

    @Column(name = "request_url", length = 200)
    private String requestUrl;

    @Column(name = "request_params", columnDefinition = "text")
    private String requestParams;

    @Column(name = "response_data", columnDefinition = "text")
    private String responseData;

    @Column(name = "ip", length = 50)
    private String ip;

    @Column(name = "username", length = 50)
    private String username;

    @Column(name = "time")
    private Long time;

    @Column(name = "status")
    private Integer status;

    @Column(name = "error_msg", columnDefinition = "text")
    private String errorMsg;

    @Column(name = "create_time")
    private LocalDateTime createTime;
}
