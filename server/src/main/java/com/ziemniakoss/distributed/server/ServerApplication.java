package com.ziemniakoss.distributed.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ServerApplication {
	private static int SERVER_ID;

	public static int getServerId() {
		return SERVER_ID;
	}

	public static void main(String[] args) {
		if (args.length == 0) {
			System.exit(1);
		}
		SERVER_ID = Integer.parseInt(args[0]);
		SpringApplication.run(ServerApplication.class, args);
	}
}
