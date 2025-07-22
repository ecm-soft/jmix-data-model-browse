package ru.cs_consult.datamodelbrowseaddon.app;

import freemarker.cache.StringTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;

import java.io.StringWriter;
import java.util.Map;

public class DataModelTemplateHelper {
    public static String processTemplate(String templateStr, Map<String, ?> parameterValues) {
        StringTemplateLoader templateLoader = new StringTemplateLoader();
        String templateName = "template";
        templateLoader.putTemplate(templateName, templateStr);
        return __processTemplate(templateLoader, templateName, parameterValues);
    }

    protected static String __processTemplate(TemplateLoader templateLoader, String templateName, Map<String, ?> parameterValues) {
        StringWriter writer = new StringWriter();

        try {
            Configuration configuration = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
            configuration.setTemplateLoader(templateLoader);
            Template template = configuration.getTemplate(templateName);
            template.process(parameterValues, writer);
            return writer.toString();
        } catch (Throwable e) {
            throw new RuntimeException("Unable to process template", e);
        }
    }
}
