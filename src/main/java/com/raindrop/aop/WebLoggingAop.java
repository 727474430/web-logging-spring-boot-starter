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
 * @description: WebLogging切面
 * @author: Wang Liang
 * @create Time: 2018/6/13 8:54 PM
 * @copyright:
 */
@Aspect
@Configuration
public class WebLoggingAop {

    private static Logger logger = LoggerFactory.getLogger(WebLogging.class);

    /**
     * Statistics request execute time
     */
    private ThreadLocal<Long> time = new ThreadLocal();
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

    @Pointcut("@annotation(com.raindrop.anno.WebLogging)")
    public void pointcut() {
    }

    @Before(value = "pointcut()")
    public void before(JoinPoint joinPoint) {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();

        if (isExcludePath(request.getRequestURI())) {
            return;
        }

        try {
            time.set(System.currentTimeMillis());
            logger.info("=============Request Payload Start============");
            logger.info("Request Url: {}", request.getRequestURL().toString());
            logger.info("Request Content-Type: {}", request.getHeader("Content-Type"));
            logger.info("Request Method: {}", request.getMethod());
            logger.info("Request Headers: {}", getHeaders(request, printHeader));
            logger.info("Request Description: {}", getDescription(joinPoint));
            logger.info("Request Payload: {}", getRequestParameter(request));
            logger.info("=============Request Payload End==============");
        } catch (Exception e) {
            logger.error("WebLoggingAop Request Parameter Parse Error: {}", e.getMessage());
        }
    }

    @AfterReturning(returning = "result", pointcut = "pointcut()")
    public void afterReturning(Object result) {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletResponse response = requestAttributes.getResponse();
        HttpServletRequest request = requestAttributes.getRequest();

        if (isExcludePath(request.getRequestURI())) {
            return;
        }

        logger.info("=============Response Payload Start============");
        logger.info("Response Status: {}", response.getStatus());
        logger.info("Response Payload: {}", JSON.toJSONString(result));
        logger.info("Request Time: [ {}ms ]", System.currentTimeMillis() - time.get());
        logger.info("=============Response Payload End==============");
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
     * @throws ClassNotFoundException
     */
    private String getDescription(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        WebLogging webLogging = method.getAnnotation(WebLogging.class);
        return webLogging.description();
    }

    /**
     * return specify headers info
     *
     * @param printHeader
     * @return
     */
    private String getHeaders(HttpServletRequest request, String printHeader) {
        JSONObject result = new JSONObject();
        if (printHeader != null && !"".equals(printHeader)) {
            String[] specifyHeaders = printHeader.split(";");
            for (int i = 0; i < specifyHeaders.length; i++) {
                String header = request.getHeader(specifyHeaders[i]);
                if (header != null || !"".equals(header)) {
                    result.put(specifyHeaders[i], header);
                }
            }
        }
        return result.toJSONString();
    }

    /**
     * 如果在excludePath中则为排除url 不进行日志处理
     * if request uri is in the exclude path, return false otherwise return true
     *
     * @param requestURI
     * @return
     */
    private boolean isExcludePath(String requestURI) {
        if (excludePath != null && !"".equals(excludePath)) {
            String[] excludePaths = this.excludePath.split(";");
            for (int i = 0; i < excludePaths.length; i++) {
                if (requestURI.indexOf(excludePaths[i]) != -1) {
                    return true;
                }
            }
        }
        return false;
    }

}
