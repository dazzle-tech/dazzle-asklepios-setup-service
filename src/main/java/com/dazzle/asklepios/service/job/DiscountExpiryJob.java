package com.dazzle.asklepios.service.job;

import com.dazzle.asklepios.service.DiscountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DiscountExpiryJob {

    private static final Logger LOG = LoggerFactory.getLogger(DiscountExpiryJob.class);

    private final DiscountService discountService;

    public DiscountExpiryJob(DiscountService discountService) {
        this.discountService = discountService;
    }

    @Scheduled(cron = "${asklepios.jobs.discount-expiry.cron:0 5 0 * * *}")
    public void expireDiscountsDaily() {
        expireDiscounts("daily");
    }

    @EventListener(ApplicationReadyEvent.class)
    public void expireDiscountsOnStartup() {
        expireDiscounts("startup");
    }

    private void expireDiscounts(String trigger) {
        try {
            int updated = discountService.expireOutdatedDiscounts();
            LOG.info("Discount expiry job ({}) marked {} discount(s) as expired", trigger, updated);
        } catch (Exception exception) {
            LOG.error("Discount expiry job ({}) failed", trigger, exception);
        }
    }
}
