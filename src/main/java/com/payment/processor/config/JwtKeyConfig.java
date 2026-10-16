package com.payment.processor.config;

import com.payment.processor.exception.AppIllegalStateException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import static com.payment.processor.constant.PaymentProcessorConstants.*;

@Configuration
public class JwtKeyConfig {

    @Bean
    public PublicKey jwtPublicKey(

            @Value(JWT_PUBLIC_KEY)
            String encodedKey
    ) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(encodedKey);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);

            return KeyFactory.getInstance(JWT_SIGNATURE_ALGORITHM_RSA).generatePublic(keySpec);
        } catch (Exception ex) {
            throw new AppIllegalStateException(
                    RSA_PUBLIC_KEY_FIELD,
                    JWT_UNABLE_TO_LOAD_RSA_PUBLIC_KEY
            );
        }
    }

}
