package uk.ac.bristol.advice;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import uk.ac.bristol.exception.SpExceptions;
import uk.ac.bristol.pojo.Asset;
import uk.ac.bristol.util.QueryTool;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Component
@Aspect
public class UserIdentificationAdvice {

    @Pointcut("execution(* *..controller..*Controller.user*UID(..))")
    public void identityUID() {
    }

    @Pointcut("execution(* *..controller..*Controller.user*AID(..))")
    public void identityAID() {
    }

    @Pointcut("execution(* *..controller..*Controller.user*ByAssetWithUID(..))")
    public void assetOwnershipUID() {
    }

    @Pointcut("execution(* *..controller..*Controller.user*ByAssetWithAID(..))")
    public void assetOwnershipAID() {
    }

    private Map<String, Object> getParametersFromJoinPoint(JoinPoint jp) {
        Map<String, Object> result = new HashMap<>();

        MethodSignature methodSignature = (MethodSignature) jp.getSignature();
        Method method = methodSignature.getMethod();
        Annotation[][] paramAnnotations = method.getParameterAnnotations();
        Object[] args = jp.getArgs();

        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            for (Annotation annotation : paramAnnotations[i]) {
                if (annotation instanceof UserUID) {
                    result.put("uid", arg);
                } else if (annotation instanceof UserAID) {
                    result.put("aid", arg);
                } else if (annotation instanceof UserAsset) {
                    result.put("assetId", ((Asset) arg).getId());
                } else if (annotation instanceof UserAssetId) {
                    result.put("assetId", arg);
                }
            }
            if (arg instanceof HttpServletRequest) {
                result.put("request", arg);
            } else if (arg instanceof HttpServletResponse) {
                result.put("response", arg);
            }
        }
        return result;
    }

    @Before("identityUID()")
    public void userIdentityVerificationByUID(JoinPoint jp) {
        Map<String, Object> parameters = getParametersFromJoinPoint(jp);
        System.out.println(parameters.get("uid"));
        System.out.println(parameters.get("response"));
        System.out.println(parameters.get("request"));

        if (parameters.get("uid") == null || parameters.get("request") == null || parameters.get("response") == null) {
            throw new IllegalStateException("Missing required parameters for user identity validation with uid.");
        }

        if (!QueryTool.userIdentityVerification(
                (HttpServletResponse) parameters.get("response"),
                (HttpServletRequest) parameters.get("request"),
                (String) parameters.get("uid"),
                null)) {
            throw new SpExceptions.ForbiddenException("User identification failed");
        }
    }

    @Before("identityAID()")
    public void userIdentityVerificationByAID(JoinPoint jp) {
        Map<String, Object> parameters = getParametersFromJoinPoint(jp);

        if (parameters.get("aid") == null || parameters.get("request") == null || parameters.get("response") == null) {
            throw new IllegalStateException("Missing required parameters for user identity validation with aid.");
        }

        if (!QueryTool.userIdentityVerification(
                (HttpServletResponse) parameters.get("response"),
                (HttpServletRequest) parameters.get("request"),
                null,
                (String) parameters.get("aid"))) {
            throw new SpExceptions.ForbiddenException("User identification failed");
        }
    }

    @Before("assetOwnershipUID()")
    public void userAssetOwnershipByUID(JoinPoint jp) {
        Map<String, Object> parameters = getParametersFromJoinPoint(jp);

        if (parameters.get("uid") == null || parameters.get("assetId") == null) {
            throw new IllegalStateException("Missing required parameters for asset ownership validation by uid.");
        }

        if (!QueryTool.verifyAssetOwnership(
                (String) parameters.get("assetId"),
                (String) parameters.get("uid"),
                null)) {
            throw new SpExceptions.ForbiddenException("Asset owner identification failed");
        }
    }

    @Before("assetOwnershipAID()")
    public void userAssetOwnershipByAID(JoinPoint jp) {
        Map<String, Object> parameters = getParametersFromJoinPoint(jp);

        if (parameters.get("aid") == null || parameters.get("assetId") == null) {
            throw new IllegalStateException("Missing required parameters for asset ownership validation by aid.");
        }

        if (!QueryTool.verifyAssetOwnership(
                (String) parameters.get("assetId"),
                null,
                (String) parameters.get("aid"))) {
            throw new SpExceptions.ForbiddenException("Asset owner identification failed");
        }
    }
}
