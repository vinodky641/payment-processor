package com.payment.processor.service;

import com.payment.processor.dto.FxQuote;
import com.payment.processor.exception.FxRateUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;

import static com.payment.processor.constant.PaymentProcessorConstants.*;

@Service
public class FxRateService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper mapper;
    private final String currencyExchangeRateUrl;
    private final RestClient restClient;


    public FxRateService(
            StringRedisTemplate redisTemplate,
            ObjectMapper mapper,
            @Value(CURRENCY_EXCHANGE_RATE_BASE_URL_KEY)
            String currencyExchangeRateUrl,
            RestClient.Builder builder) {

        this.redisTemplate = redisTemplate;
        this.mapper = mapper;
        this.currencyExchangeRateUrl = currencyExchangeRateUrl;
        this.restClient = builder.baseUrl(this.currencyExchangeRateUrl).build();
    }

    public FxQuote rate(String from, String to) {

        // if source and target currencies are the same, return 1.0 as the rate
        if (from.equalsIgnoreCase(to)) {
            return new FxQuote(BigDecimal.ONE, LocalDate.now(), CURRENCY_EXCHANGE_RATE_SOURCE_IDENTITY);
        }

        // check if the rate is cached in Redis
        String key = CURRENCY_EXCHANGE_RATE_CACHE_KEY_PREFIX + from + ":" + to;
        try {
            String cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                JsonNode cachedRate = mapper.readTree(cached);
                return new FxQuote(
                        cachedRate.get(CURRENCY_EXCHANGE_RATE).decimalValue(),
                        LocalDate.parse(cachedRate.get(CURRENCY_EXCHANGE_RATE_DATE).asString()),
                        CURRENCY_EXCHANGE_RATE_SOURCE_CACHE
                );
            }
        } catch (Exception ignored) {
        }

        // if not cached then fetch the rate from external service FRANKFURTER
        // and cache it in Redis for 24 hours
        try {
            JsonNode rateResponse = restClient
                    .get()
                    .uri(CURRENCY_EXCHANGE_RATE_FROM_TO, from, to)
                    .retrieve()
                    .body(JsonNode.class);

            BigDecimal rate = rateResponse.get(
                    CURRENCY_EXCHANGE_RATE
            ).decimalValue();

            LocalDate date = LocalDate.parse(
                    rateResponse.get(CURRENCY_EXCHANGE_RATE_DATE).asString()
            );

            String value = mapper.createObjectNode()
                    .put(CURRENCY_EXCHANGE_RATE, rate)
                    .put(CURRENCY_EXCHANGE_RATE_DATE, date.toString())
                    .toString();

            redisTemplate.opsForValue().set(key, value, Duration.ofHours(24));
            return new FxQuote(rate, date, CURRENCY_EXCHANGE_RATE_SOURCE_FRANKFURTER);
        } catch (Exception e) {
            throw new FxRateUnavailableException(CURRENCY_EXCHANGE_RATE_UNAVAILABLE_MESSAGE + from + "/" + to, e);
        }
    }
}

