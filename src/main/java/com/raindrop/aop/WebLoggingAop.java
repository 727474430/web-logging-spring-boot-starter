package com.raindrop.aop;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.raindrop.anno.WebLogging;
import com.raindrop.util.JsonUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
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
    /**
     * whether to format parameters
     */
    private boolean format;

    public WebLoggingAop(String excludePath, String printHeader, boolean format) {
        this.excludePath = excludePath;
        this.printHeader = printHeader;
        this.format = format;
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
     * Intercept api execution
     *
     * @param pj
     * @return
     * @throws Throwable
     */
    @Around(value = "pointcut()")
    public Object around(ProceedingJoinPoint pj) throws Throwable {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletResponse response = requestAttributes.getResponse();
        HttpServletRequest request = requestAttributes.getRequest();

        if (isExcludePath(request.getRequestURI())) {
            return pj.proceed();
        }

        StringBuilder sb = new StringBuilder();
        Object result = null;
        try {
            time.set(System.currentTimeMillis());
            sb.append("=============Request Logging Start============");
            sb.append("\nRequest Url          : " + request.getRequestURL());
            sb.append("\nRequest Method       : " + request.getMethod());
            sb.append("\nRequest IP           : " + request.getRemoteAddr());
            sb.append("\nRequest Headers      : " + formatParameter(getHeaders(request, printHeader)));
            sb.append("\nRequest Description  : " + getDescription(pj));
            sb.append("\nRequest Payload      : " + formatParameter(getRequestParameter(request, pj)));
            result = pj.proceed();
            sb.append("\nResponse Status      : " + response.getStatus());
            sb.append("\nResponse Payload     : " + formatParameter(JSON.toJSONString(result)));
            sb.append("\nRequest Time         : " + (System.currentTimeMillis() - time.get()) + "ms\n");
            sb.append("=============Request Logging End==============");
            time.remove();
        } catch (Exception e) {
            logger.error("WebLoggingAop Request Parameter Parse Error: {}", e.getMessage());
        } finally {
            logger.info(sb.toString());
        }
        return result;
    }

    /**
     * Combination request parameters to json format
     *
     * @param request
     * @return
     */
    private String getRequestParameter(HttpServletRequest request, JoinPoint joinPoint) {
        String params = getParameter(request);
        if (StringUtils.isEmpty(params)) {
            params = getParameter(joinPoint);
        }
        return params;
    }

    /**
     * Get parameter from request
     *
     * @param request
     * @return
     */
    private String getParameter(HttpServletRequest request) {
        JSONObject jsonObject = new JSONObject();
        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String key = parameterNames.nextElement();
            String value = request.getParameter(key);
            jsonObject.put(key, value);
        }
        return jsonObject.isEmpty() ? "" : jsonObject.toJSONString();
    }

    /**
     * Get parameter from joinPoint
     *
     * @param joinPoint
     * @return
     */
    private String getParameter(JoinPoint joinPoint) {
        JSONObject jsonObject = new JSONObject();
        JSONObject signatureJson = JSON.parseObject(JSON.toJSONString(joinPoint.getSignature()));
        JSONArray parameterNames = signatureJson.getJSONArray("parameterNames");
        Object[] args = joinPoint.getArgs();
        for (int i = 0; i < parameterNames.size(); i++) {
            if (!(args[i] instanceof HttpServletRequest) && !(args[i] instanceof HttpServletResponse)) {
                jsonObject.put(parameterNames.getString(i), args[i]);
            }
        }
        return jsonObject.isEmpty() ? "" : jsonObject.toJSONString();
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

    /**
     * Format json type parameter
     *
     * @param parameter parameter data
     * @return
     */
    private String formatParameter(String parameter) {
        if (format) {
            boolean isJsonObject = JsonUtil.isValidObject(parameter);
            if (isJsonObject) {
                return JsonUtil.jsonFormat(parameter);
            }
        }
        return parameter;
    }

}
