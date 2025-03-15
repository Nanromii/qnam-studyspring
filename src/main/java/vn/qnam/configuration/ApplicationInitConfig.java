package vn.qnam.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.qnam.model.Role;
import vn.qnam.model.User;
import vn.qnam.repository.UserRepository;
import vn.qnam.util.Scope;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class ApplicationInitConfig {
    private final PasswordEncoder passwordEncoder;

    @Bean
    protected ApplicationRunner applicationRunner(UserRepository userRepository) {
        return args -> {
            if (userRepository.findUserByUserName("admin").isEmpty()) {
                User user = User.builder()
                        .firstName("admin")
                        .userName("admin")
                        .password(passwordEncoder.encode("admin"))
                        .role(Role.builder().name(Scope.ADMIN.toString()).build())
                        .build();
                userRepository.save(user);
                log.warn("admin user has been created with default password: admin, please change it.");
            }
        };
    }
}
