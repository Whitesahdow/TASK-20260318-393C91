package com.busapp.repository;

import com.busapp.model.ImportAuditLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImportAuditLogRepository extends JpaRepository<ImportAuditLog, Long> {
    List<ImportAuditLog> findTop50ByOrderByTimestampDesc();
}
