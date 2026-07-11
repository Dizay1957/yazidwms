package com.yazidwms.notification.service;

import com.yazidwms.notification.entity.NotificationLog;
import com.yazidwms.notification.repository.NotificationLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final JavaMailSender mailSender;
    private final NotificationLogRepository notificationLogRepository;

    public NotificationService(JavaMailSender mailSender, NotificationLogRepository notificationLogRepository) {
        this.mailSender = mailSender;
        this.notificationLogRepository = notificationLogRepository;
    }

    @Async
    @Transactional
    public void sendEmail(String recipient, String subject, String body) {
        var logEntry = new NotificationLog();
        logEntry.setRecipient(recipient);
        logEntry.setSubject(subject);
        logEntry.setBody(body);
        try {
            var message = new SimpleMailMessage();
            message.setTo(recipient);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            logEntry.setSent(true);
        } catch (Exception ex) {
            log.warn("Email notification failed for {}", recipient, ex);
            logEntry.setSent(false);
            logEntry.setErrorMessage(ex.getMessage());
        }
        notificationLogRepository.save(logEntry);
    }

    @Async
    public void lowStock(String recipient, String sku, int quantity) {
        sendEmail(recipient, "Low stock alert: " + sku, "Product " + sku + " has low stock: " + quantity);
    }

    @Async
    public void purchaseOrderReceived(String recipient, String orderNumber) {
        sendEmail(recipient, "Purchase order received", "Purchase order " + orderNumber + " has been received.");
    }

    @Async
    public void salesOrderShipped(String recipient, String orderNumber) {
        sendEmail(recipient, "Sales order shipped", "Sales order " + orderNumber + " has been shipped.");
    }

    @Async
    public void userCreated(String recipient, String activationToken) {
        sendEmail(recipient, "YazidWMS user created", "Your activation token is: " + activationToken);
    }

    @Async
    public void passwordReset(String recipient, String token) {
        sendEmail(recipient, "YazidWMS password reset", "Your password reset token is: " + token);
    }
}
