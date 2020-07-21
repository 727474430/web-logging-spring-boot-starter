package com.raindrop.aop;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.raindrop.anno.WebLogging;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Enumeration;

/**
 * @name: com.raindrop.aop.WebLoggingAop.java
 * @description: WebLogging Aop
 * @author: Wang Liang
 * @create Time: 2018/6/13 8:54 PM
 * @copyright:
 */
@Aspect
@Configuration
public class WebLoggingAop {

    private static final Logger logger = LoggerFactory.getLogger(WebLoggingAop.class);

    /**
     * Statistics request execute time
     */
    private final ThreadLocal<Long> time = new ThreadLocal();
    /**
     * need to exclude path
     */
    private String excludePath;
    /**
     * need to print request headers
     */
    private String printHeader;

    public WebLoggingAop(String excludePath, String printHeader) {
        this.excludePath = excludePath;
        this.printHeader = printHeader;
    }

    public WebLoggingAop() {
    }

    /**
     * Annotation pointcut
     */
    @Pointcut("@annotation(com.raindrop.anno.WebLogging)")
    public void pointcut() {
    }

    /**
     * Intercept before api execution
     *
     * @param joinPoint
     */
    @Before(value = "pointcut()")
    public void before(JoinPoint joinPoint) {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();

        if (isExcludePath(request.getRequestURI())) {
            return;
        }

        try {
            time.set(System.currentTimeMillis());
            logger.info("=============Request Start============");
            logger.info("Request Url          : {}", request.getRequestURL());
            logger.info("Request Method       : {}", request.getMethod());
            logger.info("Request IP           : {}", request.getRemoteAddr());
            logger.info("Request Content-Type : {}", request.getHeader("Content-Type"));
            logger.info("Request Headers      : {}", getHeaders(request, printHeader));
            logger.info("Request Description  : {}", getDescription(joinPoint));
            logger.info("Request Payload      : {}", getRequestParameter(request));
        } catch (Exception e) {
            logger.error("WebLoggingAop Request Parameter Parse Error: {}", e.getMessage());
        }
    }

    /**
     * Intercept after api execution
     *
     * @param result
     */
    @AfterReturning(returning = "result", pointcut = "pointcut()")
    public void afterReturning(Object result) {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletResponse response = requestAttributes.getResponse();
        HttpServletRequest request = requestAttributes.getRequest();

        if (isExcludePath(request.getRequestURI())) {
            return;
        }

        logger.info("Response Status      : {}", response.getStatus());
        logger.info("Response Payload     : {}", JSON.toJSONString(result));
        logger.info("Request Time         : {}ms", System.currentTimeMillis() - time.get());
        logger.info("=============Request End==============");
        time.remove();
    }

    /**
     * Combination request parameters to json format
     *
     * @param request
     * @return
     */
    private String getRequestParameter(HttpServletRequest request) {
        JSONObject jsonObject = new JSONObject();
        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String key = parameterNames.nextElement();
            String value = request.getParameter(key);
            jsonObject.put(key, value);
        }
        return jsonObject.toJSONString();
    }

    /**
     * get request method description info
     *
     * @param joinPoint
     * @return
     */
    private String getDescription(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        WebLogging webLogging = method.getAnnotation(WebLogging.class);
        return webLogging.value();
    }

    /**
     * return specify headers info
     *
     * @param printHeader Request headers that need to be printed
     * @return
     */
    private String getHeaders(HttpServletRequest request, String printHeader) {
        JSONObject result = new JSONObject();
        if (printHeader != null && !"".equals(printHeader)) {
            String[] specifyHeaders = printHeader.split(";");
            for (String specifyHeader : specifyHeaders) {
                String header = request.getHeader(specifyHeader);
                result.put(specifyHeader, header);
            }
        }
        return result.toJSONString();
    }

    /**
     * if request uri is in the exclude path, return false otherwise return true
     *
     * @param requestUri Client Request Target Url
     * @return
     */
    private boolean isExcludePath(String requestUri) {
        if (excludePath != null && !"".equals(excludePath)) {
            String[] excludePaths = this.excludePath.split(";");
            for (String path : excludePaths) {
                if (requestUri.contains(path)) {
                    return true;
                }
            }
        }
        return false;
    }

}
