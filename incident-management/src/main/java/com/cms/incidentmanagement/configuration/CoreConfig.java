package com.cms.incidentmanagement.configuration;

import com.cms.core.config.Dev;
import com.cms.core.config.Production;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Import({Dev.class, Production.class})
@Configuration
public class CoreConfig {
}
