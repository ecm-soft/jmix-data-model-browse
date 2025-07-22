package ru.cs_consult.datamodelbrowseaddon;

import io.jmix.core.security.SystemAuthenticator;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

public class AuthAsAdmin implements BeforeEachCallback, AfterEachCallback {

    @Override
    public void beforeEach(ExtensionContext context) {
        getSystemAuthenticator(context).begin("admin");
    }

    @Override
    public void afterEach(ExtensionContext context) {
        getSystemAuthenticator(context).end();
    }

    private SystemAuthenticator getSystemAuthenticator(ExtensionContext context) {
        ApplicationContext applicationContext = SpringExtension.getApplicationContext(context);
        return applicationContext.getBean(SystemAuthenticator.class);
    }
}
