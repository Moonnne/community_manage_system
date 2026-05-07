package edu.tut.cms.common.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {
    int[] value(); // 0=系统管理员, 1=物业经理, 2=物业员工, 3=安保人员, 4=业主, 5=租户
}
