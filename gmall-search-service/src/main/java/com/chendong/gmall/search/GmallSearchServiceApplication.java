package com.chendong.gmall.search;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@MapperScan(basePackages = "com.chendong.gmall.manage.mapper")
public class GmallSearchServiceApplication {

	public static void main(String[] args) {
		//配置dubbo.qos.port端口
		//System.setProperty(Constants.QOS_PORT,"33333");
		//配置dubbo.qos.accept.foreign.ip是否关闭远程连接
		//System.setProperty(Constants.ACCEPT_FOREIGN_IP,"false");
		SpringApplication.run(GmallSearchServiceApplication.class, args);
		//关闭QOS服务
		//Server.getInstance().stop();
	}

}
