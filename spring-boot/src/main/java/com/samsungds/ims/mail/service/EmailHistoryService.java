package com.samsungds.ims.mail.service;

import com.samsungds.ims.mail.model.*;
import com.samsungds.ims.mail.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class EmailHistoryService {

    private final EmailQueueRepository emailQueueRepository;
    private final EmailQueueContentRepository emailQueueContentRepository;
    private final EmailQueueRecipientRepository emailQueueRecipientRepository;
    private final EmailHistoryRepository emailHistoryRepository;
    private final EmailHistoryContentRepository emailHistoryContentRepository;
    private final EmailHistoryRecipientRepository emailHistoryRecipientRepository;

    public EmailHistoryService(
            EmailQueueRepository emailQueueRepository,
            EmailQueueContentRepository emailQueueContentRepository,
            EmailQueueRecipientRepository emailQueueRecipientRepository,
            EmailHistoryRepository emailHistoryRepository,
            EmailHistoryContentRepository emailHistoryContentRepository,
            EmailHistoryRecipientRepository emailHistoryRecipientRepository) {
        this.emailQueueRepository = emailQueueRepository;
        this.emailQueueContentRepository = emailQueueContentRepository;
        this.emailQueueRecipientRepository = emailQueueRecipientRepository;
        this.emailHistoryRepository = emailHistoryRepository;
        this.emailHistoryContentRepository = emailHistoryContentRepository;
        this.emailHistoryRecipientRepository = emailHistoryRecipientRepository;
    }

    @Transactional
    public int moveAllSentEmailsToHistory() {
        // 1. 발송 완료된 이메일 큐 모두 조회
        List<EmailQueue> sentEmails = emailQueueRepository.findByStatus(EmailQueue.EmailStatus.SENT);
        if (sentEmails.isEmpty()) {
            return 0;
        }

        List<Long> emailIds = sentEmails.stream()
                .map(EmailQueue::getId)
                .collect(Collectors.toList());

        // 2. 이메일 히스토리 데이터 일괄 생성
        List<EmailHistory> emailHistories = sentEmails.stream()
                .map(queue -> {
                    EmailHistory history = new EmailHistory();
                    history.setOriginalEmailId(queue.getId());
                    history.setSender(queue.getSender());
                    history.setSubject(queue.getSubject());
                    history.setStatus(queue.getStatus());
                    history.setProcessorId(queue.getProcessorId());
                    history.setErrorMessage(queue.getErrorMessage());
                    history.setRetryCount(queue.getRetryCount());
                    history.setSentAt(queue.getSentAt());
                    return history;
                })
                .collect(Collectors.toList());
        
        List<EmailHistory> savedHistories = emailHistoryRepository.saveAll(emailHistories);

        // 3. 컨텐츠 일괄 복사
        List<EmailQueueContent> queueContents = emailQueueContentRepository.findAllById(emailIds);
        List<EmailHistoryContent> historyContents = new ArrayList<>();
        
        for (int i = 0; i < queueContents.size(); i++) {
            EmailHistoryContent historyContent = new EmailHistoryContent();
            historyContent.setEmailHistory(savedHistories.get(i));
            historyContent.setBody(queueContents.get(i).getBody());
            historyContents.add(historyContent);
        }
        emailHistoryContentRepository.saveAll(historyContents);

        // 4. 수신자 정보 일괄 복사
        List<EmailQueueRecipient> queueRecipients = emailQueueRecipientRepository.findAllByEmailQueueIdIn(emailIds);
        Map<Long, EmailHistory> historyMap = savedHistories.stream()
                .collect(Collectors.toMap(
                    history -> history.getOriginalEmailId(), 
                    history -> history
                ));

        List<EmailHistoryRecipient> historyRecipients = queueRecipients.stream()
                .map(qr -> {
                    EmailHistoryRecipient hr = new EmailHistoryRecipient();
                    hr.setEmailHistory(historyMap.get(qr.getEmailQueue().getId()));
                    hr.setEmail(qr.getEmail());
                    hr.setType(qr.getType());
                    return hr;
                })
                .collect(Collectors.toList());
        emailHistoryRecipientRepository.saveAll(historyRecipients);

        // 5. 큐 데이터 일괄 삭제
        emailQueueRecipientRepository.deleteAllByEmailQueueIdIn(emailIds);
        emailQueueContentRepository.deleteAllById(emailIds);
        emailQueueRepository.deleteAllById(emailIds);

        return sentEmails.size();
    }
}