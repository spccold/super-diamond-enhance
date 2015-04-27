package com.github.diamond.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenValidator {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(OpenValidator.class);

	// base64(cs.51.nb&spccold&nice)
	private static final String SALT = "Y3MuNTEubmImc3BjY29sZCZuaWNl";

	public static boolean isValid(String projectCode, String profile,
			String cipherContent) {
		return generateCipherContent(projectCode,profile).equals(cipherContent);
	}

	public static String generateCipherContent(String projectCode,
			String profile) {
		String simple = projectCode + "&" + profile + "&" + SALT;
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			String text = Base64.encodeBase64String(digest.digest(simple
					.getBytes()));
			return text;
		} catch (NoSuchAlgorithmException e) {
			LOGGER.error("生成消息摘要失败", e);
			return StringUtils.EMPTY;
		}
	}
	public static void main(String[] args) {
		System.out.println(generateCipherContent("001","development"));
	}
}
