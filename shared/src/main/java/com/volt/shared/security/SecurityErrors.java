package com.volt.shared.security;

import java.io.IOException;
import java.time.Instant;

import jakarta.servlet.http.HttpServletResponse;

public final class SecurityErrors {
	private SecurityErrors() {
	}

	public static void unauthorized(HttpServletResponse response, String message) throws IOException {
		write(response, HttpServletResponse.SC_UNAUTHORIZED, message);
	}

	public static void forbidden(HttpServletResponse response, String message) throws IOException {
		write(response, HttpServletResponse.SC_FORBIDDEN, message);
	}

	private static void write(HttpServletResponse response, int status, String message) throws IOException {
		response.setStatus(status);
		response.setCharacterEncoding("UTF-8");
		response.setContentType("application/json");
		response.getWriter().write("{\"error\":\"" + escape(message) + "\",\"timestamp\":\"" + Instant.now() + "\"}");
	}

	private static String escape(String value) {
		return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
	}
}
