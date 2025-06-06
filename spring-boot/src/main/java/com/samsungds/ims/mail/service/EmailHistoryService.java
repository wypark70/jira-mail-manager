package com.samsungds.ims.mail.service;

import com.samsungds.ims.mail.model.*;
import com.samsungds.ims.mail.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class EmailHistoryService {
    private final EmailQueueRepository emailQueueRepository;
    private final EmailQueueContentRepository emailQueueContentRepository;
    private final EmailQueueRecipientRepository emailQueueRecipientRepository;
    private final EmailQueueAttachmentRepository emailQueueAttachmentRepository;
    private final EmailHistoryRepository emailHistoryRepository;
    private final EmailHistoryContentRepository emailHistoryContentRepository;
    private final EmailHistoryRecipientRepository emailHistoryRecipientRepository;
    private final EmailHistoryAttachmentRepository emailHistoryAttachmentRepository;

    @Transactional
    public int moveEmailsToHistoryByStatus(EmailQueue.EmailStatus status) {
        List<EmailQueue> emails = emailQueueRepository.findByStatus(status);
        if (emails.isEmpty()) {
            return 0;
        }

        List<Long> emailIds = emails.stream()
                .map(EmailQueue::getId)
                .collect(Collectors.toList());

        List<EmailHistory> emailHistories = emails.stream()
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
                    history.setProcessorId(queue.getProcessorId());
                    history.setUniqueId(queue.getUniqueId());
                    history.setTags(queue.getTags());
                    history.setSource(queue.getSource());
                    return history;
                })
                .collect(Collectors.toList());

        List<EmailHistory> savedHistories = emailHistoryRepository.saveAll(emailHistories);

        List<EmailQueueContent> queueContents = emailQueueContentRepository.findAllById(emailIds);
        List<EmailHistoryContent> historyContents = new ArrayList<>();

        for (int i = 0; i < queueContents.size(); i++) {
            EmailHistoryContent historyContent = new EmailHistoryContent();
            historyContent.setEmailHistory(savedHistories.get(i));
            historyContent.setBody(queueContents.get(i).getBody());
            historyContents.add(historyContent);
        }
        emailHistoryContentRepository.saveAll(historyContents);

        List<EmailQueueRecipient> queueRecipients = emailQueueRecipientRepository.findAllByEmailQueueIdIn(emailIds);
        Map<Long, EmailHistory> historyRecipientsMap = savedHistories.stream()
                .collect(Collectors.toMap(
                        EmailHistory::getOriginalEmailId,
                        history -> history
                ));

        List<EmailHistoryRecipient> historyRecipients = queueRecipients.stream()
                .map(qr -> {
                    EmailHistoryRecipient hr = new EmailHistoryRecipient();
                    hr.setEmailHistory(historyRecipientsMap.get(qr.getEmailQueue().getId()));
                    hr.setEmail(qr.getEmail());
                    hr.setType(qr.getType());
                    return hr;
                })
                .toList();
        emailHistoryRecipientRepository.saveAll(historyRecipients);

        List<EmailQueueAttachment> queueAttachments = emailQueueAttachmentRepository.findAllByEmailQueueIdIn(emailIds);
        Map<Long, EmailHistory> historyAttachmentsMap = savedHistories.stream()
                .collect(Collectors.toMap(
                        EmailHistory::getOriginalEmailId,
                        history -> history
                ));

        List<EmailHistoryAttachment> historyAttachment = queueAttachments.stream()
                .map(qa -> {
                    EmailHistoryAttachment ha = new EmailHistoryAttachment();
                    ha.setEmailHistory(historyAttachmentsMap.get(qa.getEmailQueue().getId()));
                    ha.setContentType(qa.getContentType());
                    ha.setFileName(qa.getFileName());
                    ha.setFilePath(qa.getFilePath());
                    return ha;
                })
                .toList();
        emailHistoryAttachmentRepository.saveAll(historyAttachment);

        emailQueueContentRepository.deleteAllByEmailQueueIdIn(emailIds);
        emailQueueRecipientRepository.deleteAllByEmailQueueIdIn(emailIds);
        emailQueueAttachmentRepository.deleteAllByEmailQueueIdIn(emailIds);
        emailQueueRepository.deleteAllById(emailIds);

        return emails.size();
    }


    @Transactional
    public long deleteAllHistory() {
        long historyCount = emailHistoryRepository.count();
        emailHistoryContentRepository.deleteAll();
        emailHistoryRecipientRepository.deleteAll();
        emailHistoryAttachmentRepository.deleteAll();
        emailHistoryRepository.deleteAll();
        return historyCount;
    }
}