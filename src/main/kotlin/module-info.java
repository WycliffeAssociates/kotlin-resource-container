module org.wycliffeassociates.resourcecontainer {
    requires java.base;
    requires kotlin.stdlib;

    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.dataformat.yaml;
    requires jackson.module.kotlin;

    exports org.wycliffeassociates.resourcecontainer;
    exports org.wycliffeassociates.resourcecontainer.entity;
    exports org.wycliffeassociates.resourcecontainer.errors;
}