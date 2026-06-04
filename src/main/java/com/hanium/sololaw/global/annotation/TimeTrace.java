/* 
 * Copyright (c) HANIUM SOLOLAW 
 */
package com.hanium.sololaw.global.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.hanium.sololaw.global.aspect.TimeAspect;

/**
 * 메서드를 수행하는 데 걸리는 시간을 로그로 출력할 때 사용하는 어노테이션입니다. <br>
 * 이 어노테이션은 Method 부분에 선언하면 해당 메서드의 수행 시간을 로그로 출력합니다. <br>
 * 또한 실행 환경이 일치할 때만 로그를 출력하도록 설정할 수 있습니다. ex)local, dev, prod ... <br>
 * <br>
 * 사용 방법: <br>
 * {@code @TimeTrace(methodName = "...", env = {"..", ".."})} <br>
 * <br>
 * - methodName: 로그 출력에서 확인할 메서드명 <br>
 * - env: 로그 출력을 수행할 환경 (미 입력 시 모든 환경에서 출력) <br>
 *
 * @see TimeAspect
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TimeTrace {

  String methodName() default "";

  String[] env() default {};
}
