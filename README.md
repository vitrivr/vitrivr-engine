# vitrivr engine

[vitrivr](https://vitrivr.org)'s next-generation retrieval engine.
Based on the experiences with its predecessor, [Cineast](https://github.com/vitrivr/cineast),
vitrivr engine's data model, ingestion pipeline and retrieval logic have been reworked.

## Project Structure

The project is set up as a multi-module Kotlin project:

* [`vitrivr-engine-core`](/vitrivr-engine-core) - The core library of the project (also published on maven as a library)
* [`vitrivr-engine-index`](/vitrivr-engine-index) - Indexing / Ingestion related core library
* [`vitrivr-engine-plugin-cottontaildb`](/vitrivr-engine-plugin-cottontaildb) - The java module for the column-store and kNN faciliating databse, [CottontailDB](https://github.com/vitrivr/cottontaildb)
* [`vitrivr-engine-plugin-features`](/vitrivr-engine-plugin-features) - The java module which provides specific indexing and retrieval implementations such as fulltext, colour, etc.
* [`vitrivr-engine-plugin-m3d`](/vitrivr-engine-plugin-m3d) - The in-project plugin related to 3d model indexing and retrieval
* [`vtirivr-engine-query`](/vitrivr-engine-query) - Query / Retrieval related core library
* [`vitrivr-engine-server`](/vitrivr-engine-server) - A [Javalin](https://javalin.io) powered server providing an [OpenApi](https://openapis.org) [documented REST API](vitrivr-engine-server/doc/oas.json) for both, ingestion and querying and a CLI, essentially the runtime of the project

## Getting Started: Usage

vitrivr engine is a Kotlin project and hence requires a JDK (e.g. [OpenJDK](https://openjdk.org/)) to properly run.
Furthermore, we use [Gradle](https://gradle.org) in order to facilitate the building and deployment
through the Gradle wrapper.

In the context of retrieval, often times a distinction of _indexing_ / _ingestion_ (also known as _offline phase_)
and _querying_ / _retrieval_ (also known as _online phase_) is made.
While the former addresses (multimedia) content to be analysed and indexed, i.e. made ready for search, is the latter's purpose to
search within the previously built index.

### Indexing / Ingestion

The most essential prerequisite for the ingestion is the existence of multimedia content.
For the sake of this example, let's assume the existence of such multimedia content in the form of image and video files.

Also, since vitrivr engine is highly configurable, the first few steps involve the creation of a suitable
configuration.

#### Schema

vitrivr engine operates on the notion of _schema_, similarly to a database or a collection, 
essentially providing, among other things, a namespace.
For this guide, we will have a single schema `sandbox`.

Create a config file `sandbox-config.json` with one named schema in it:

```json
{
  "schemas": [{
    "name": "sandbox"
  }]
}
```

#### Database

The database is also a rather important component of the system. 
This guide assumes a running [CottontailDB](https://github.com/vitrivr/cottontaildb)
instance on the same machine on the default port `1865`.
We address the database with the [`ConnectionConfig`](/vitrivr-engine-core/src/main/kotlin/org/vitrivr/engine/core/config/ConnectionConfig.kt):

```json
{
  "database": "CottontailConnectionProvider",
  "parameters": {
    "Host": "127.0.0.1",
    "port": "1865"
  }
}
```

We add the cottontail connection to the schema's connection property:

```json
{
  "schemas": [{
    "name": "sandbox",
    "connection": {
      "database": "CottontailConnectionProvider",
      "parameters": {
        "Host": "127.0.0.1",
        "port": "1865"
      }
    }
  }]
}
```

#### Analyser

The [`Analyser`](/vitrivr-engine-core/src/main/kotlin/org/vitrivr/engine/core/model/metamodel/Analyser.kt)
performs analysis to derive a [`Descriptor`](/vitrivr-engine-core/src/main/kotlin/org/vitrivr/engine/core/model/descriptor/Descriptor.kt).
In other words, the _analyser_ produces a _descriptor_ which represents the media content analysed.
However, this is only for _indexing_ / _ingestion_. During _querying_ / _retrieval_ time, 
the _analyser_ queries the underlying storage layer to perform a query on said _descriptors_.

#### Field Configuration

A schema consists of unique-named _fields_, that have to be backed by an [`Analyser`](/vitrivr-engine-core/src/main/kotlin/org/vitrivr/engine/core/model/metamodel/Analyser.kt),
essentially representing a specific [`Descriptor`](/vitrivr-engine-core/src/main/kotlin/org/vitrivr/engine/core/model/descriptor/Descriptor.kt).
This is configured using the [`FieldConfig`](/vitrivr-engine-core/src/main/kotlin/org/vitrivr/engine/core/config/FieldConfig.kt):

```json
{
  "name": "uniqueName",
  "factory": "FactoryClass",
  "parameters":{
    "key": "value"
  }
}
```

For images (and video frames), it might be worthwhile to use the average colour for representation purposes.
The built-in [`AverageColor`](/vitrivr-engine-plugin-features/src/main/kotlin/org/vitrivr/engine/base/features/averagecolor/AverageColor.kt)
analyser can be facilitated for this endeavour.
To use it, we specifically craft a corresponding _field config_:

```json
{
  "name": "averagecolor",
  "factory": "AverageColor"
}
```

There are no additional parameters, unlike, for instance, an [`ExternalAnalyser`](/vitrivr-engine-plugin-features/src/main/kotlin/org/vitrivr/engine/base/features/external/ExternalAnalyser.kt),
which requires the parameter `host` with an endpoint as value.

Other fields are for (technical) metadata such as the [`FileSourceMetadata`](/vitrivr-engine-core/src/main/kotlin/org/vitrivr/engine/core/features/metadata/source/file/FileSourceMetadata.kt),
which additionally stores the file's path and size.

Currently, there is no list of available fields and analysers, therefore a quick look into the code
reveals those existent. For basic (metadata), see in [the core module](/vitrivr-engine-core/src/main/kotlin/org/vitrivr/engine/core/features/),
for content-based features, see in [the features' module](/vitrivr-engine-plugin-features/src/main/kotlin/org/vitrivr/engine/base/features/).

In this guide, we use the _average colour_ and _file source_ fields, which results in the (currently) following
configuration file:

```json
{
  "schemas": [{
    "name": "sandbox",
    "connection": {
      "database": "CottontailConnectionProvider",
      "parameters": {
        "Host": "127.0.0.1",
        "port": "1865"
      }
    },
    "fields": [
      {
        "name": "averagecolor",
        "factory": "AverageColor"
      },
      {
        "name": "file",
        "factory": "FileSourceMetadata"
      }
    ]
  }]
}
```

#### Exporter Configuration

In the context of images and videos, having thumbnails is desirable, which can be generated
during ingestion with the configuration of an _exporter_.
Generally speaking, an _exporter_ exports an artifact based on the media content.

The [`ExporterConfig`](/vitrivr-engine-core/src/main/kotlin/org/vitrivr/engine/core/config/ExporterConfig.kt)
describes such a configuration:

```json
{
  "name": "uniqueName",
  "factory": "FactoryClass",
  "resolver": {
    "factory": "ResolverFactoryClass"
  },
  "parameters": {
    "key": "value"
  }
}
```

Specifically, the [`ThumbnailExporter`](/vitrivr-engine-index/src/main/kotlin/org/vitrivr/engine/index/exporters/ThumbnailExporter.kt),
can be configured as follows, which uses a [`DiskResolver`](/vitrivr-engine-index/src/main/kotlin/org/vitrivr/engine/index/resolvers/DiskResolver.kt).

```json
{
    "name": "thumbnail",
    "factory": "ThumbnailExporter",
    "resolver": {
      "factory": "DiskResolver",
      "parameters": {
        "location": "./thumbnails/sandbox"
      }
    },
    "parameters": {
      "maxSideResolution": "400",
      "mimeType": "JPG"
    }
}
```

Resulting in the following schema config:

```json
{
  "schemas": [{
    "name": "sandbox",
    "connection": {
      "database": "CottontailConnectionProvider",
      "parameters": {
        "Host": "127.0.0.1",
        "port": "1865"
      }
    },
    "fields": [
      {
        "name": "averagecolor",
        "factory": "AverageColor"
      },
      {
        "name": "file",
        "factory": "FileSourceMetadata"
      }
    ],
    "exporters": [
      {
        "name": "thumbnail",
        "factory": "ThumbnailExporter",
        "resolver": {
          "factory": "DiskResolver",
          "parameters": {
            "location": "./thumbnails/sandbox"
          }
        },
        "parameters": {
          "maxSideResolution": "400",
          "mimeType": "JPG"
        }
      }
    ]
  }]
}
```

#### Extraction Pipeline Configuration

In order to effectively support a specific _ingestion_ / _indexing_, we have to provide
a reference to the [`IndexConfig`](/vitrivr-engine-core/src/main/kotlin/org/vitrivr/engine/core/config/IndexConfig.kt),
which is configured as a [`PipelineConfig`](/vitrivr-engine-core/src/main/kotlin/org/vitrivr/engine/core/config/PipelineConfig.kt) within the schema config:

```json
{
  "name": "pipelineName",
  "path": "path/to/config-pipeline.json"
}
```

We will create said _index config_ later as `sandbox-pipeline.json`, hence, our schema config
is as follows:

```json
{
  "schemas": [{
    "name": "sandbox",
    "connection": {
      "database": "CottontailConnectionProvider",
      "parameters": {
        "Host": "127.0.0.1",
        "port": "1865"
      }
    },
    "fields": [
      {
        "name": "averagecolor",
        "factory": "AverageColor"
      },
      {
        "name": "file",
        "factory": "FileSourceMetadata"
      }
    ],
    "exporters": [
      {
        "name": "thumbnail",
        "factory": "ThumbnailExporter",
        "resolver": {
          "factory": "DiskResolver",
          "parameters": {
            "location": "./thumbnails/sandbox"
          }
        },
        "parameters": {
          "maxSideResolution": "400",
          "mimeType": "JPG"
        }
      }
    ],
    "extractionPipelines": [
      {
        "name": "sandboxpipeline",
        "path": "./sandbox-pipeline.json"
      }
    ]
  }]
}
```

#### Index Pipeline Configuration

Let's create a new file `sandbox-pipeline.json` right next to the `sandbox-config.json` in the root of the project.
This file will contain the [`IndexConfig`](/vitrivr-engine-core/src/main/kotlin/org/vitrivr/engine/core/config/IndexConfig.kt).

In order to address (reference) our schema, we reference it in our index config and provide a _context_ as well as an _enumerator_:

```json
{
  "schema": "sandbox",
  "context": {
    
  },
  "enumerator": {
    
  }
}
```

#### Index Context Configuration



#### Complete Sandbox Configuration

The schema config:

```json
{
  "schemas": [{
    "name": "sandbox",
    "connection": {
      "database": "CottontailConnectionProvider",
      "parameters": {
        "Host": "127.0.0.1",
        "port": "1865"
      }
    },
    "fields": [
      {
        "name": "averagecolor",
        "factory": "AverageColor"
      },
      {
        "name": "file",
        "factory": "FileSourceMetadata"
      }
    ],
    "exporters": [
      {
        "name": "thumbnail",
        "factory": "ThumbnailExporter",
        "resolver": {
          "factory": "DiskResolver",
          "parameters": {
            "location": "./thumbnails/sandbox"
          }
        },
        "parameters": {
          "maxSideResolution": "400",
          "mimeType": "JPG"
        }
      }
    ],
    "extractionPipelines": [
      {
        "name": "sandboxpipeline",
        "path": "./sandbox-pipeline.json"
      }
    ]
  }]
}

```

The pipeline config:


### Querying / Retrieval

## Getting Started: Development

This is a Gradle-powered Kotlin project, we assume prerequisites are handled accordingly.

1. Generate the OpenApi client code by executing the (top-level) `generateOpenApi` gradle task
2. Start developing

If you develop another module (plugin), please keep in mind that the providers and factories are
exposed in `<your-module>/resources/META-INF/services/`

```
./gradlew openApiGenerate
```


