package com.cms.core;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;

@ComponentScan
@PropertySource(value = {"classpath:core.properties"})
public class CoreApplication {
}
