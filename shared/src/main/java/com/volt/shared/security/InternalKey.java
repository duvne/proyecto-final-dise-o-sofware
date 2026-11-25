package com.volt.shared.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import jakarta.servlet.http.HttpServletRequest;

public final class InternalKey {
	public static final String HEADER = "X-Volt-Internal-Key";

	private InternalKey() {
	}

	public static boolean valida(HttpServletRequest request, String expected) {
		String provided = request.getHeader(HEADER);
		if (provided == null || expected == null) {
			return false;
		}
		return MessageDigest.isEqual(
				provided.getBytes(StandardCharsets.UTF_8),
				expected.getBytes(StandardCharsets.UTF_8));
	}
}
