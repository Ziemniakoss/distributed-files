package com.ziemniakoss.distributed.client;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class MainTestMD5 {
	public static void main(String[] args) throws NoSuchAlgorithmException, IOException {
		MessageDigest md = MessageDigest.getInstance("MD5");

		byte[] digest = md.digest(Files.readAllBytes(Paths.get("/home/ziemniak/IdeaProjects/distributed-files/client/pom.xml")));
		System.out.println(Arrays.toString(digest));

		md = MessageDigest.getInstance("MD5");
		try (InputStream is = Files.newInputStream(Paths.get("/home/ziemniak/IdeaProjects/distributed-files/client/pom.xml"))) {
			DigestInputStream dis = new DigestInputStream(is, md);
			while (dis.read()>1);

		}
//		new BigInteger(1,new byte[]);
		var d2 = md.digest();
		System.out.println(Arrays.toString(d2));

		for (byte b : d2){
			if((0xff & b) < 0x10){
				System.out.print("0"+Integer.toHexString((0xFF & b)));
			}else{
				System.out.print(Integer.toHexString(0xFF & b));
			}
		}

	}
}
