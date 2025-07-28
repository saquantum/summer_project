package uk.ac.bristol.advice;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// TODO: replace creating io streams to thread pool

@Component
@Aspect
public class LogAdvice {
    private static final ExecutorService threadPool = Executors.newFixedThreadPool(4);

    @Pointcut("execution(* *..service..*Service.*(..))")
    public void service() {
    }

    @After("service()")
    public void serviceLogger(JoinPoint jp) throws IOException {
        threadPool.submit(() -> {
            try (BufferedOutputStream bos = getLogFileStream()) {
                StringBuilder sb = new StringBuilder()
                        .append(TraceThreadLocalID.get()).append("\t")
                        .append(LocalDateTime.now()).append("\t")
                        .append(jp.getSignature().getDeclaringTypeName()).append(".")
                        .append(jp.getSignature().getName())
                        .append(Arrays.toString(jp.getArgs())).append("\t")
                        .append(System.lineSeparator());
                bos.write(sb.toString().getBytes());
                bos.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Pointcut("execution(* *..controller..*Controller.*(..))")
    public void controller() {
    }

    @Around("controller()")
    public Object controllerLogger(ProceedingJoinPoint jp) throws Throwable {
        try (BufferedOutputStream bos = getLogFileStream()) {
            String traceId = UUID.randomUUID().toString();
            TraceThreadLocalID.set(traceId);
            threadPool.submit(() -> logControllerMethod("before", jp, traceId));
            Object result = jp.proceed();
            threadPool.submit(() -> logControllerMethod("after", jp, traceId));
            return result;
        }
    }

    private void logControllerMethod(String stage, ProceedingJoinPoint jp, String traceId) {
        try (BufferedOutputStream bos = getLogFileStream()) {
            StringBuilder sb = new StringBuilder()
                    .append(traceId).append("\t")
                    .append(LocalDateTime.now()).append("\t")
                    .append(stage).append("\t")
                    .append(jp.getSignature().getDeclaringTypeName()).append(".")
                    .append(jp.getSignature().getName())
                    .append(Arrays.toString(jp.getArgs())).append("\t")
                    .append(System.lineSeparator());
            bos.write(sb.toString().getBytes());
            bos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private BufferedOutputStream getLogFileStream() throws IOException {
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

        return new BufferedOutputStream(new FileOutputStream(logFile, true));
    }
}
