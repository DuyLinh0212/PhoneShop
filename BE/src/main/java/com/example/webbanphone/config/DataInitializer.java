package com.example.webbanphone.config;

import com.example.webbanphone.entities.Role;
import com.example.webbanphone.entities.User;
import com.example.webbanphone.repositories.RoleRepository;
import com.example.webbanphone.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final String ADMIN_EMAIL = "admin@phoneshop.vn";
    private static final String DEFAULT_ADMIN_PASSWORD = "Admin@123";

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            RoleRepository roleRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        Role adminRole = roleRepository.findByNameIgnoreCase("admin")
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName("admin");
                    role.setDescription("Quản trị viên hệ thống");
                    return roleRepository.save(role);
                });

        User admin = userRepository.findByEmailIgnoreCase(ADMIN_EMAIL)
                .orElseGet(() -> {
                    User user = new User();
                    user.setEmail(ADMIN_EMAIL);
                    user.setFullName("Super Admin");
                    user.setPhone("0901234567");
                    user.setIsActive(true);
                    return user;
                });

        admin.setRole(adminRole);
        admin.setIsActive(true);

        admin.setPasswordHash(passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD));

        userRepository.save(admin);
    }
}
