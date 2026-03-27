package com.stockmaster.common.aop;

import com.stockmaster.common.enums.OperationType;
import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogOperation {

    OperationType value() default OperationType.OTHER;

    String module() default "";

    String description() default "";
}
