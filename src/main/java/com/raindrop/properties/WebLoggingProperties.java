package com.raindrop.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @name: com.raindrop.properties.WebLoggingProperties.java
 * @description: WebLogging配置实体
 * @author: Wang Liang
 * @create Time: 2018/6/13 8:44 PM
 * @copyright:
 */
@ConfigurationProperties("web.log")
public class WebLoggingProperties {

	private String enable = "false";
	private String excludePath = "";
	private String printHeader = "";

	public String getEnable() {
		return enable;
	}

	public void setEnable(String enable) {
		this.enable = enable;
	}

	public String getExcludePath() {
		return excludePath;
	}

	public void setExcludePath(String excludePath) {
		this.excludePath = excludePath;
	}

	public String getPrintHeader() {
		return printHeader;
	}

	public void setPrintHeader(String printHeader) {
		this.printHeader = printHeader;
	}
}
