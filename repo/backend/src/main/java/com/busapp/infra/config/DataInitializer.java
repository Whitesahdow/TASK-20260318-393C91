package com.busapp.infra.config;

import com.busapp.model.UserEntity;
import com.busapp.model.UserRole;
import com.busapp.model.BusRoute;
import com.busapp.model.BusStop;
import com.busapp.model.RouteStopMapping;
import com.busapp.model.NotificationTemplate;
import com.busapp.model.TaskStatus;
import com.busapp.model.TaskType;
import com.busapp.model.WorkflowTask;
import com.busapp.repository.BusRouteRepository;
import com.busapp.repository.BusStopRepository;
import com.busapp.repository.NotificationTemplateRepository;
import com.busapp.repository.RouteStopMappingRepository;
import com.busapp.repository.WorkflowTaskRepository;
import com.busapp.repository.UserRepository;
import com.busapp.service.AdminConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminConfigService adminConfigService;
    private final BusStopRepository busStopRepository;
    private final BusRouteRepository busRouteRepository;
    private final RouteStopMappingRepository routeStopMappingRepository;
    private final WorkflowTaskRepository workflowTaskRepository;
    private final NotificationTemplateRepository notificationTemplateRepository;

    public DataInitializer(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AdminConfigService adminConfigService,
            BusStopRepository busStopRepository,
            BusRouteRepository busRouteRepository,
            RouteStopMappingRepository routeStopMappingRepository
            , WorkflowTaskRepository workflowTaskRepository
            , NotificationTemplateRepository notificationTemplateRepository
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminConfigService = adminConfigService;
        this.busStopRepository = busStopRepository;
        this.busRouteRepository = busRouteRepository;
        this.routeStopMappingRepository = routeStopMappingRepository;
        this.workflowTaskRepository = workflowTaskRepository;
        this.notificationTemplateRepository = notificationTemplateRepository;
    }

    @org.springframework.beans.factory.annotation.Value("${admin.initial.password:}")
    private String adminInitialPassword;

    @Override
    public void run(String... args) {
        adminConfigService.ensureDefaults();
        if (userRepository.findByUsername("admin").isEmpty()) {
            if (adminInitialPassword == null || adminInitialPassword.isBlank()) {
                log.warn("[Trace: INIT] Admin user not created. ADMIN_INITIAL_PASSWORD is not set.");
            } else {
                UserEntity admin = new UserEntity();
                admin.setUsername("admin");
                admin.setPasswordHash(passwordEncoder.encode(adminInitialPassword));
                admin.setRole(UserRole.ADMIN);
                userRepository.save(admin);
                log.info("[Trace: INIT] Default admin created successfully.");
            }
        }
        
        if (userRepository.findByUsername("dispatcher").isEmpty()) {
            if (adminInitialPassword == null || adminInitialPassword.isBlank()) {
                log.warn("[Trace: INIT] Dispatcher user not created. ADMIN_INITIAL_PASSWORD is not set.");
            } else {
                UserEntity dispatcher = new UserEntity();
                dispatcher.setUsername("dispatcher");
                dispatcher.setPasswordHash(passwordEncoder.encode(adminInitialPassword));
                dispatcher.setRole(UserRole.DISPATCHER);
                userRepository.save(dispatcher);
                log.info("[Trace: INIT] Default dispatcher created successfully.");
            }
        }
        seedSearchData();
        seedWorkflowData();
        seedNotificationTemplates();
    }

    private void seedSearchData() {
        BusStop central = busStopRepository.findByNameEn("Central Avenue").orElseGet(() -> {
            BusStop stop = new BusStop();
            stop.setNameEn("Central Avenue");
            stop.setPinyinInitials("CA");
            stop.setPopularityScore(50.0);
            return busStopRepository.save(stop);
        });

        BusRoute b12 = busRouteRepository.findByRouteNumber("B12").orElseGet(() -> {
            BusRoute route = new BusRoute();
            route.setRouteNumber("B12");
            route.setFrequencyPriority(10.0);
            return busRouteRepository.save(route);
        });

        BusRoute k66 = busRouteRepository.findByRouteNumber("K66").orElseGet(() -> {
            BusRoute route = new BusRoute();
            route.setRouteNumber("K66");
            route.setFrequencyPriority(8.0);
            return busRouteRepository.save(route);
        });

        if (routeStopMappingRepository.countByRoute_IdAndStop_Id(b12.getId(), central.getId()) == 0) {
            RouteStopMapping map = new RouteStopMapping();
            map.setRoute(b12);
            map.setStop(central);
            routeStopMappingRepository.save(map);
        }
        if (routeStopMappingRepository.countByRoute_IdAndStop_Id(k66.getId(), central.getId()) == 0) {
            RouteStopMapping map = new RouteStopMapping();
            map.setRoute(k66);
            map.setStop(central);
            routeStopMappingRepository.save(map);
        }
    }

    private void seedWorkflowData() {
        if (workflowTaskRepository.count() > 0) {
            return;
        }
        WorkflowTask a = new WorkflowTask();
        a.setTitle("Route data change approval - Central Avenue");
        a.setType(TaskType.ROUTE_CHANGE);
        a.setStatus(TaskStatus.PENDING);
        a.setHighImpact(true);
        a.setProgress(55);
        workflowTaskRepository.save(a);

        WorkflowTask b = new WorkflowTask();
        b.setTitle("Reminder rule configuration review");
        b.setType(TaskType.REMINDER_RULE);
        b.setStatus(TaskStatus.PENDING);
        b.setHighImpact(false);
        b.setProgress(35);
        workflowTaskRepository.save(b);

        WorkflowTask c = new WorkflowTask();
        c.setTitle("Abnormal data batch review");
        c.setType(TaskType.ABNORMAL_DATA_REVIEW);
        c.setStatus(TaskStatus.PENDING);
        c.setHighImpact(false);
        c.setProgress(20);
        workflowTaskRepository.save(c);
    }

    private void seedNotificationTemplates() {
        if (notificationTemplateRepository.findByTemplateKey("ARRIVAL_REMINDER").isEmpty()) {
            NotificationTemplate t = new NotificationTemplate();
            t.setTemplateKey("ARRIVAL_REMINDER");
            t.setTemplateBody("Reminder: Your bus to {stopName} arrives in {leadTimeMinutes} minutes.");
            notificationTemplateRepository.save(t);
        }
        if (notificationTemplateRepository.findByTemplateKey("MISSED_CHECKIN").isEmpty()) {
            NotificationTemplate t = new NotificationTemplate();
            t.setTemplateKey("MISSED_CHECKIN");
            t.setTemplateBody("Missed check-in detected for {stopName}.");
            notificationTemplateRepository.save(t);
        }
    }
}