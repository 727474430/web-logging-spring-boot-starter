package com.raindrop.config;

import com.raindrop.aop.WebLoggingAop;
import com.raindrop.properties.WebLoggingProperties;
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

    private final WebLoggingProperties properties;

    public WebLoggingAutoConfigure(WebLoggingProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean(WebLoggingAop.class)
    public WebLoggingAop initWebLogging() {
        return new WebLoggingAop(properties.getExcludePath(), properties.getPrintHeader());
    }

}
