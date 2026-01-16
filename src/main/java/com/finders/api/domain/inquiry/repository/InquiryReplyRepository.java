package com.finders.api.domain.inquiry.repository;

import com.finders.api.domain.inquiry.entity.InquiryReply;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InquiryReplyRepository extends JpaRepository<InquiryReply, Long> {
}
