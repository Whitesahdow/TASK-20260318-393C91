package com.busapp.repository;

import com.busapp.model.FieldDictionaryEntry;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FieldDictionaryRepository extends JpaRepository<FieldDictionaryEntry, Long> {
    Optional<FieldDictionaryEntry> findByDictKey(String dictKey);
}
