package ru.cs_consult.datamodelbrowseaddon;

import io.jmix.core.annotation.JmixModule;
import io.jmix.core.impl.scanning.AnnotationScanMetadataReaderFactory;
import io.jmix.eclipselink.EclipselinkConfiguration;
import io.jmix.ui.UiConfiguration;
import io.jmix.ui.sys.ActionsConfiguration;
import io.jmix.ui.sys.UiControllersConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.Collections;

@Configuration
@ComponentScan
@ConfigurationPropertiesScan
@JmixModule(dependsOn = {EclipselinkConfiguration.class, UiConfiguration.class})
@PropertySource(name = "ru.cs_consult.datamodelbrowseaddon", value = "classpath:/ru/cs_consult/datamodelbrowseaddon/module.properties")
public class DmbConfiguration {

    @Bean("dmb_DmbUiControllers")
    public UiControllersConfiguration screens(ApplicationContext applicationContext,
                                              AnnotationScanMetadataReaderFactory metadataReaderFactory) {
        UiControllersConfiguration uiControllers =
                new UiControllersConfiguration(applicationContext, metadataReaderFactory);
        uiControllers.setBasePackages(Collections.singletonList("ru.cs_consult.datamodelbrowseaddon"));
        return uiControllers;
    }

    @Bean("dmb_DmbUiActions")
    public ActionsConfiguration actions(ApplicationContext applicationContext,
                                        AnnotationScanMetadataReaderFactory metadataReaderFactory) {
        ActionsConfiguration actionsConfiguration =
                new ActionsConfiguration(applicationContext, metadataReaderFactory);
        actionsConfiguration.setBasePackages(Collections.singletonList("ru.cs_consult.datamodelbrowseaddon"));
        return actionsConfiguration;
    }

    @Configuration
    @ConfigurationProperties(prefix = "jmix.datamodelview")
    public static class ApplicationProperties {

        private String excludedMetaClasses;

        public String getExcludedMetaClasses() {
            return excludedMetaClasses;
        }

        public void setExcludedMetaClasses(String excludedMetaClasses) {
            this.excludedMetaClasses = excludedMetaClasses;
        }
    }
}
