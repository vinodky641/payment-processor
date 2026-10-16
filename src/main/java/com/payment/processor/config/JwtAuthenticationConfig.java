package com.payment.processor.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import java.util.List;

import static com.payment.processor.constant.PaymentProcessorConstants.JWT_ROLES_CLAIM;
import static com.payment.processor.constant.PaymentProcessorConstants.ROLE_PREFIX;

@Configuration
public class JwtAuthenticationConfig {

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter(

            @Value(JWT_ROLES_CLAIM)
            String rolesClaim
    ) {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            List<String> roles = jwt.getClaimAsStringList(rolesClaim);
            if (roles == null || roles.isEmpty()) {
                return List.<GrantedAuthority>of();
            }

            return roles.stream()
                    .filter(role -> role != null && !role.isBlank())
                    .map(role -> role.startsWith(ROLE_PREFIX) ? role : ROLE_PREFIX + role)
                    .distinct()
                    .map(role -> (GrantedAuthority) new SimpleGrantedAuthority(role))
                    .toList();
        });

        return converter;
    }

}

