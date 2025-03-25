package com.example.LMS_test.aspect;

import com.example.LMS_test.user.User;
import com.example.LMS_test.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.util.Optional;

@Aspect
@Component
@RequiredArgsConstructor
public class LoggingAspect {
    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);
    private final HttpServletRequest request;
    private final UserRepository userRepository;

    // Pointcut for all APIs
    @Pointcut("@annotation(org.springframework.web.bind.annotation.RequestMapping)"
            + "|| @annotation(org.springframework.web.bind.annotation.PostMapping)"
            + "|| @annotation(org.springframework.web.bind.annotation.GetMapping)"
            + "|| @annotation(org.springframework.web.bind.annotation.PutMapping)"
            + "|| @annotation(org.springframework.web.bind.annotation.DeleteMapping)")
    public void controllerPointcut() {
    }


    @Around("controllerPointcut()")
    public Object logControllerMethods(ProceedingJoinPoint joinPoint) throws Throwable {

        String api = request.getRequestURI();

        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        StopWatch stopWatch = new StopWatch();

        String userName = "anonymousUser";
        User user = getUser();
        if (user != null) userName = user.getUsername();


        try {
            logger.info("Received API request {} [{}.{}] by {}", api, className, methodName, userName);
            stopWatch.start();

            Object result = joinPoint.proceed();

            stopWatch.stop();
            logger.info("Completed API request {} [{}.{}] by {} in {} ms", api, className, methodName, userName, stopWatch.getTotalTimeMillis());

            return result;
        } catch (Exception e) {
            logger.error("Exception in {} [{}.{}] - {}", api, className, methodName, e.getMessage());
            throw e;
        }
    }

    public User getUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Object principal = auth.getPrincipal();
        if (auth.isAuthenticated() && principal instanceof String) {
            Optional<User> user = userRepository.findByUsername(principal.toString());
            return user.orElse(null);
        } else {
            return (User) principal;
        }
    }
}