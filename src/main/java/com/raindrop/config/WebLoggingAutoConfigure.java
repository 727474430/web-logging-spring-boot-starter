package com.raindrop.config;

import com.raindrop.aop.WebLoggingAop;
import com.raindrop.properties.WebLoggingProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @name: com.raindrop.config.WebLoggingAutoConfigure.java
 * @description: WebLogging Auto Configure
 * @author: Wang Liang
 * @create Time: 2018/6/13 8:49 PM
 */
@Configuration
@ConditionalOnClass(WebLoggingAop.class)
@EnableConfigurationProperties(WebLoggingProperties.class)
@ConditionalOnProperty(prefix = "web.log", value = "enable", havingValue = "true")
public class WebLoggingAutoConfigure {

    private static final Logger logger = LoggerFactory.getLogger(WebLoggingAutoConfigure.class);

    private final WebLoggingProperties properties;

    public WebLoggingAutoConfigure(WebLoggingProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean(WebLoggingAop.class)
    public WebLoggingAop initWebLogging() {
        logger.info("" +
                "\n=============================================================" +
                "\n=========== Web Logging Spring Boot Start Enabled ===========" +
                "\n=============================================================");
        return new WebLoggingAop(properties.getExcludePath(), properties.getPrintHeader(), properties.isFormat());
    }

}
