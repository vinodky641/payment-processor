package com.payment.processor.service;

import com.payment.processor.enums.PaymentStatus;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

import static com.payment.processor.constant.PaymentProcessorConstants.*;

@Component
public class ProcessingMetrics {

    private final AtomicLong processed = new AtomicLong();
    private final AtomicLong held = new AtomicLong();
    private final AtomicLong rejected = new AtomicLong();
    private final AtomicLong count = new AtomicLong();
    private final AtomicLong totalProcessingTimeMs = new AtomicLong();

    public ProcessingMetrics(MeterRegistry meterRegistry) {
        Gauge.builder(
                PAYMENT_PROCESSOR_TOTAL_PROCESSED,
                processed,
                AtomicLong::doubleValue
        ).register(meterRegistry);

        Gauge.builder(
                PAYMENT_PROCESSOR_TOTAL_HELD,
                held,
                AtomicLong::doubleValue
        ).register(meterRegistry);

        Gauge.builder(
                PAYMENT_PROCESSOR_TOTAL_REJECTED,
                rejected,
                AtomicLong::doubleValue
        ).register(meterRegistry);
    }

    public void record(String status, long processingTimeMs) {

        if (status.equals(PaymentStatus.PROCESSED.name())) {
            processed.incrementAndGet();
        }
        else if (status.equals(PaymentStatus.HELD.name())) {
            held.incrementAndGet();
        } else {
            rejected.incrementAndGet();
        }
        count.incrementAndGet();
        totalProcessingTimeMs.addAndGet(processingTimeMs);
    }

    public long processed() {
        return processed.get();
    }

    public long held() {
        return held.get();
    }

    public long rejected() {
        return rejected.get();
    }

    public double averageProcessingTimeMs() {
        long totalCount = count.get();
        return totalCount == 0 ? 0.0 :   (double) totalProcessingTimeMs.get() / totalCount;
    }
}

