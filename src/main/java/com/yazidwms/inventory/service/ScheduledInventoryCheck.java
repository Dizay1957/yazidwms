package com.yazidwms.inventory.service;

import com.yazidwms.notification.service.NotificationService;
import com.yazidwms.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ScheduledInventoryCheck {

    private final ProductRepository productRepository;
    private final NotificationService notificationService;
    private final String notificationAdminEmail;

    public ScheduledInventoryCheck(ProductRepository productRepository, NotificationService notificationService,
                                   @Value("${app.notification.admin-email}") String notificationAdminEmail) {
        this.productRepository = productRepository;
        this.notificationService = notificationService;
        this.notificationAdminEmail = notificationAdminEmail;
    }

    @Scheduled(cron = "0 0 7 * * *")
    @Transactional(readOnly = true)
    public void checkLowStock() {
        productRepository.findAll().stream()
                .filter(product -> !product.isDeleted() && product.isLowStock())
                .forEach(product -> notificationService.lowStock(notificationAdminEmail, product.getSku(), product.getQuantity()));
    }
}
