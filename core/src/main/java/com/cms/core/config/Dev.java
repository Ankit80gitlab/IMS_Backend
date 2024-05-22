package com.cms.core.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

@Profile({"dev"})
@Configuration
@PropertySource("classpath:core.properties")
@Import({CoreCommonConfig.class})
public class Dev {
}
