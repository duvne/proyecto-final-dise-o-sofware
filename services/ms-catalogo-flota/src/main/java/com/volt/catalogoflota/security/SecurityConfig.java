package com.volt.catalogoflota.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.volt.shared.security.JwtAuthenticationFilter;
import com.volt.shared.security.JwtTokenService;
import com.volt.shared.security.SecurityErrors;

@Configuration
public class SecurityConfig {
	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http, JwtTokenService jwtTokenService) throws Exception {
		return http
				.csrf(csrf -> csrf.disable())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/api/catalogo/patinetas", "/api/catalogo/patinetas/**").permitAll()
						.anyRequest().authenticated())
				.exceptionHandling(exceptions -> exceptions
						.authenticationEntryPoint((request, response, exception) ->
								SecurityErrors.unauthorized(response, "Debes iniciar sesion para continuar."))
						.accessDeniedHandler((request, response, exception) ->
								SecurityErrors.forbidden(response, "No tienes permisos para esta operacion.")))
				.addFilterBefore(new JwtAuthenticationFilter(jwtTokenService), UsernamePasswordAuthenticationFilter.class)
				.build();
	}
}
