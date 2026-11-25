package com.volt.reservaspagos.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.volt.shared.security.JwtAuthenticationFilter;
import com.volt.shared.security.JwtTokenService;
import com.volt.shared.security.SecurityErrors;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
	@Bean
	SecurityFilterChain securityFilterChain(JwtTokenService jwtTokenService) throws Exception {
		HttpSecurity http = new HttpSecurity(ObjectPostProcessor.identity(),
				new AuthenticationManagerBuilder(ObjectPostProcessor.identity()), java.util.Map.of());
		return http
				.csrf(csrf -> csrf.disable())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
						.requestMatchers("/actuator/health", "/actuator/info",
								"/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
						.requestMatchers(HttpMethod.GET,
								"/api/reservas/disponibilidad/**").permitAll()
						.requestMatchers("/api/reservas/interno/**").permitAll()
						.anyRequest().authenticated())
				.exceptionHandling(exceptions -> exceptions
						.authenticationEntryPoint((request, response, exception) ->
								SecurityErrors.unauthorized(response, "Debes iniciar sesion para continuar."))
						.accessDeniedHandler((request, response, exception) ->
								SecurityErrors.forbidden(response, "No tienes permisos para esta operacion.")))
				.addFilterBefore(new JwtAuthenticationFilter(jwtTokenService), UsernamePasswordAuthenticationFilter.class)
				.httpBasic(basic -> basic.disable())
				.formLogin(form -> form.disable())
				.build();
	}
}
