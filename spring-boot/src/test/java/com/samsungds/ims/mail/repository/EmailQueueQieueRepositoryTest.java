package com.samsungds.ims.mail.repository;

import com.samsungds.ims.mail.model.EmailQueue;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class EmailQueueQieueRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EmailQueueRepository emailQueueRepository;

    @Test
    void testSaveAndFindEmail() {
        // Arrange
        EmailQueue emailQueue = new EmailQueue();
        emailQueue.setSender("test@example.com");
        emailQueue.setRecipient("recipient@example.com");
        emailQueue.setSubject("Test Email");
        emailQueue.setBody("This is a test email body");

        // Act
        EmailQueue savedEmailQueue = emailQueueRepository.save(emailQueue);

        // Force a flush to ensure the entity is persisted
        entityManager.flush();

        // Clear the persistence context to ensure we're getting from the database
        entityManager.clear();

        Optional<EmailQueue> foundEmail = emailQueueRepository.findById(savedEmailQueue.getId());

        // Assert
        assertTrue(foundEmail.isPresent());
        assertEquals("test@example.com", foundEmail.get().getSender());
        assertEquals("recipient@example.com", foundEmail.get().getRecipient());
        assertEquals("Test Email", foundEmail.get().getSubject());
        assertEquals("This is a test email body", foundEmail.get().getBody());
    }

    @Test
    void testFindAllEmails() {
        // Arrange
        EmailQueue emailQueue1 = new EmailQueue();
        emailQueue1.setSender("sender1@example.com");
        emailQueue1.setRecipient("recipient1@example.com");
        emailQueue1.setSubject("Subject 1");

        EmailQueue emailQueue2 = new EmailQueue();
        emailQueue2.setSender("sender2@example.com");
        emailQueue2.setRecipient("recipient2@example.com");
        emailQueue2.setSubject("Subject 2");

        entityManager.persist(emailQueue1);
        entityManager.persist(emailQueue2);
        entityManager.flush();

        // Act
        List<EmailQueue> emailQueues = emailQueueRepository.findAll();

        // Assert
        assertEquals(2, emailQueues.size());
        assertTrue(emailQueues.stream().anyMatch(e -> "sender1@example.com".equals(e.getSender())));
        assertTrue(emailQueues.stream().anyMatch(e -> "sender2@example.com".equals(e.getSender())));
    }
}