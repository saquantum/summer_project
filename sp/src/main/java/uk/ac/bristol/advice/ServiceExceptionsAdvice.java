package uk.ac.bristol.advice;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import uk.ac.bristol.exception.SpExceptions;

import java.util.Arrays;

@Component
@Aspect
public class ServiceExceptionsAdvice {
    @Pointcut("execution(* *..service.impl.*.*(..))")
    public void pt() {
    }

    @Around("pt()")
    public Object classifyExceptions(ProceedingJoinPoint jp) throws Throwable {
        try {
            return jp.proceed();
        } catch (SpExceptions e) {
            throw e;
        } catch (Exception e) {
            StackTraceElement[] stackTrace = e.getStackTrace();
            StackTraceElement caller = Arrays.stream(stackTrace)
                    .filter(el -> el.getClassName().startsWith("uk.ac.bristol."))
                    .findFirst()
                    .orElse(stackTrace[0]);

            String className = caller.getClassName();
            String methodName = caller.getMethodName();
            int lineNumber = caller.getLineNumber();
            String fileName = caller.getFileName();
            String message = String.format("Exception at %s.%s (%s:%d): %s", className, methodName, fileName, lineNumber, e.getMessage());

            if (methodName.startsWith("get")) {
                throw new SpExceptions.GetMethodException(message);
            } else if (methodName.startsWith("insert")) {
                throw new SpExceptions.PostMethodException(message);
            } else if (methodName.startsWith("update")) {
                throw new SpExceptions.PutMethodException(message);
            } else if (methodName.startsWith("delete")) {
                throw new SpExceptions.DeleteMethodException(message);
            } else {
                throw new SpExceptions.SystemException(e.getMessage());
            }
        }
    }
}
