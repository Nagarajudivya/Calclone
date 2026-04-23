package com.calclone.service;

import com.calclone.entity.Appointment;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendBookingConfirmation(Appointment appt) {
        try {

            sendEmail(
                    appt.getGuestEmail(),
                    "Meeting Confirmed: " + appt.getEventType().getTitle(),
                    buildEmailBody(appt, false)
            );


            sendEmail(
                    appt.getEventType().getUser().getEmail(),
                    "New Booking: " + appt.getEventType().getTitle() + " with " + appt.getGuestName(),
                    buildEmailBody(appt, true)
            );

        } catch (MessagingException e) {
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }

    private void sendEmail(String to, String subject, String htmlBody) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true); // true = HTML
        mailSender.send(message);
    }

    private String buildEmailBody(Appointment appt, boolean isHost) {
        String meetLink = appt.getMeetingLink() != null ? appt.getMeetingLink() : "Link not available";

        return """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 24px; border: 1px solid #e5e7eb; border-radius: 12px;">
                <h2 style="color: #16a34a;">✅ Meeting Confirmed</h2>
                <p>Hi %s,</p>
                <p>Your meeting has been scheduled. Here are the details:</p>

                <table style="width: 100%%; border-collapse: collapse; margin: 20px 0;">
                    <tr>
                        <td style="padding: 10px; font-weight: bold; color: #6b7280; width: 120px;">WHAT</td>
                        <td style="padding: 10px; color: #111827;">%s between %s and %s</td>
                    </tr>
                    <tr style="background: #f9fafb;">
                        <td style="padding: 10px; font-weight: bold; color: #6b7280;">WHEN</td>
                        <td style="padding: 10px; color: #111827;">%s at %s (India Standard Time)</td>
                    </tr>
                    <tr>
                        <td style="padding: 10px; font-weight: bold; color: #6b7280;">HOST</td>
                        <td style="padding: 10px; color: #111827;">%s</td>
                    </tr>
                    <tr style="background: #f9fafb;">
                        <td style="padding: 10px; font-weight: bold; color: #6b7280;">GUEST</td>
                        <td style="padding: 10px; color: #111827;">%s (%s)</td>
                    </tr>
                    <tr>
                        <td style="padding: 10px; font-weight: bold; color: #6b7280;">WHERE</td>
                        <td style="padding: 10px;"><a href="%s" style="color: #2563eb;">Join Meeting</a></td>
                    </tr>
                </table>

                <p style="color: #6b7280; font-size: 14px;">If you need to make changes, please contact the organiser.</p>
            </div>
            """.formatted(
                isHost ? appt.getEventType().getUser().getUsername() : appt.getGuestName(),
                appt.getEventType().getTitle(),
                appt.getEventType().getUser().getUsername(),
                appt.getGuestName(),
                appt.getDate().toString(),
                appt.getStartTime(),
                appt.getEventType().getUser().getUsername(),
                appt.getGuestName(),
                appt.getGuestEmail(),
                meetLink
        );
    }
}