package com.ourrainwater.advice;

import com.ourrainwater.controller.Code;
import com.ourrainwater.controller.ResponseResult;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;

@Component
@Aspect
public class LogAdvice {
    @Pointcut("execution(* *..*Service.*(..))")
    public void pt() {
    }

    @After("pt()")
    public void log(JoinPoint jp) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream("src/main/resources/static/pages/logs", true));

        LocalDateTime now = LocalDateTime.now();
        StringBuilder sb = new StringBuilder()
                .append(now).append("\t")
                .append(Arrays.toString(jp.getArgs())).append("\t")
                .append(jp.getSignature().getName()).append(System.lineSeparator());
        bos.write(sb.toString().getBytes());
        bos.flush();
    }
}
