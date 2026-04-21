package com.busapp;

import com.busapp.model.StopVersion;
import com.busapp.repository.CleaningRuleRepository;
import com.busapp.repository.ImportAuditLogRepository;
import com.busapp.repository.StopVersionRepository;
import com.busapp.service.DataCleaningService;
import com.busapp.service.RawInput;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DataIntegrationModuleTest {

    @Mock
    private StopVersionRepository stopVersionRepository;
    @Mock
    private CleaningRuleRepository cleaningRuleRepository;
    @Mock
    private ImportAuditLogRepository importAuditLogRepository;

    @InjectMocks
    private DataCleaningService cleaningService;

    @Test
    void whenAreaInSqft_thenConvertToSqm() {
        when(cleaningRuleRepository.findByRuleKey(any())).thenReturn(Optional.empty());
        RawInput input = new RawInput();
        input.setName("Central Avenue");
        input.setArea(100.0);
        input.setUnit("sqft");

        StopVersion result = cleaningService.cleanAndTransform(input);

        assertEquals(9.29, result.getAreaSqm(), 0.01);
    }

    @Test
    void whenFieldMissing_thenMarkAsNull() {
        when(cleaningRuleRepository.findByRuleKey(any())).thenReturn(Optional.empty());
        RawInput input = new RawInput();
        StopVersion result = cleaningService.cleanAndTransform(input);

        assertNull(result.getAreaSqm());
        assertEquals("NULL", result.getStopName());
    }

    @Test
    void whenSameStopImportedTwice_thenVersionIncrements() {
        when(cleaningRuleRepository.findByRuleKey(any())).thenReturn(Optional.empty());
        RawInput input = new RawInput();
        input.setName("Main Street");
        input.setArea(10.0);
        input.setUnit("sqm");

        StopVersion previous = new StopVersion();
        previous.setVersionNumber(2);
        when(stopVersionRepository.findTopByStopNameOrderByVersionNumberDesc("Main Street"))
                .thenReturn(Optional.of(previous));
        when(stopVersionRepository.save(any(StopVersion.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StopVersion saved = cleaningService.importAndPersist(input);

        assertEquals(3, saved.getVersionNumber());
    }
}
