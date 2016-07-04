package org.apache.catalina.loader;

import org.apache.tomcat.util.codec.binary.Base64;

/**
 * 加解密
 * @author lindezhi
 * 2016年7月2日 上午11:52:06
 */
public class SimpleCodec {
	
	private static Base64 base64 = new Base64();
	
	public static byte[] encode(byte[] content){
		return base64.encode(content);
	}
	
	public static byte[] decode(byte[] content){
		return base64.decode(content);
	}

}
