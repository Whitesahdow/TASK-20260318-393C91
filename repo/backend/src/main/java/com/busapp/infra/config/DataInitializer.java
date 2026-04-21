package com.busapp.infra.config;

import com.busapp.model.UserEntity;
import com.busapp.model.UserRole;
import com.busapp.model.BusRoute;
import com.busapp.model.BusStop;
import com.busapp.model.RouteStopMapping;
import com.busapp.repository.BusRouteRepository;
import com.busapp.repository.BusStopRepository;
import com.busapp.repository.RouteStopMappingRepository;
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

    public DataInitializer(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AdminConfigService adminConfigService,
            BusStopRepository busStopRepository,
            BusRouteRepository busRouteRepository,
            RouteStopMappingRepository routeStopMappingRepository
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminConfigService = adminConfigService;
        this.busStopRepository = busStopRepository;
        this.busRouteRepository = busRouteRepository;
        this.routeStopMappingRepository = routeStopMappingRepository;
    }

    @Override
    public void run(String... args) {
        adminConfigService.ensureDefaults();
        if (userRepository.findByUsername("admin").isEmpty()) {
            UserEntity admin = new UserEntity();
            admin.setUsername("admin");
            admin.setPasswordHash(passwordEncoder.encode("admin1234"));
            admin.setRole(UserRole.ADMIN);
            userRepository.save(admin);
            log.info("[Trace: INIT] Default admin created: admin/admin1234");
        }
        seedSearchData();
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
}