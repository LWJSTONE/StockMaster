package com.stockmaster.common.aop;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stockmaster.common.security.SecurityUtils;
import com.stockmaster.modules.system.entity.SysLog;
import com.stockmaster.modules.system.service.SysLogService;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LogAspect {

    private final SysLogService sysLogService;
    private final ObjectMapper objectMapper;

    @Pointcut("@annotation(com.stockmaster.common.aop.LogOperation)")
    public void logOperationPointcut() {}

    @Around("logOperationPointcut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long beginTime = System.currentTimeMillis();
        Object result = null;
        Exception exception = null;

        try {
            result = point.proceed();
            return result;
        } catch (Exception e) {
            exception = e;
            throw e;
        } finally {
            saveLog(point, result, exception, System.currentTimeMillis() - beginTime);
        }
    }

    private void saveLog(ProceedingJoinPoint joinPoint, Object result, Exception exception, long time) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            LogOperation logOperation = method.getAnnotation(LogOperation.class);

            SysLog sysLog = new SysLog();
            sysLog.setOperationType(logOperation.value().name());
            sysLog.setModule(logOperation.module());
            sysLog.setDescription(logOperation.description());
            sysLog.setMethodName(signature.getDeclaringTypeName() + "." + signature.getName());
            sysLog.setRequestMethod(getRequestMethod());
            sysLog.setRequestUrl(getRequestUrl());
            sysLog.setRequestParams(getRequestParams(joinPoint));
            sysLog.setIp(getIpAddress());
            sysLog.setUsername(SecurityUtils.getCurrentUsername());
            sysLog.setTime(time);
            sysLog.setCreateTime(LocalDateTime.now());
            sysLog.setStatus(exception == null ? 1 : 0);
            sysLog.setErrorMsg(exception != null ? exception.getMessage() : null);

            sysLogService.saveLog(sysLog);
        } catch (Exception e) {
            log.error("保存操作日志失败", e);
        }
    }

    private String getRequestMethod() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            return attributes.getRequest().getMethod();
        }
        return null;
    }

    private String getRequestUrl() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            return attributes.getRequest().getRequestURI();
        }
        return null;
    }

    private String getRequestParams(ProceedingJoinPoint joinPoint) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String[] paramNames = signature.getParameterNames();
            Object[] paramValues = joinPoint.getArgs();

            Map<String, Object> params = new HashMap<>();
            for (int i = 0; i < paramNames.length; i++) {
                if (paramValues[i] instanceof MultipartFile) {
                    params.put(paramNames[i], "MultipartFile");
                } else {
                    params.put(paramNames[i], paramValues[i]);
                }
            }

            return objectMapper.writeValueAsString(params);
        } catch (Exception e) {
            return null;
        }
    }

    private String getIpAddress() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getHeader("X-Real-IP");
            }
            if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
            }
            return ip;
        }
        return null;
    }
}
