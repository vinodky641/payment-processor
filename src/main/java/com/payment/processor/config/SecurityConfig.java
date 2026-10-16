package com.payment.processor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import static com.payment.processor.constant.PaymentProcessorConstants.*;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationConverter jwtAuthenticationConverter) throws Exception {

        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                SWAGGER_UI_PATH,
                                SWAGGER_UI_RESOURCES_PATH,
                                API_DOCS_PATH,
                                API_DOCS_YAML_PATH
                        )
                        .permitAll()
                        .requestMatchers(
                                ACTUATOR_PATH
                        )
                        .permitAll()
                        .requestMatchers(
                                METRICS_SUMMARY_PATH,
                                REPORTS_PATH,
                                ACCOUNTS_HISTORY_PATH
                        )
                        .hasRole(ADMIN_ROLE)
                        .anyRequest()
                        .authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(
                                jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)
                        )
                )
                .build();
    }

}
