[![](https://jitpack.io/v/WycliffeAssociates/kotlin-resource-container.svg)](https://jitpack.io/#WycliffeAssociates/kotlin-resource-container)

This library implements the Door43 Content Services resource container 
specification for the kotlin language. This library provides kotlin type safe builders
for constructing a resource container.

# resource-container
A utility for interacting with Door43 Resource Containers. This follows the specification at http://resource-container.readthedocs.io/en/v0.2/.

## What is an RC?
A Resource Container (RC) is a modular/portable package of translation data.

## Installation
The preferred method of linking this library is to use https://jitpack.io
Specifically, https://jitpack.io/#WycliffeAssociates/kotlin-resource-container

First, add the followingto your build.gradle file:
```groovy
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

Then, in your dependencies, add:

```groovy
implementation 'com.github.WycliffeAssociates:kotlin-resource-container'
```

## Usage

ResourceContainer provides a create and load static method for creation.

### Create

```kotlin
val rc = ResourceContainer.create(File("./resource_container_dir")) {
    manifest = manifest {
        dublinCore = dublincore {
            conformsTo = "rc0.2"
            contributor = listOf("A Contributor", "Another Contributor")
            creator = "Someone or Organization"
            description = "One or two sentence description of the resource."
            format = "text/usfm"
            identifier = "ulb"
        }
        projects = listOf(
            project {
                    identifier = "gen"
                    title = "Genesis"
                    versification = "kjv"
                    sort = 1
                    path = "./gen"
                    categories = listOf("bible-ot")
            }
        )
    }
}
```

Note that some required fields of dublin_core are absent. All fields are optional
in the type safe builder. Omitted fields will be initialized to empty string or empty list,
with only Config being a nullable type.

### Read

```kotlin
    val container = ResourceContainer.load(containerDir)
```

### Using the ResourceContainer

Once created, the resource container exposes the manifest and config as mutable public fields.

To write changes:

```kotlin
    // write out all yaml files for the resource container and underlying projects
    // toc.yaml, and config.yaml files will be included if they exist
    container.write()

    // writes the manifest to manifest.yaml at the container's root
    container.writeManifest()

    // write the table of contents to toc.yaml in a project's directory
    container.writeTableOfContents(project: Project)

    // write config to config.yaml
    container.writeConfig()
```

### Config files

As configuration files do not follow a set specification, this library
only provides loose support for them. An interface is provided, pushing the burden of
implementing read and write functionality to the developer.

```kotlin
Interface Config {
    fun read(dir: File)
    fun write(dir: File)
}

val rc = ResourceContainer.load(config: Config, dir: File, strict: Boolean = true)

```

I recommend using a kotlin data class to model your config specification and using
Jackson's YAML and Kotlin modules for serializing and deserializing. 

The following is an example of how this library makes use of this library

Read:
```kotlin
val mapper = ObjectMapper(YAMLFactory())
mapper.registerModule(KotlinModule())
manifest = manifestFile.bufferedReader().use {
    mapper.readValue(it, Manifest::class.java)
}
```

Write:
```kotlin
val mapper = ObjectMapper(YAMLFactory())
mapper.registerModule(KotlinModule())
mapper.setSerializationInclusion(Include.NON_NULL)
val manifestFile = out.bufferedWriter().use {
    mapper.writeValue(it, manifest)
}
```