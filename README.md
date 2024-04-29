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

#### Schema Database

The database is also a rather important component of the system. 
This guide assumes a running [CottontailDB](https://github.com/vitrivr/cottontaildb)
instance on the same machine on the default port `1865`.

---
**NOTE this requires [Cottontail 0.16.5](https://github.com/vitrivr/cottontaildb/releases/tag/0.16.5) or newer**

---

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

#### Schema Analyser

The [`Analyser`](/vitrivr-engine-core/src/main/kotlin/org/vitrivr/engine/core/model/metamodel/Analyser.kt)
performs analysis to derive a [`Descriptor`](/vitrivr-engine-core/src/main/kotlin/org/vitrivr/engine/core/model/descriptor/Descriptor.kt).
In other words, the _analyser_ produces a _descriptor_ which represents the media content analysed.
However, this is only for _indexing_ / _ingestion_. During _querying_ / _retrieval_ time, 
the _analyser_ queries the underlying storage layer to perform a query on said _descriptors_.

#### Schema Field Configuration

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

For exif metadata, the [`ExifMetadata`](/vitrivr-engine-core/src/main/kotlin/org/vitrivr/engine/core/features/metadata/source/exif/ExifMetadata.kt) Analyser can be used. For each exif tag that should be included, a field parameter with the name "{EXIF_DIRECTORY_NAME}_{TAG_NAME}" must be set to a type. Keys that do not match an exif tag via the aforementioned pattern are interpreted to be custom metadata tags that are stored in the exif UserComment tag in JSON format. Here is an example with custom "time_zone" metadata:
```dtd
        {
          "name": "exif",
          "factory": "ExifMetadata",
          "parameters": {
            "ExifSubIFD_FocalLength": "INT",
            "ExifSubIFD_ApertureValue": "FLOAT",
            "ExifSubIFD_DateTimeOriginal": "DATETIME",
            "ExifSubIFD_MeteringMode": "STRING",
            "time_zone": "STRING"
          }
        }
```
For extraction, the exif UserComment of images might look like this:
```dtd
{"time_zone": "Europe/Berlin", "hours_awake": 12}
```

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

#### Schema Resolver Configuration

Some data is stored e.g. on disk during extraction, which later will also be required during query time,
therefore the [`Resolver`](vitrivr-engine-core/src/main/kotlin/org/vitrivr/engine/core/resolver/Resolver.kt)
is configured as the [`ResolverConfig`](vitrivr-engine-core/src/main/kotlin/org/vitrivr/engine/core/config/schema/ResolverConfig.kt)
on the schema with a unique name.

The [`ResolverConfig`](vitrivr-engine-core/src/main/kotlin/org/vitrivr/engine/core/config/schema/ResolverConfig.kt) describes such a configuration:

```json
{
  "factory": "FactoryClass",
  "parameters": {
    "key": "value"
  }
}
```

Specifically, the [`DiskResolver`](/vitrivr-engine-core/src/main/kotlin/org/vitrivr/engine/core/resolver/impl/DiskResolver.kt) is implemented and configured as such:

```json
{
  "factory": "DiskResolver",
  "parameters": {
    "location": "./thumbnails/vitrivr"
  }
}
```

Therefore, the _schema_ config is expanded with the _disk resolver_, named `disk`:

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
  }],
  "resolvers": {
    "disk": {
      "factory": "DiskResolver",
      "parameters": {
        "location": "./thumbnails/vitrivr"
      }
    }
  }
}
```

#### Schema Exporter Configuration

In the context of images and videos, having thumbnails is desirable, which can be generated
during ingestion with the configuration of an _exporter_.
Generally speaking, an _exporter_ exports an artifact based on the media content.

The [`ExporterConfig`](/vitrivr-engine-core/src/main/kotlin/org/vitrivr/engine/core/config/ExporterConfig.kt)
describes such a configuration:

```json
{
  "name": "uniqueName",
  "factory": "FactoryClass",
  "resolverName": "disk",
  "parameters": {
    "key": "value"
  }
}
```

Specifically, the [`ThumbnailExporter`](/vitrivr-engine-index/src/main/kotlin/org/vitrivr/engine/index/exporters/ThumbnailExporter.kt),
can be configured as follows, which references a [`DiskResolver`](/vitrivr-engine-core/src/main/kotlin/org/vitrivr/engine/core/resolver/impl/DiskResolver.kt) named `disk`, see the previous section.

```json
{
    "name": "thumbnail",
    "factory": "ThumbnailExporter",
    "resolverName": "disk",
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
    "resolvers": {
      "disk": {
        "factory": "DiskResolver",
        "parameters": {
          "location": "./thumbnails/vitrivr"
        }
      }
    },
    "exporters": [
      {
        "name": "thumbnail",
        "factory": "ThumbnailExporter",
        "resolverName": "disk",
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
  "path": "path/to/vbs-config-pipeline.json"
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
This file will contain the [`IngestionConfig`](/vitrivr-engine-core/src/main/kotlin/org/vitrivr/engine/core/config/ingest/IngestionConfig.kt).

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

An [`IngestionContextConfig`](/vitrivr-engine-core/src/main/kotlin/org/vitrivr/engine/core/context/IngestionContextConfig.kt)
is used to specify the _context_, additional information to the media data.
Specifically, a [`Resolver`](vitrivr-engine-core/src/main/kotlin/org/vitrivr/engine/core/resolver/Resolver.kt), `disk`, is referenced by its name from the _schema_ configuration.

```json
{
  "contentFactory": "InMemoryContentFactory",
  "resolverName": "disk",
  "global": {
    "key": "global-value"
  },
  "local": {
    "operator-name": {
      "key": "local-value"
    }
  }
}
```

The [`Context`](vitrivr-engine-core/src/main/kotlin/org/vitrivr/engine/core/context/Context.kt) has two properties: `global` and `local`, which
enable local and global configuration in the form of key-value pairs.
However, `local`ly defined key-value pairs override their `global`ly defined counterpart.
In the above example, for the _operator_ (see further below), the key-value pair with the key `key` is also locally defined,
resulting in the value `local-value` to be used. However, a separate operator named other than `operator-name`, would get the value `global-value`
for the key `key`, as no local value is defined.
We refer to this part of the context, to the _context-parameters_.

The example index pipeline configuration then, the current context looks as follow:

```json
{
  "schema": "sandbox",
  "context": {
    "contentFactory": "InMemoryContentFactory",
    "resolverName": "disk"
  },
  "enumerator": {

  }
}
```

There are these special sections for the context-parameters:

* `content` - For the local context-parameters for the _content factory_.

#### Index Operators Configuration

Next up, we declare a list of [operators](vitrivr-engine-core/src/main/kotlin/org/vitrivr/engine/core/operators/Operator.kt)
in the form of [`OperatorConfig`](vitrivr-engine-core/src/main/kotlin/org/vitrivr/engine/core/config/ingest/operator/OperatorConfig.kt)s.
These _operators_ must have a unique name in the `operators` property of the [`IngestionConfig`](vitrivr-engine-core/src/main/kotlin/org/vitrivr/engine/core/config/ingest/IngestionConfig.kt):
These names are used in the local context-parameters to configure them

```json
{
  "schema": "sandbox",
  "context": {
    "contentFactory": "InMemoryContentFactory",
    "resolverName": "disk",
    "local": {
      "enumerator": {
        "path": "./sandbox/imgs",
        "mediaTypes": "IMAGE;VIDEO",
        "depth": "1"
      },
      "myoperator1": {
        "key1": "a-value",
        "key2": "1234"
      }
    }
  },
  "operators": {
    "myoperator1": {},
    "myoperator2": {}
  }
}
```

There are different _types_ of operators:

* [`Enumerator`](vitrivr-engine-core/src/main/kotlin/org/vitrivr/engine/core/operators/ingest/Enumerator.kt) which emit items to ingest.
* [`Decoder`](vitrivr-engine-core/src/main/kotlin/org/vitrivr/engine/core/operators/ingest/Decoder.kt) which decode the file sources such that the content is available for ingestion.
* [`Segmenter`](vitrivr-engine-core/src/main/kotlin/org/vitrivr/engine/core/operators/ingest/Segmenter.kt) which segment incoming content and emit _n_ [`Retrievable`](vitrivr-engine-core/src/main/kotlin/org/vitrivr/engine/core/model/retrievable/Retrievable.kt)s, resulting in a 1:n mapping.
* [`Transformer`](vitrivr-engine-core/src/main/kotlin/org/vitrivr/engine/core/operators/ingest/Segmenter.kt), [`Extractor`](vitrivr-engine-core/src/main/kotlin/org/vitrivr/engine/core/operators/ingest/Extractor.kt), and [`Exporter`](vitrivr-engine-core/src/main/kotlin/org/vitrivr/engine/core/operators/ingest/Exporter.kt), which all process one retrievable and emit _one_ [`Retrievable`](vitrivr-engine-core/src/main/kotlin/org/vitrivr/engine/core/model/retrievable/Retrievable.kt)s, resulting in a 1:1 mapping.
* [`Aggregator`](vitrivr-engine-core/src/main/kotlin/org/vitrivr/engine/core/operators/ingest/Aggregator.kt) which aggregate _n_ incoming retrievables and emit one [`Retrievable`](vitrivr-engine-core/src/main/kotlin/org/vitrivr/engine/core/model/retrievable/Retrievable.kt)s, resulting in a n:1 mapping.

Notably, `Extractor`s are backed by a schema's field and `Exporter`s are also referenced by name from the _schema_.

In the following, we briefly introduce these configurations:

##### Index Operator Configuration: Enumerator

The _enumerator_ enumerates the content to index and provides it to the indexing pipeline.
It is described with a [`EnumeratorConfig](/vitrivr-engine-core/src/main/kotlin/org/vitrivr/engine/core/config/ingest/operator/OperatorConfig.kt).

Requires the property `mediaTypes`, a list of [`MediaType`](/vitrivr-engine-core/src/main/kotlin/org/vitrivr/engine/core/source/MediaType.kt)s.

```json
{
  "type": "ENUMERATOR",
  "factory": "FactoryClass",
  "mediaTypes": ["IMAGE","VIDEO"]
}
```

Currently implemented enumerators are found [in the index module](/vitrivr-engine-index/src/main/kotlin/org/vitrivr/engine/index/enumerate),
of which we will use the [`FileSystemEnumerator`](/vitrivr-engine-index/src/main/kotlin/org/vitrivr/engine/index/enumerate/FileSystemEnumerator.kt).

The configuration **requires** the context-parameter `path`, the path to the folder containing multimedia content.
Essentially, for still images, use `IMAGE` and for videos `VIDEO`.
Additional context-parameters are `skip` (how many files should be skipped), `limit` (how many files should at max be enumerated over)
and `depth` (the depth to traverse the file system, `1` stands for current folder only, `2` for sub-folders, `3` for sub-sub-folders, ...).
Let's assume we do have in the root project a folder `sandbox`, with two sub-folders `imgs` and `vids`:

```
/sandbox
  |
  - /imgs
    |
    - img1.png
    |
    - img2.png
  |
  - /vids
    |
    - vid1.mp4
```

For an image only ingestion, we could set-up the configuration as follows (`skip` and `limit` have sensible default values of `0` and `Integer.MAX_VALUE`, respectively):

**Context**:
```json
{
  "contentFactory": "InMemoryContentFactory",
  "resolverName": "disk",
  "local": {
    "enumerator": {
      "path": "./your/media/path",
      "depth": "1"
    }
  }
}
```
**Enumerator**:
```json
{
  "type": "ENUMERATOR",
  "factory": "FileSystemEnumerator",
  "mediaTypes": [
    "IMAGE", "VIDEO"
  ]
}
```

##### Index Operator Configuration: Decoder

The [`DecoderConfig`](/vitrivr-engine-core/src/main/kotlin/org/vitrivr/engine/core/config/ingest/operator/OperatorConfig.kt)
describes how the media content is decoded.

```json
{
  "type": "",
  "factory": "DecoderClass"
}
```

Available decodes can be found [in the index module](/vitrivr-engine-index/src/main/kotlin/org/vitrivr/engine/index/decode).
Since we work with images in this tutorial, we require the [`ImageDecoder`](/vitrivr-engine-index/src/main/kotlin/org/vitrivr/engine/index/decode/ImageDecoder.kt):

```json
{
  "name": "ImageDecoder"
}
```

##### Index Operators Configuration: Segmenter

A [`Segmenter`](vitrivr-engine-core/src/main/kotlin/org/vitrivr/engine/core/operators/ingest/Segmenter.kt) is a 1:n operator,
its [`OperatorConfig`](vitrivr-engine-core/src/main/kotlin/org/vitrivr/engine/core/config/ingest/operator/OperatorConfig.kt) looks as follows:

```json
{
  "type": "SEGMENTER",
  "factory": "FactoryClass"
}
```

The `type` property is mandatory, equally so the `factory`, which has to point to a [`SegmenterFactory`](vitrivr-engine-core/src/main/kotlin/org/vitrivr/engine/core/operators/ingest/SegmenterFactory.kt) implementation.
The context-parameters are optional and implementation dependent.

See [implementations](vitrivr-engine-index/src/main/kotlin/org/vitrivr/engine/index/segment/) 

##### Index Operators Configuration: Transformer

A [`Transformer`](vitrivr-engine-core/src/main/kotlin/org/vitrivr/engine/core/operators/ingest/Transformer.kt) is a 1:1 operator,
its [`OperatorConfig`](vitrivr-engine-core/src/main/kotlin/org/vitrivr/engine/core/config/ingest/operator/OperatorConfig.kt) looks as follows:

```json
{
  "type": "TRANSFORMER",
  "factory": "FactoryClass"
}
```

The `type` property is mandatory, equally so the `factory`, which has to point to a [`TransformerFactory`](vitrivr-engine-core/src/main/kotlin/org/vitrivr/engine/core/operators/ingest/TransformerFactory.kt) implementation.
The context-parameters are optional and implementation dependent.

See [implementations](vitrivr-engine-index/src/main/kotlin/org/vitrivr/engine/index/transform)

##### Index Operators Configuration: Exporter

A [`Exporter`](vitrivr-engine-core/src/main/kotlin/org/vitrivr/engine/core/operators/ingest/Exporter.kt) is a 1:1 operator,
its [`OperatorConfig`](vitrivr-engine-core/src/main/kotlin/org/vitrivr/engine/core/config/ingest/operator/OperatorConfig.kt) looks as follows:

```json
{
  "type": "EXPORTER",
  "exporterName": "name-from-schema"
}
```

The `type` property is mandatory, equally so the `exporterName`, which has to point to an `Exporter` defined on the _schema_.
The context-parameters are optional and implementation dependent and override those present in the _schema_ configuration.

See [implementations](vitrivr-engine-index/src/main/kotlin/org/vitrivr/engine/index/exporters)

##### Index Operators Configuration: Extractor

A [`Extractor`](vitrivr-engine-core/src/main/kotlin/org/vitrivr/engine/core/operators/ingest/Extractor.kt) is a 1:1 operator,
its [`OperatorConfig`](vitrivr-engine-core/src/main/kotlin/org/vitrivr/engine/core/config/ingest/operator/OperatorConfig.kt) looks as follows:

```json
{
  "type": "EXTRACTOR",
  "fieldName": "name-from-schema"
}
```

The `type` property is mandatory, equally so the `fieldName`, which has to point to a _field_ as defined on the _schema_.


See [implementations](vitrivr-engine-module-features/src/main/kotlin/org/vitrivr/engine/base/features/)

##### Index Operators Configuration: Aggregator

A [`Aggregator`](vitrivr-engine-core/src/main/kotlin/org/vitrivr/engine/core/operators/ingest/Aggregator.kt) is a 1:n operator,
its [`OperatorConfig`](vitrivr-engine-core/src/main/kotlin/org/vitrivr/engine/core/config/ingest/operator/OperatorConfig.kt) looks as follows:

```json
{
  "type": "AGGREGATOR",
  "factory": "FactoryClass"
}
```

The `type` property is mandatory, equally so the `factory`, which has to point to a [`AggregatorFactory`](vitrivr-engine-core/src/main/kotlin/org/vitrivr/engine/core/operators/ingest/AggregatorFactory.kt) implementation.
The context-parameters are optional and implementation dependent.

See [implementations](vitrivr-engine-index/src/main/kotlin/org/vitrivr/engine/index/aggregators)

#### Index Operations Configuration: The Pipeline

So far, we only have _declared_ the operators, with the `operations` property, we define the ingestion pipeline as a tree in the form of
[`OperationsConfig`](vitrivr-engine-core/src/main/kotlin/org/vitrivr/engine/core/config/ingest/operation/OperationsConfig.kt):

```json
{
  "schema": "sandbox",
  "context": {
    "contentFactory": "InMemoryContentFactory",
    "resolverName": "disk",
    "local": {
      "enumerator": {
        "path": "./sandbox/imgs",
        "depth": "1"
      },
      "myoperator": {
        "key1": "a-avlue",
        "key2": "1234"
      }
    }
  },
  "operators": {
    "myoperator": {},
    "myoperator1": {},
    "myoperator2": {}
  },
  "operations": {
    "myOperation": {
      "operator": "myoperator"
    },
    "myOperation1": {
      "operator": "myoperator1", "inputs": ["myOperation"]
    },
    "myOperation2": {
      "operator": "myoperator2", "inputs": ["myOperation"]
    }
  }
}
```

Specifically, the `operator` property must point to a previously declared _operator_ and
the entries in the `inputs` property must point to an _operation_ with that name.

Currently, there are the following rules to build such a pipeline:

**Pipeline Rules:**

1. The first _operation_ **must** be a `ENUMERATOR`
2. Following an `ENUMERATOR`, there **must** come a `DECODER`
3. Following a `DECODER`, there **must** either be a `TRANSFORMER` or `SEGMENTER`
4. `TRANSFORMER`s and `SEGMENTER`s can be daisy-chained 
5. A `SEGMENTER` must be followed by one or more `AGGREGATOR`s, multiple `AGGREGATORS` results in branching.
6. An `AGGREGATOR` must be followed by either a `EXTRACTOR` or `EXPORTER`
7. `EXPORTER`s and `EXTRACTOR`s can be daisy-chained
8. The end or the ends, in case of branching, must be of type `EXPORTER` or `EXTRACTOR`.

Notably, currently multiple `ENUMERATORS` are treated as separate trees, since merging is not yet supported.

One example, based on the _schema_ further above (without branching), might look as follows:

```json
{
  "schema": "sandbox",
  "context": {
    "contentFactory": "InMemoryContentFactory",
    "resolverName": "disk",
    "local": {
      "enumerator": {
        "path": "./sandbox/imgs",
        "depth": "1"
      },
      "thumbs": {
        "maxSideResolution": "350",
        "mimeType": "JPG"
      }
    }
  },
  "operators": {
    "fsenumerator": {
      "type": "ENUMERATOR",
      "factory": "FileSystemEnumerator",
      "mediaTypes": ["IMAGE"]
    },
    "decoder": {
      "type": "DECODER",
      "factory": "ImageDecoder"
    },
    "pass": {
      "type": "SEGMENTER",
      "factory": "PassThroughSegmenter"
    },
    "allContent": {
      "type": "AGGREGATOR",
      "factory": "AllContentAggregator"
    },
    "avgColor": {
      "type": "EXTRACTOR",
      "fieldName": "averagecolor"
    },
    "thumbs": {
      "type": "EXPORTER",
      "exporterName": "thumbnail"
    },
    "fileMeta": {
      "type": "EXTRACTOR",
      "fieldName": "file"
    }
  },
  "operations": {
    "stage2": {"operator": "pass", "inputs": ["stage1"]},
    "stage0": {"operator": "fsenumerator"},
    "stage1": {"operator": "decoder", "inputs": ["stage0"]},
    "stage3": {"operator": "allContent", "inputs": ["stage2"]},
    "stage4": {"operator": "avgColor", "inputs": ["stage3"]},
    "stage5": {"operator": "thumbs", "inputs": ["stage4"]},
    "stage6": {"operator": "fileMeta", "inputs": ["stage5"]}
  }
}
```

Here, the linear pipeline is: `fsenumerator` -> `decoder` -> `pass` -> `allContent` -> `avgColor` -> `thumbs` -> `fileMeta`.
Note that there are context-parameters defined for the `thumbs` exporter.

#### Complete Sandbox Configuration

After following above's guide on how to build your _schema_ config and your _index pipeline_ config,
the files should be similar as follows.

The **schema** config:

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

The **pipeline** config:

```json
{
  "schema": "sandbox",
  "context": {
    "contentFactory": "InMemoryContentFactory",
    "resolverName": "disk",
    "local": {
      "fsenumerator": {
        "path": "./sandbox/imgs",
        "depth": "1"
      },
      "thumbs": {
        "path": "./sandbox/thumbnails",
        "maxSideResolution": "350",
        "mimeType": "JPG"
      }
    }
  },
  "operators": {
    "fsenumerator": {
      "type": "ENUMERATOR",
      "factory": "FileSystemEnumerator",
      "mediaTypes": ["IMAGE","VIDEO"]
    },
    "decoder": {
      "type": "DECODER",
      "factory": "ImageDecoder"
    },
    "pass": {
      "type": "SEGMENTER",
      "factory": "PassThroughSegmenter"
    },
    "allContent": {
      "type": "AGGREGATOR",
      "factory": "AllContentAggregator"
    },
    "avgColor": {
      "type": "EXTRACTOR",
      "fieldName": "averagecolor"
    },
    "thumbs": {
      "type": "EXPORTER",
      "exporterName": "thumbnail"
    },
    "fileMeta": {
      "type": "EXTRACTOR",
      "fieldName": "file"
    }
  },
  "operations": {
    "stage2": {"operator": "pass", "inputs": ["stage1"]},
    "stage0": {"operator": "fsenumerator"},
    "stage1": {"operator": "decoder", "inputs": ["stage0"]},
    "stage3": {"operator": "allContent", "inputs": ["stage2"]},
    "stage4": {"operator": "avgColor", "inputs": ["stage3"]},
    "stage5": {"operator": "thumbs", "inputs": ["stage4"]},
    "stage6": {"operator": "fileMeta", "inputs": ["stage5"]}
  }
}
```

---

#### Starting the indexing pipeline

To start the actual pipeline, we start [the server module](/vitrivr-engine-server)'s [`Main`](vitrivr-engine-server/src/main/kotlin/org/vitrivr/engine/server/Main.kt)
with the path to the schema configuration as argument.

For this to work you either build the stack or you use an IDE.

1. Then, when the server is running, we have to first initialise the database (since our schema is named `sandbox`):

```
sandbox init
```

2. The extraction is started via the CLI by calling:
```
sandbox extract -c sandbox-pipeline.json
```

Which should result in logging messages that confirm the usage of our ThumbnailExporter (including its parameters) and the message:
```
Started extraction job with UUID <uuid>
```

3. The server (by default) provides an [OpenAPI swagger ui](http://localhost:7070/swagger-ui) with which the job status can be queried.
The same can be achieved by this cURL command, where `<uuid>` is the UUID printed above (and again, we have named our schema `sandbox`, hence the sandbox path:

```bash
curl -X 'GET' \
  'http://localhost:7070/api/sandbox/index/<uuid>' \
  -H 'accept: application/json'
```

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


