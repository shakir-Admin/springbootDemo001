package com.example.demo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;


@SpringBootApplication
@MapperScan("com.example.demo.Mapper")
//@EnableTransactionManagement
@EnableAspectJAutoProxy(exposeProxy = true)
public class SpringbootDemo001Application {

	public static void main(String[] args) {
		SpringApplication.run(SpringbootDemo001Application.class, args);
	}

}
