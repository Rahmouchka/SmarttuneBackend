package com.example.SmarttuneBackend.metier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;
    private final String FROM = "no-reply@smarttune.com";

    @Async
    public void sendWelcomeEmail(String to, String prenom) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(FROM);
        msg.setTo(to);
        msg.setSubject("Bienvenue sur SmartTune !");
        msg.setText(String.format("Bonjour %s,\n\nVotre compte est créé avec succès !\n\nL'équipe SmartTune", prenom));
        mailSender.send(msg);
    }

    @Async
    public void sendArtistPendingEmail(String to, String prenom) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(FROM);
        msg.setTo(to); // BIEN L'EMAIL DE L'ARTISTE
        msg.setSubject("Demande artiste en attente");
        msg.setText(String.format(
                "Bonjour %s,\n\n" +
                        "Votre demande pour devenir artiste sur SmartTune a été reçue.\n\n" +
                        "Nous l'examinons actuellement. Vous recevrez un email dès qu'elle sera approuvée ou refusée.\n\n" +
                        "Merci pour votre patience !\n\n" +
                        "L'équipe SmartTune", prenom
        ));
        mailSender.send(msg);
    }

    @Async
    public void sendAdminNewArtistRequest(Long id, String nomArtiste, String email) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(FROM);
        msg.setTo("admin@smarttune.com");
        msg.setSubject("Nouvelle demande artiste");
        msg.setText(String.format("Nouvelle demande artiste :\nID: %d\nNom: %s\nEmail: %s\nLien: http://localhost:8080/admin/artist-request/%d", id, nomArtiste, email, id));
        mailSender.send(msg);
    }

    @Async
    public void sendArtistApprovedEmail(String to, String prenom) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("no-reply@smarttune.com");
        message.setTo(to);
        message.setSubject("Félicitations ! Votre compte artiste est approuvé");
        message.setText(
                "Bonjour " + prenom + ",\n\n" +
                        "Nous sommes ravis de vous annoncer que votre demande pour devenir artiste sur SmartTune a été APPROUVÉE !\n\n" +
                        "Vous pouvez dès maintenant vous connecter avec votre email et mot de passe, et commencer à uploader vos morceaux.\n\n" +
                        "Bienvenue dans la communauté des artistes SmartTune !\n\n" +
                        "L'équipe SmartTune"
        );
        mailSender.send(message);
    }

    @Async
    public void sendArtistRejectedEmail(String to, String prenom) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("no-reply@smarttune.com");
        message.setTo(to);
        message.setSubject("Demande artiste : statut mis à jour");
        message.setText(
                "Bonjour " + prenom + ",\n\n" +
                        "Nous avons examiné votre demande pour devenir artiste sur SmartTune.\n\n" +
                        "Malheureusement, elle n'a pas été acceptée pour le moment.\n\n" +
                        "Vous pouvez retenter votre chance plus tard avec un dossier plus complet (liens vers vos œuvres, press kit, etc.).\n\n" +
                        "Merci pour votre intérêt !\n\n" +
                        "L'équipe SmartTune"
        );
        mailSender.send(message);
    }
    @Async
    public void sendPasswordResetEmail(String to, String token, String frontendUrl) {
        String resetLink = frontendUrl + "/reset-password?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("no-reply@smarttune.com");
        message.setTo(to);
        message.setSubject("Réinitialisation de votre mot de passe");
        message.setText(
                "Bonjour,\n\n" +
                        "Vous avez demandé une réinitialisation de mot de passe.\n\n" +
                        "Cliquez sur le lien suivant pour réinitialiser votre mot de passe :\n" +
                        resetLink + "\n\n" +
                        "Ce lien expire dans 15 minutes.\n\n" +
                        "Si vous n'êtes pas à l'origine de cette demande, ignorez cet email.\n\n" +
                        "L'équipe SmartTune"
        );
        mailSender.send(message);
    }
}