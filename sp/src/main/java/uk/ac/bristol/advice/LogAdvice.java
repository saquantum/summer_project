package uk.ac.bristol.advice;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.io.BufferedOutputStream;
import java.io.File;
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
        String logFile = "logs/app.log";
        File file = new File(logFile);

        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            if (!parent.mkdirs()) {
                throw new IOException("Failed to create log directory: " + parent.getAbsolutePath());
            }
        }

        if (!file.exists()) {
            if (!file.createNewFile()) {
                throw new IOException("Failed to create log file: " + file.getAbsolutePath());
            }
        }

        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(logFile, true));
        LocalDateTime now = LocalDateTime.now();
        StringBuilder sb = new StringBuilder()
                .append(now).append("\t")
                .append(Arrays.toString(jp.getArgs())).append("\t")
                .append(jp.getSignature().getName()).append(System.lineSeparator());
        bos.write(sb.toString().getBytes());
        bos.flush();
    }
}
