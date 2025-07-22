package ru.cs_consult.datamodelbrowseaddon.app;

import freemarker.ext.beans.BeansWrapper;
import freemarker.template.*;
import io.jmix.core.*;
import io.jmix.core.accesscontext.CrudEntityContext;
import io.jmix.core.accesscontext.EntityAttributeContext;
import io.jmix.core.metamodel.annotation.Comment;
import io.jmix.core.metamodel.datatype.Enumeration;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

public class DataModelMetaClassRepresentation {
    private final MetaClass metaClass;
    private final Metadata metadata;
    private final MetadataTools metadataTools;
    private final Messages messages;
    private final MessageTools messageTools;
    private final AccessManager accessManager;
    private final FetchPlanRepository fetchPlanRepository;

    public DataModelMetaClassRepresentation(MetaClass metaClass, Metadata metadata, MetadataTools metadataTools, Messages messages, MessageTools messageTools, AccessManager accessManager, FetchPlanRepository fetchPlanRepository) {
        this.metaClass = metaClass;
        this.metadata = metadata;
        this.metadataTools = metadataTools;
        this.messages = messages;
        this.messageTools = messageTools;
        this.accessManager = accessManager;
        this.fetchPlanRepository = fetchPlanRepository;
    }

    public String getTableName() {
        boolean isEmbeddable = metaClass.getJavaClass().isAnnotationPresent(Embeddable.class);
        if (isEmbeddable) {
            return "not defined for embeddable entities";
        } else {
            String databaseTable = metadataTools.getDatabaseTable(metaClass);
            return databaseTable != null ? databaseTable : "not defined";
        }
    }

    public String getName() {
        return metaClass.getName();
    }

    public String getParent() {
        MetaClass ancestor = metaClass.getAncestor();
        if (ancestor != null && (ancestor.getName().contains("$") || ancestor.getName().contains("_")) && !ancestor.getJavaClass().isAnnotationPresent(MappedSuperclass.class)) {
            return !readPermitted(ancestor) ? null : "Parent is " + asHref(ancestor.getName());
        } else {
            return "";
        }
    }

    public String getDescription() {
        return messageTools.getEntityCaption(metaClass);
    }

    public String getComment() {
        Comment comment = metaClass.getJavaClass().getAnnotation(Comment.class);
        return comment != null ? comment.value() : null;
    }

    public Collection<MetaClassRepProperty> getProperties() {
        List<MetaClassRepProperty> result = new ArrayList<>();

        for(MetaProperty property : metaClass.getProperties()) {
            MetaProperty.Type propertyType = property.getType();
            if (attrFetchPlanPermitted(metaClass, property.getName())) {
                if (propertyType == MetaProperty.Type.COMPOSITION || propertyType == MetaProperty.Type.ASSOCIATION) {
                    MetaClass propertyMetaClass = property.getRange().asClass();
                    if (!readPermitted(propertyMetaClass)) {
                        continue;
                    }
                }

                MetaClassRepProperty prop = new MetaClassRepProperty(property);
                result.add(prop);
            }
        }

        return result;
    }

    public Collection<MetaClassRepFetchPlan> getFetchPlans() {
        Collection<String> fetchPlanNames = fetchPlanRepository.getFetchPlanNames(metaClass);
        List<FetchPlan> metaClassFetchPlans = fetchPlanNames.stream()
                .map(e -> fetchPlanRepository.getFetchPlan(metaClass, e)).collect(Collectors.toList());
        return metaClassFetchPlans.stream().filter(this::fetchPlanAccessPermitted).map(MetaClassRepFetchPlan::new).collect(Collectors.toList());
    }

    private boolean fetchPlanAccessPermitted(FetchPlan fetchPlan) {
        return readPermitted(metadata.getClass(fetchPlan.getEntityClass()));
    }

    private boolean fetchPlanPropertyReadPermitted(MetaClass meta, FetchPlanProperty fetchPlanProperty) {
        if (!attrFetchPlanPermitted(meta, fetchPlanProperty.getName())) {
            return false;
        } else {
            MetaProperty metaProperty = meta.getProperty(fetchPlanProperty.getName());
            if (metaProperty.getType() != MetaProperty.Type.DATATYPE && metaProperty.getType() != MetaProperty.Type.ENUM) {
                MetaClass propertyMeta = metaProperty.getRange().asClass();
                return readPermitted(propertyMeta);
            } else {
                return true;
            }
        }
    }

    private boolean attrFetchPlanPermitted(MetaClass meta, String property) {
        return attrPermitted(meta, property);
    }

    private boolean attrPermitted(MetaClass meta, String property) {
        EntityAttributeContext entityAttributeContext  = new EntityAttributeContext(meta, property);
        accessManager.applyRegisteredConstraints(entityAttributeContext);
        return entityAttributeContext.canView();
    }

    private boolean readPermitted(MetaClass meta) {
        return entityOpPermitted(meta);
    }

    private boolean entityOpPermitted(MetaClass meta) {
        CrudEntityContext crudEntityContext  = new CrudEntityContext(meta);
        accessManager.applyRegisteredConstraints(crudEntityContext);
        return crudEntityContext.isReadPermitted();
    }

    private String asHref(String element) {
        return "<a href=\"#" + element + "\">" + element + "</a>";
    }

    public class MetaClassRepProperty {
        private final MetaProperty property;

        public MetaClassRepProperty(MetaProperty property) {
            this.property = property;
        }

        public String getColumnName() {
            Column column = property.getAnnotatedElement().getAnnotation(Column.class);
            if (column != null) {
                return column.name();
            } else {
                JoinColumn joinColumn = property.getAnnotatedElement().getAnnotation(JoinColumn.class);
                return joinColumn != null ? joinColumn.name() : "";
            }
        }

        public String getName() {
            return property.getName();
        }

        public String getDescription() {
            return messageTools.getPropertyCaption(property.getDomain(), property.getName());
        }

        public String getEnum() {
            return property.getRange().isEnum() ? asHref(property.getRange().asEnumeration().toString()) : null;
        }

        public TemplateHashModel getEnumValues() {
            if (!property.getRange().isEnum()) {
                return null;
            } else {
                Enumeration<?> enumeration = property.getRange().asEnumeration();
                SimpleHash wrappedEnum = new SimpleHash((ObjectWrapper) null);
                wrappedEnum.put("name", enumeration.toString());
                SimpleSequence values = new SimpleSequence((ObjectWrapper) null);

                for(Enum<?> enumItem : enumeration.getValues()) {
                    SimpleHash wrappedEnumElement = new SimpleHash((ObjectWrapper) null);

                    try {
                        wrappedEnumElement.put("idObj", new BeansWrapper(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS).wrap(enumItem));
                        wrappedEnumElement.put("name", messages.getMessage(enumItem));
                        values.add(wrappedEnumElement);
                    } catch (TemplateModelException e) {
                        throw new RuntimeException(e.getMessage());
                    }
                }

                wrappedEnum.put("values", values);
                return wrappedEnum;
            }
        }

        public String getJavaType() {
            String type = property.getJavaType().getName();
            String simpleName = property.getJavaType().getSimpleName();
            return (!type.startsWith("java.lang.") || "java.lang.".length() + simpleName.length() != type.length()) && !type.startsWith("[") ?
                    (property.getRange().isClass() ? asHref(property.getRange().asClass().getName()) : type) : simpleName;
        }

        public String getCardinality() {
            switch (property.getRange().getCardinality()) {
                case NONE:
                    return "";
                case ONE_TO_ONE:
                    return property.getRange().isClass() ? "1:1" : "";
                case ONE_TO_MANY:
                    return "1:N";
                case MANY_TO_ONE:
                    return "N:1";
                case MANY_TO_MANY:
                    return "N:N";
                default:
                    return property.getRange().getCardinality().toString();
            }
        }

        public Collection<String> getAnnotations() {
            List<String> result = new ArrayList<>();
            Map<String, Object> map = property.getAnnotations();

            for(Map.Entry<String, Object> entry : map.entrySet()) {
                String annotationName = entry.getKey();
                if (!"jmix.length".equals(annotationName) || String.class.equals(property.getJavaType())) {
                    if (Boolean.TRUE.equals(entry.getValue())) {
                        result.add(annotationName);
                    } else {
                        result.add(annotationName + ": " + entry.getValue());
                    }
                }
            }

            Collections.sort(result);
            return result;
        }

    }

    public class MetaClassRepFetchPlan {
        private final FetchPlan fetchPlan;

        public MetaClassRepFetchPlan(FetchPlan fetchPlan) {
            this.fetchPlan = fetchPlan;
        }

        public String getName() {
            return fetchPlan.getName();
        }

        public Collection<MetaClassRepFetchPlanProperty> getProperties() {
            Collection<MetaClassRepFetchPlanProperty> result = new ArrayList<>();
            MetaClass meta = metadata.getClass(fetchPlan.getEntityClass());

            for(FetchPlanProperty property : fetchPlan.getProperties()) {
                if (fetchPlanPropertyReadPermitted(meta, property)) {
                    result.add(new MetaClassRepFetchPlanProperty(property));
                }
            }

            return result;
        }
    }

    public class MetaClassRepFetchPlanProperty {
        private final FetchPlanProperty property;

        public MetaClassRepFetchPlanProperty(FetchPlanProperty property) {
            this.property = property;
        }

        public String getName() {
            return property.getName();
        }

        public MetaClassRepFetchPlan getFetchPlan() {
            return property.getFetchPlan() == null ? null : new MetaClassRepFetchPlan(property.getFetchPlan());
        }
    }
}
