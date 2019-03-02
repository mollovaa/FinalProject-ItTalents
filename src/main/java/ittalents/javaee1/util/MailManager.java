package ittalents.javaee1.util;


import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Date;
import java.util.Properties;

public class MailManager {
    private static Logger logger = LogManager.getLogger(MailManager.class);

    public static void sendEmail(String emailTo, String subject, String content) {

        new Thread(() -> {
            try {
                sendmail(emailTo, subject, content);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }).start();
    }

    private static void sendmail(String emailTo, String subject, String content) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("ittalentsprojectemail@gmail.com", "<password>");
            }
        });
        Message msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress("ittalentsprojectemail@gmail.com", false));

        msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emailTo));
        msg.setSubject(subject);
        msg.setContent(content, "text/html");
        msg.setSentDate(new Date());

        MimeBodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setContent("Tutorials point email", "text/html");

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);
//		MimeBodyPart attachPart = new MimeBodyPart(); // for attachments if needed
//
//		attachPart.attachFile("/var/tmp/image19.png");
//		multipart.addBodyPart(attachPart);
//		msg.setContent(multipart);
        Transport.send(msg);
    }
}
