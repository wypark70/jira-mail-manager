package com.samsungds.ims.mail.repository;

import com.samsungds.ims.mail.model.EmailContent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailContentRepository extends JpaRepository<EmailContent, Long> {
}