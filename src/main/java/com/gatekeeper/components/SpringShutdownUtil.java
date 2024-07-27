package com.gatekeeper.components;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 *
 * @author null
 */
@Component
public class SpringShutdownUtil {

    private final ApplicationContext appContext;

    public SpringShutdownUtil(ApplicationContext appContext) {
        this.appContext = appContext;
    }

    public void shutDownSpringApp() {
        SpringApplication.exit(appContext, () -> 1);
        return;
    }

}
