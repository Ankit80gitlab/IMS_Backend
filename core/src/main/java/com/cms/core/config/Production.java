package com.cms.core.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

@Profile("production")
@Configuration
@PropertySource("file:${app.home}/core.properties")
@Import({CoreCommonConfig.class})
public class Production {
}
