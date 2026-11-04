package com.gigcampus.config;

import com.gigcampus.entity.Role;
import com.gigcampus.entity.User;
import com.gigcampus.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class AdminBootstrap implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.bootstrap.enabled:true}")
    private boolean bootstrapEnabled;

    @Value("${app.admin.bootstrap.email:admin@gigcampus.local}")
    private String adminEmail;

    @Value("${app.admin.bootstrap.password:Admin@GigCampus2026}")
    private String adminPassword;

    @Value("${app.admin.bootstrap.name:Erode District Administrator}")
    private String adminName;

    @Value("${app.admin.bootstrap.phone:9876543210}")
    private String adminPhone;

    public AdminBootstrap(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (!bootstrapEnabled) {
            return;
        }

        long adminCount = userRepository.countByRole(Role.ROLE_ADMIN);
        if (adminCount == 0) {
            String cleanEmail = adminEmail.trim().toLowerCase();
            if (!userRepository.existsByEmail(cleanEmail)) {
                User admin = new User(
                        cleanEmail,
                        passwordEncoder.encode(adminPassword),
                        adminName.trim(),
                        adminPhone.trim(),
                        Role.ROLE_ADMIN
                );
                userRepository.save(admin);
                log.info("==========================================================");
                log.info("GigCampus: Initial Administrator Account bootstrapped safely.");
                log.info("Email: {}", cleanEmail);
                log.info("Password: [Configured via app.admin.bootstrap.password / ADMIN_BOOTSTRAP_PASSWORD]");
                log.info("==========================================================");
            }
        }
    }
}
