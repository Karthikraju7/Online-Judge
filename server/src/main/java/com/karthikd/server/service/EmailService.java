package com.karthikd.server.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendVerificationEmail(String toEmail, String token) {
        String subject = "Verify your account";
        String verificationLink = "http://localhost:5173/verify?token=" + token;

        String body = "<div style='font-family: Arial, sans-serif; color: #333; padding: 20px;'>"
                + "<h2>Welcome to Online Judge!</h2>"
                + "<p>This mail is secured and sent from the owner of <b>Online Judge</b>.</p>"
                + "<p>Please verify your account by clicking the button below:</p>"
                + "<a href='" + verificationLink + "' "
                + "style='display: inline-block; padding: 10px 20px; background-color: #007BFF; "
                + "color: #fff; text-decoration: none; border-radius: 5px; font-weight: bold;'>"
                + "Verify Account</a>"
                + "<p style='margin-top: 20px;'>Note: This link will expire in 10 minutes.</p>"
                + "</div>";

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(body, true); // HTML content enabled
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send verification email", e);
        }
    }
}
