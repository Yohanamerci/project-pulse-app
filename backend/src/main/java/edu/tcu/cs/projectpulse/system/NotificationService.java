package edu.tcu.cs.projectpulse.system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Sends email notifications.
 * Uses Mailpit in dev (localhost:1025) — no auth required.
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final JavaMailSender mailSender;
    private final String frontendBaseUrl;

    public NotificationService(JavaMailSender mailSender,
                                @Value("${app.frontend-base-url}") String frontendBaseUrl) {
        this.mailSender = mailSender;
        this.frontendBaseUrl = frontendBaseUrl;
    }

    /**
     * Notifies all students in a section that a new active week has opened.
     * Runs asynchronously so it never delays the HTTP response.
     *
     * @param sectionName  display name of the section
     * @param weekNumber   the newly activated week number
     * @param startDate    week start date
     * @param endDate      week end date
     * @param studentEmails list of student email addresses in the section
     */
    @Async
    public void notifyActiveWeekOpened(String sectionName,
                                       int weekNumber,
                                       LocalDate startDate,
                                       LocalDate endDate,
                                       List<String> studentEmails) {
        if (studentEmails.isEmpty()) {
            return;
        }

        String subject = String.format("[Project Pulse] Week %d is now active — %s", weekNumber, sectionName);
        String body = String.format("""
                Hello,

                Week %d is now the active submission week for %s.

                  Start: %s
                  End:   %s

                Please submit your Weekly Activity Report (WAR) and peer evaluations
                before the week closes.

                — Project Pulse
                """, weekNumber, sectionName, startDate, endDate);

        for (String email : studentEmails) {
            try {
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom("noreply@projectpulse.edu");
                message.setTo(email);
                message.setSubject(subject);
                message.setText(body);
                mailSender.send(message);
                log.debug("Sent active-week notification to {}", email);
            } catch (Exception ex) {
                log.warn("Failed to send active-week notification to {}: {}", email, ex.getMessage());
            }
        }
    }

    /**
     * UC-30: Sends an instructor invitation email containing a registration link.
     * Runs asynchronously so it never delays the HTTP response.
     */
    @Async
    public void sendInvitationEmail(String email, String token) {
        String registrationLink = frontendBaseUrl + "/register?token=" + token;
        String subject = "[Project Pulse] You've been invited as an Instructor";
        String body = String.format("""
                Hello,

                You have been invited to join Project Pulse as an Instructor.

                Click the link below to complete your registration (valid for 7 days):

                  %s

                If you did not expect this email, you can safely ignore it.

                — Project Pulse
                """, registrationLink);
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("noreply@projectpulse.edu");
            message.setTo(email);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Sent instructor invitation to {}", email);
        } catch (Exception ex) {
            log.warn("Failed to send invitation email to {}: {}", email, ex.getMessage());
        }
    }
}
