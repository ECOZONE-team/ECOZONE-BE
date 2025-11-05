package co.ecozone.ecozoneapi.auth.domain.port.out;

public interface MailSenderPort {
    void send(String to, String subject, String htmlBody);
}
