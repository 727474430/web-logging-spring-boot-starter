package com.raindrop.aop;

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
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Date;

/**
 * @name: com.raindrop.aop.WebLoggingAop.java
 * @description: WebLogging切面
 * @author: Wang Liang
 * @create Time: 2018/6/13 8:54 PM
 * @copyright:
 */
@Aspect
@Component
public class WebLoggingAop {

	private static Logger logger = LoggerFactory.getLogger(WebLogging.class);

	private ThreadLocal<Long> time = new ThreadLocal();

	private String excludePath;
	private String printHeader;

	public WebLoggingAop(String excludePath, String printHeader) {
		this.excludePath = excludePath;
		this.printHeader = printHeader;
	}

	public WebLoggingAop() { }

	@Pointcut("@annotation(com.raindrop.anno.WebLogging)")
	public void pointcut() { }

	@Before(value = "pointcut()")
	public void before(JoinPoint joinPoint) {
		ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		HttpServletRequest request = requestAttributes.getRequest();

		if (isExcludePath(request.getRequestURI())) return;

		try {
			time.set(new Date().getTime());
			logger.info("=============Request Payload Start============");
			logger.info("Request Url: {}", request.getRequestURL().toString());
			logger.info("Request Content-Type: {}", request.getContentType());
			logger.info("Request Method: {}", request.getMethod());
			logger.info("Request Headers: {}", getHeaders(request, printHeader));
			logger.info("Request Description: {}", getDescription(joinPoint));
			logger.info("Request Parameters: {}", getRequestParameter(joinPoint));
			logger.info("=============Request Payload End==============");
		} catch (Exception e) { }
	}

	/**
	 * 封装请求参数为Json格式
	 *
	 * @param joinPoint
	 * @return
	 */
	private String getRequestParameter(JoinPoint joinPoint) {
		JSONObject jsonObject = new JSONObject();
		MethodSignature signature = (MethodSignature) joinPoint.getSignature();
		String[] parameterNames = signature.getParameterNames();
		Object[] args = joinPoint.getArgs();
		for (int i = 0; i < args.length; i++) {
			jsonObject.put(parameterNames[i], args[i]);
		}
		return jsonObject.toJSONString();
	}

	@AfterReturning(returning = "result", pointcut = "pointcut()")
	public void afterReturning(Object result) {
		ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		HttpServletResponse response = requestAttributes.getResponse();
		HttpServletRequest request = requestAttributes.getRequest();

		if (isExcludePath(request.getRequestURI())) return;

		logger.info("=============Response Payload Start============");
		logger.info("Response Status: {}", response.getStatus());
		logger.info("Response Body: {}", result.toString());
		logger.info("Request Time: [ {}ms ]", new Date().getTime() - time.get());
		logger.info("=============Response Payload End==============");
	}

	/**
	 * 得到方法描述
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
	 * 返回指定Header信息
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
	 *
	 * @param requestURI
	 * @return
	 */
	private boolean isExcludePath(String requestURI) {
		if (excludePath != null) {
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
