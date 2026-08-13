package com.asset.itassetsystem.service;

public interface MailService {
    void send(String to, String subject, String content);
}
