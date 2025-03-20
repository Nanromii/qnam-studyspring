package vn.qnam.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.qnam.model.InvalidatedToken;
import vn.qnam.model.Role;
import vn.qnam.model.User;
import vn.qnam.repository.InvalidatedRepository;
import vn.qnam.repository.UserRepository;
import vn.qnam.util.Scope;
import vn.qnam.util.UserStatus;

import java.time.Instant;
import java.util.Date;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class ApplicationInitConfig {
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.valid-duration}")
    private int VALID_DURATION;

    @Bean
    protected ApplicationRunner applicationRunner(UserRepository userRepository, InvalidatedRepository invalidatedRepository) {
        return args -> {
            List<InvalidatedToken> expiredTokens = invalidatedRepository.findByExpiryTimeLessThan(new Date());
            if (!expiredTokens.isEmpty()) {
                invalidatedRepository.deleteAll(expiredTokens);
                log.info("{} expired tokens have been removed.", expiredTokens.size());
            }

            Date oneHourAgo = Date.from(Instant.now().minusSeconds(VALID_DURATION));
            List<User> inactiveUser = userRepository.findUserByStatusAndCreateAtLessThan(
                    UserStatus.valueOf(UserStatus.INACTIVE.name()),
                    oneHourAgo
            );
            if (!inactiveUser.isEmpty()) {
                userRepository.deleteAll(inactiveUser);
                log.info("{} users who exceeded the activation deadline has been deleted..", inactiveUser.size());
            }

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
