package com.raindrop;

import com.raindrop.anno.WebLogging;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@SpringBootApplication
@Controller
@RequestMapping("/web")
public class WebLoggingSpringBootStarterApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebLoggingSpringBootStarterApplication.class, args);
	}

	@RequestMapping("/index")
	@ResponseBody
	@WebLogging(description = "主方法")
	public String index(String name, String age, String sex) {
		System.out.println("name = [" + name + "], age = [" + age + "], sex = [" + sex + "]");
		return "ok";
	}

}
