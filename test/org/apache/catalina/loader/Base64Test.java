package org.apache.catalina.loader;

import org.apache.tomcat.util.codec.binary.Base64;

public class Base64Test {

	public static void main(String[] args) {
		Base64 base64 = new org.apache.tomcat.util.codec.binary.Base64();
		byte[] encode = base64.encode("12345678".getBytes());
		System.out.println(new String(encode));
		
	}
}
