package ru.cs_consult.datamodelbrowseaddon.service;

import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModelException;
import io.jmix.core.*;
import io.jmix.core.accesscontext.CrudEntityContext;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.security.CurrentAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.cs_consult.datamodelbrowseaddon.DmbConfiguration;
import ru.cs_consult.datamodelbrowseaddon.app.DataModelMetaClassRepresentation;
import ru.cs_consult.datamodelbrowseaddon.app.DataModelTemplateHelper;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service(DataModelService.NAME)
public class DataModelServiceBean implements DataModelService {
    private static final String TEMPLATE_SUFFIX = "DataModel.html";

    @Autowired
    private Metadata metadata;
    @Autowired
    private Messages messages;
    @Autowired
    private MessageTools messageTools;
    @Autowired
    private FetchPlanRepository fetchPlanRepository;
    @Autowired
    private MetadataTools metadataTools;
    @Autowired
    private Resources resources;
    @Autowired
    private AccessManager accessManager;
    @Autowired
    private DatatypeRegistry datatypes;
    @Autowired
    private CurrentAuthentication currentAuthentication;
    @Autowired
    private DmbConfiguration.ApplicationProperties applicationProperties;


    @Override
    public String getDataModel() {
        List<DataModelMetaClassRepresentation> classes = new ArrayList<>();
        List<TemplateHashModel> enums = new ArrayList<>();
        Set<String> addedEnums = new HashSet<>();
        Set<MetaClass> metasSet = new HashSet<>(metadataTools.getAllJpaEntityMetaClasses());
        metasSet.addAll(metadataTools.getAllJpaEmbeddableMetaClasses());
        String excludedMetaClassesProperty = applicationProperties.getExcludedMetaClasses();
        List<String> excludedMetaClasses = Arrays.stream(excludedMetaClassesProperty.split(",")).map(String::trim).collect(Collectors.toList());
        metasSet = metasSet.stream().filter(metaClass -> !excludedMetaClasses.contains(metaClass.getName())).collect(Collectors.toSet());
        for (MetaClass meta : metasSet) {
            if (readPermitted(meta)) {
                DataModelMetaClassRepresentation rep = new DataModelMetaClassRepresentation(meta, metadata, metadataTools, messages, messageTools, accessManager, fetchPlanRepository);
                classes.add(rep);
                for (DataModelMetaClassRepresentation.MetaClassRepProperty metaProperty : rep.getProperties()) {
                    TemplateHashModel enumValues = metaProperty.getEnumValues();
                    if (enumValues != null && !addedEnums.contains(metaProperty.getJavaType())) {
                        addedEnums.add(metaProperty.getJavaType());
                        enums.add(enumValues);
                    }
                }
            }
        }
        classes.sort(Comparator.comparing(DataModelMetaClassRepresentation::getName));
        enums.sort((o1, o2) -> {
            try {
                return o1.get("name").toString().compareTo(o2.get("name").toString());
            } catch (TemplateModelException var4) {
                return 0;
            }
        });
        Map<String, Object> values = new HashMap<>();
        values.put("knownEntities", classes);
        String[] availableTypes = this.getAvailableBasicTypes();
        values.put("availableTypes", availableTypes);
        values.put("enums", enums);
        Locale locale = currentAuthentication.getLocale();
        String template = this.getModuleAvailableLocales().contains(locale) ?
            this.getTemplateForLocale(locale) : this.getTemplateForLocale(Locale.ENGLISH);
        return DataModelTemplateHelper.processTemplate(template, values);
    }

    public List<Locale> getModuleAvailableLocales() {
        Properties moduleProperties = new Properties();
        try (InputStream is = this.resources.getResourceAsStream("ru/cs_consult/datamodelbrowseaddon/module.properties")) {
            moduleProperties.load(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Arrays.stream(moduleProperties.getProperty("jmix.core.available-locales").split(","))
                .map(str -> LocaleResolver.resolve(str.trim())).collect(Collectors.toList());
    }

    public String getTemplateForLocale(Locale locale) {
        return this.resources.getResourceAsString(locale.getLanguage() + TEMPLATE_SUFFIX);
    }

    private boolean readPermitted(MetaClass metaClass) {
        CrudEntityContext accessContext = new CrudEntityContext(metaClass);
        accessManager.applyRegisteredConstraints(accessContext);
        return accessContext.isReadPermitted();
    }

    public String[] getAvailableBasicTypes() {
        Set<String> allAvailableTypes = datatypes.getIds();
        TreeSet<String> availableTypes = new TreeSet<>();
        for (String type : allAvailableTypes) {
            if (!"byteArray".equals(type)) {
                availableTypes.add(type);
            }
        }
        return availableTypes.toArray(String[]::new);
    }
}

