package com.payment.processor.config;

import com.payment.processor.exception.AppIllegalStateException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;

import static com.payment.processor.constant.PaymentProcessorConstants.*;

@Configuration
public class JwtDecoderConfig {

    @Bean
    public JwtDecoder jwtDecoder(

            PublicKey jwtPublicKey,

            @Value(JWT_ISSUER)
            String issuer,

            @Value(JWT_AUDIENCE)
            String audience
    ) {
        if (!(jwtPublicKey instanceof RSAPublicKey rsaPublicKey)) {
            throw new AppIllegalStateException(
                    RSA_PUBLIC_KEY_FIELD,
                    JWT_VERIFICATION_REQUIRES_AN_RSA_PUBLIC_KEY
            );

        }

        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withPublicKey(rsaPublicKey)
                .signatureAlgorithm(SignatureAlgorithm.RS256)
                .build();

        OAuth2TokenValidator<Jwt> issuerAndTimestampValidator = JwtValidators.createDefaultWithIssuer(issuer);

        OAuth2TokenValidator<Jwt> audienceValidator = jwt ->
                jwt.getAudience().contains(audience)
                        ? OAuth2TokenValidatorResult.success()
                        : OAuth2TokenValidatorResult.failure(
                        new OAuth2Error(OAUTH2_INVALID_TOKEN, OAUTH2_REQUIRED_JWT_AUDIENCE_IS_MISSING, null)
                );

        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(issuerAndTimestampValidator, audienceValidator));
        return decoder;
    }

}
