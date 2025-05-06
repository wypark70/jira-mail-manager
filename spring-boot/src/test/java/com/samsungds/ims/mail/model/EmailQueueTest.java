package com.samsungds.ims.mail.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EmailQueueTest {

    @Test
    void testEmailProperties() {
        // Arrange
        EmailQueue emailQueue = new EmailQueue();
        emailQueue.setId(1L);
        emailQueue.setSender("sender@example.com");
        emailQueue.setRecipient("recipient@example.com");
        emailQueue.setSubject("Test Subject");
        emailQueue.setBody("Test Body Content");

        // Assert
        assertEquals(1L, emailQueue.getId());
        assertEquals("sender@example.com", emailQueue.getSender());
        assertEquals("recipient@example.com", emailQueue.getRecipient());
        assertEquals("Test Subject", emailQueue.getSubject());
        assertEquals("Test Body Content", emailQueue.getBody());
    }

    @Test
    void testEmailEquality() {
        // Arrange
        EmailQueue emailQueue1 = new EmailQueue();
        emailQueue1.setId(1L);
        emailQueue1.setSender("sender@example.com");
        emailQueue1.setRecipient("recipient@example.com");

        EmailQueue emailQueue2 = new EmailQueue();
        emailQueue2.setId(1L);
        emailQueue2.setSender("sender@example.com");
        emailQueue2.setRecipient("recipient@example.com");

        EmailQueue emailQueue3 = new EmailQueue();
        emailQueue3.setId(2L);

        // Assert
        assertEquals(emailQueue1, emailQueue2, "Emails with same ID and properties should be equal");
        assertNotEquals(emailQueue1, emailQueue3, "Emails with different IDs should not be equal");
    }
}