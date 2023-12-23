package site.controller;

import site.facade.MailService;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ivan St. Ivanov
 */
class MailServiceMock extends MailService {

    List<String> recipientAddresses = new ArrayList<>();

    String lastMessageText;
    
    @Override
    public void sendEmail(String to, String subject, String messageText) {
        recipientAddresses.add(to);
        lastMessageText = messageText;
    }
}
