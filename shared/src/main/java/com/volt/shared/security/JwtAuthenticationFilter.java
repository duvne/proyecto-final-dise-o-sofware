package com.volt.shared.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
	private final JwtTokenService tokenService;

	public JwtAuthenticationFilter(JwtTokenService tokenService) {
		this.tokenService = tokenService;
	}

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {
		String authorization = request.getHeader("Authorization");
		if (authorization == null || !authorization.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}

		try {
			TokenPrincipal principal = tokenService.validar(authorization.substring(7));
			List<SimpleGrantedAuthority> authorities = principal.roles().stream()
					.map(role -> new SimpleGrantedAuthority("ROLE_" + role))
					.toList();
			SecurityContextHolder.getContext().setAuthentication(
					new UsernamePasswordAuthenticationToken(principal, null, authorities));
			filterChain.doFilter(request, response);
		} catch (InvalidTokenException exception) {
			SecurityContextHolder.clearContext();
			SecurityErrors.unauthorized(response, exception.getMessage());
		}
	}
}
