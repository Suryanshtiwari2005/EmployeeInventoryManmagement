package com.inventoryEmployee.demo.service;

import com.inventoryEmployee.demo.entity.StockAlert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendLowStockAlert(StockAlert alert) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo("manager@company.com"); // Configure recipient
            message.setSubject("Stock Alert: " + alert.getAlertType());
            message.setText(buildAlertMessage(alert));

            mailSender.send(message);
            log.info("Stock alert email sent for product: {}", alert.getProduct().getId());

        } catch (Exception e) {
            log.error("Error sending stock alert email", e);
        }
    }

    private String buildAlertMessage(StockAlert alert) {
        return String.format(
                "Alert Type: %s\n" +
                        "Product: %s (SKU: %s)\n" +
                        "Current Quantity: %d\n" +
                        "Threshold: %d\n" +
                        "Time: %s\n\n" +
                        "Please take action immediately.",
                alert.getAlertType(),
                alert.getProduct().getName(),
                alert.getProduct().getSku(),
                alert.getCurrentQuantity(),
                alert.getThreshold(),
                alert.getCreatedAt()
        );
    }

    @Async
    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);
            log.info("Email sent to: {}", to);

        } catch (Exception e) {
            log.error("Error sending email to: {}", to, e);
        }
    }
}
