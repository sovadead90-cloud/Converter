package com.example.fileConversionService.repository;

import com.example.fileConversionService.domain.InboxMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface InboxRepository extends JpaRepository<InboxMessage, UUID> {
}
