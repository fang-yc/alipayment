package com.ff.alipayment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@ComponentScan(basePackages = "com.ff.alipayment")
public class AlipaymentApplication {

	public static void main(String[] args) {
		SpringApplication.run(AlipaymentApplication.class, args);
	}

}
