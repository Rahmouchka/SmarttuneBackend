package com.example.SmarttuneBackend.metier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;
    private final String FROM = "no-reply@smarttune.com";

    private String getEmailTemplate(String title, String content, String buttonText, String buttonUrl) {
        String buttonHtml = buttonText != null && buttonUrl != null
                ? "<p style=\"text-align: center; margin: 30px 0;\"><a href=\"" + buttonUrl +
                "\" style=\"display: inline-block; padding: 16px 40px; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; text-decoration: none; border-radius: 8px; font-weight: bold; font-size: 16px;\">"
                + buttonText + "</a></p>"
                : "";

        String template = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <style>
                body { margin: 0; padding: 0; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); font-family: Arial, sans-serif; }
                .container { max-width: 600px; margin: 40px auto; background: white; border-radius: 16px; overflow: hidden; box-shadow: 0 20px 60px rgba(0,0,0,0.3); }
                .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 40px; text-align: center; color: white; }
                .content { padding: 40px; color: #333; }
                .footer { background: #f8f9fa; padding: 20px; text-align: center; color: #666; font-size: 14px; }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <h1>SmartTune</h1>
                </div>
                <div class="content">
                    <h2>__TITLE__</h2>
                    __CONTENT__
                    __BUTTON__
                </div>
                <div class="footer">
                    <p>© 2025 SmartTune</p>
                </div>
            </div>
        </body>
        </html>
        """;

        return template
                .replace("__TITLE__", title)
                .replace("__CONTENT__", content)
                .replace("__BUTTON__", buttonHtml);
    }

    @Async
    public void sendWelcomeEmail(String to, String prenom) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(FROM);
            helper.setTo(to);
            helper.setSubject("🎉 Bienvenue sur SmartTune !");

            String content = "<p>Bonjour <strong>" + prenom + "</strong>,</p>" +
                    "<p>Votre compte a été créé avec succès ! Vous pouvez dès maintenant explorer des millions de morceaux.</p>" +
                    "<p>Bonne écoute ! 🎶</p>";

            helper.setText(getEmailTemplate("Bienvenue sur SmartTune", content, "Explorer SmartTune", "http://localhost:8081/login"), true);
            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    @Async
    public void sendArtistPendingEmail(String to, String prenom) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(FROM);
            helper.setTo(to);
            helper.setSubject("⏳ Demande artiste en cours d'examen");

            String content = "<p>Bonjour <strong>" + prenom + "</strong>,</p>" +
                    "<p>Nous avons bien reçu votre demande pour devenir artiste sur SmartTune.</p>" +
                    "<p>Notre équipe examine votre dossier. Vous recevrez une réponse sous <strong>48 heures maximum</strong>.</p>" +
                    "<p>Merci pour votre patience ! 🎵</p>";

            helper.setText(getEmailTemplate("Demande reçue", content, null, null), true);
            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    @Async
    public void sendAdminNewArtistRequest(Long id, String nomArtiste, String email) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(FROM);
            helper.setTo("admin@smarttune.com");
            helper.setSubject("🚨 Nouvelle demande artiste");

            String content = "<p><strong>Nouvelle demande artiste à traiter :</strong></p>" +
                    "<ul>" +
                    "<li>ID : " + id + "</li>" +
                    "<li>Nom d'artiste : " + nomArtiste + "</li>" +
                    "<li>Email : " + email + "</li>" +
                    "</ul>";

            helper.setText(getEmailTemplate("Nouvelle demande", content, "Voir la demande", "http://localhost:8081/admin/dashboard"), true);
            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    @Async
    public void sendArtistApprovedEmail(String to, String prenom) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(FROM);
            helper.setTo(to);
            helper.setSubject("🎉 Votre compte artiste est approuvé !");

            String content = "<p>Bonjour <strong>" + prenom + "</strong>,</p>" +
                    "<p>Excellente nouvelle ! 🎊</p>" +
                    "<p>Votre demande pour devenir artiste sur SmartTune a été <strong>APPROUVÉE</strong> !</p>" +
                    "<p>Vous pouvez dès maintenant :</p>" +
                    "<ul>" +
                    "<li>✅ Vous connecter avec votre email et mot de passe</li>" +
                    "<li>✅ Uploader vos morceaux</li>" +
                    "<li>✅ Gérer votre profil artiste</li>" +
                    "<li>✅ Suivre vos statistiques</li>" +
                    "</ul>" +
                    "<p>Bienvenue dans la communauté SmartTune ! 🎶</p>";

            helper.setText(getEmailTemplate("Compte approuvé", content, "Se connecter", "http://localhost:8081/login"), true);
            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    @Async
    public void sendArtistRejectedEmail(String to, String prenom) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(FROM);
            helper.setTo(to);
            helper.setSubject("❌ Mise à jour de votre demande artiste");

            String content = "<p>Bonjour <strong>" + prenom + "</strong>,</p>" +
                    "<p>Nous avons examiné votre demande pour devenir artiste sur SmartTune.</p>" +
                    "<p>Malheureusement, elle n'a pas été acceptée pour le moment.</p>" +
                    "<p><strong>Pourquoi ?</strong> Votre dossier nécessite plus d'informations (liens vers vos œuvres, press kit, réseaux sociaux, etc.).</p>" +
                    "<p>Vous pouvez retenter votre chance avec un dossier plus complet !</p>" +
                    "<p>Merci pour votre intérêt. 💜</p>";

            helper.setText(getEmailTemplate("Demande non acceptée", content, "En savoir plus", "http://localhost:8081/register/artist"), true);
            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    @Async
    public void sendPasswordResetEmail(String to, String token, String frontendUrl) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String resetLink = frontendUrl + "/reset-password?token=" + token;

            helper.setFrom(FROM);
            helper.setTo(to);
            helper.setSubject("🔑 Réinitialisation de votre mot de passe");

            String content = "<p>Vous avez demandé une réinitialisation de mot de passe.</p>" +
                    "<p>Ce lien expire dans <strong>15 minutes</strong>.</p>" +
                    "<p>Si vous n'êtes pas à l'origine de cette demande, ignorez cet email.</p>";

            helper.setText(getEmailTemplate("Réinitialisation mot de passe", content, "Réinitialiser", resetLink), true);
            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}