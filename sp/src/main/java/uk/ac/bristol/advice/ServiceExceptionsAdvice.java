package uk.ac.bristol.advice;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import uk.ac.bristol.exception.SpExceptions;

@Component
@Aspect
public class ServiceExceptionsAdvice {
    @Pointcut("execution(* *..service.impl.*(..))")
    public void pt() {
    }

    @Around("pt()")
    public Object classifyExceptions(ProceedingJoinPoint jp) throws Throwable {
        try {
            return jp.proceed();
        } catch (SpExceptions.SystemException e) {
            throw e;
        } catch (Exception e) {
            String methodName = jp.getSignature().getName().toLowerCase();

            if (methodName.startsWith("get")) {
                throw new SpExceptions.GetMethodException(e.getMessage());
            } else if (methodName.startsWith("insert")) {
                throw new SpExceptions.PostMethodException(e.getMessage());
            } else if (methodName.startsWith("update")) {
                throw new SpExceptions.PutMethodException(e.getMessage());
            } else if (methodName.startsWith("delete")) {
                throw new SpExceptions.DeleteMethodException(e.getMessage());
            } else {
                throw new SpExceptions.BusinessException(e.getMessage());
            }
        }
    }
}
