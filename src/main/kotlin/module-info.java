module kotlin.resource.container {
    requires kotlin.stdlib;
    requires kotlin.reflect;

    requires org.json;

    requires transitive com.fasterxml.jackson.core;
    requires transitive com.fasterxml.jackson.databind;
    requires transitive com.fasterxml.jackson.dataformat.yaml;
    requires transitive com.fasterxml.jackson.kotlin;

    exports org.wycliffeassociates.resourcecontainer;
}
