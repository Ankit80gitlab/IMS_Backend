package com.cms.core.config;

        import org.springframework.boot.autoconfigure.domain.EntityScan;
        import org.springframework.context.annotation.Configuration;
        import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
        import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement(proxyTargetClass = true)
@EnableJpaRepositories(basePackages = "com.cms.core.repository")
@EntityScan(basePackages = {"com.cms.core.entity"})
public class CoreCommonConfig {
}
