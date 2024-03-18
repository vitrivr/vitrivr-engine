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

_NOTE: THIS SECTION REQUIRES REVIEW_

An [`ContextConfig](/vitrivr-engine-core/src/main/kotlin/org/vitrivr/engine/core/config/ContextConfig.kt)
is used to specify the _context_, additional information to the media data.


```json
{
    "contentFactory": "InMemoryContentFactory",
    "resolverFactory": "DiskResolver",
    "parameters": {
      "location": "path/to/thumbnails"
    }
}
```

The example index pipeline configuration then, with the path adjusted to the one we used in our configuration,
looks as follows:

```json
{
  "schema": "sandbox",
  "context": {
    "contentFactory": "InMemoryContentFactory",
    "resolverFactory": "DiskResolver",
    "parameters": {
      "location": "./thumbnails/sandbox"
    }
  },
  "enumerator": {

  }
}
```

#### Index Enumerator Configuration

The _enumerator_ enumerates the content to index and provides it to the indexing pipeline.
It is described with a [`EnumeratorConfig](/vitrivr-engine-core/src/main/kotlin/org/vitrivr/engine/core/config/operators/EnumeratorConfig.kt).

```json
{
  "name": "EnumeratorClass",
  "api": true,
  "parameters": {
    "key": "value"
  },
  "next": {}
}
```

Currently implemented enumerators are found [in the index module](/vitrivr-engine-index/src/main/kotlin/org/vitrivr/engine/index/enumerate),
of which we will use the [`FileSystemEnumerator`](/vitrivr-engine-index/src/main/kotlin/org/vitrivr/engine/index/enumerate/FileSystemEnumerator.kt).
The configuration **requires** the parameter `path`, the path to the folder containing multimedia content
and the parameter `mediaTypes`, which is a semicolon (`;`) separated list of [`MediaType`](/vitrivr-engine-core/src/main/kotlin/org/vitrivr/engine/core/source/MediaType.kt)s.
Essentially, for still images, use `IMAGE` and for videos `VIDEO`.
Additional parameters are `skip` (how many files should be skipped), `limit` (how many files should at max be enumerated over)
and `depth` (the depth to traverse the file system, `0` stands for current folder only, `1` for sub-folders, `2` for sub-sub-folders, ...).
Let's assume we do have in the root project a folder `sandbox-media`, with two sub-folders `imgs` and `vids`:

```
/sandbox-media
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

```json
{
    "name": "FileSystemEnumerator",
    "api": true,
    "parameters": {
      "path": "./sandbox-media/imgs",
      "mediaTypes": "IMAGE",
      "depth": 0
    },
    "next": {}
}
```

This results in the following index pipeline config:

```json
{
  "schema": "sandbox",
  "context": {
    "contentFactory": "InMemoryContentFactory",
    "resolverFactory": "DiskResolver",
    "parameters": {
      "location": "./thumbnails/sandbox"
    }
  },
  "enumerator": {
    "name": "FileSystemEnumerator",
    "api": true,
    "parameters": {
      "path": "./sandbox-media/imgs",
      "mediaTypes": "IMAGE",
      "depth": 0
    },
    "next": {}
  }
}
```

The `next` property is a _decoder_ configuration, which will be addressed in the next section.

#### Index Next / Decoder Configuration

The [`DecoderConfig`](/vitrivr-engine-core/src/main/kotlin/org/vitrivr/engine/core/config/operators/DecoderConfig.kt)
describes how the media content is decoded.

```json
{
  "name": "DecoderClass",
  "parameters": {
    "key": "value"
  },
  "nextTransformer": {},
  "nextSegmenter": {}
}
```
Note that either a _transformer_ **or** a _segmenter_ can be configured as next.

Available decodes can be found [in the index module](/vitrivr-engine-index/src/main/kotlin/org/vitrivr/engine/index/decode).
Since we work with images in this tutorial, we require the [`ImageDecoder`](/vitrivr-engine-index/src/main/kotlin/org/vitrivr/engine/index/decode/ImageDecoder.kt):

```json
{
  "name": "ImageDecoder",
  "nextSegmenter": {}
}
```

Resulting in the following index pipeline configuration:

```json
{
  "schema": "sandbox",
  "context": {
    "contentFactory": "InMemoryContentFactory",
    "resolverFactory": "DiskResolver",
    "parameters": {
      "location": "./thumbnails/sandbox"
    }
  },
  "enumerator": {
    "name": "FileSystemEnumerator",
    "api": true,
    "parameters": {
      "path": "./sandbox-media/imgs",
      "mediaTypes": "IMAGE",
      "depth": 0
    },
    "next": {
      "name": "ImageDecoder",
      "nextSegmenter": {}
    }
  }
}
```

#### Index Transformer and Segmenter Configuration

_Transformers_ can be chained, if so desired, whereas a _segmenter_ is required in order to proceed with the
index pipeline.
The [`TransformerConfig`](/vitrivr-engine-core/src/main/kotlin/org/vitrivr/engine/core/config/operators/TransformerConfig.kt) is similarly
to the previously shown [`DecoderConfig`](/vitrivr-engine-core/src/main/kotlin/org/vitrivr/engine/core/config/operators/DecoderConfig.kt).
The [`SegmenterConfig`](/vitrivr-engine-core/src/main/kotlin/org/vitrivr/engine/core/config/operators/SegmenterConfig.kt)
is as follows:

```json
{
  "name": "SegmenterClass",
  "parameters": {
    "key": "value"
  },
  "aggregators": []
}
```

Both, _transformers_ and _segmenters_ can be found in the index module [transformer package](/vitrivr-engine-index/src/main/kotlin/org/vitrivr/engine/index/transform)
and [segmenter package](/vitrivr-engine-index/src/main/kotlin/org/vitrivr/engine/index/segment).
There exists a `PassThroughSegmenter` and `PassThroughTransformer` which do nothing but pass through.
We will directly use the `PassThroughSegmenter` to further build our index pipeline config:

```json
{
  "schema": "sandbox",
  "context": {
    "contentFactory": "InMemoryContentFactory",
    "resolverFactory": "DiskResolver",
    "parameters": {
      "location": "./thumbnails/sandbox"
    }
  },
  "enumerator": {
    "name": "FileSystemEnumerator",
    "api": true,
    "parameters": {
      "path": "./sandbox-media/imgs",
      "mediaTypes": "IMAGE",
      "depth": 0
    },
    "next": {
      "name": "ImageDecoder",
      "nextSegmenter": {
        "name": "PassThroughSegmenter",
        "aggregators": []
      }
    }
  }
}
```

#### Index Aggregator Config

The [`AggregatorConfig`](/vitrivr-engine-core/src/main/kotlin/org/vitrivr/engine/core/config/operators/AggregatorConfig.kt)
describes the configuration of one single aggregator:

```json
{
  "name": "AggregatorClass",
  "parameters": {
    "key": "value"
  },
  "nextExtractor": {},
  "nextExporter": {}
}
```

An aggregator can only be followed by **either** an exporter **or** an extractor

The available aggregators can be seen in the [aggregator package](/vitrivr-engine-index/src/main/kotlin/org/vitrivr/engine/index/aggregators).

For the sake of this tutorial, we will use the simple
[`AllContentAggregator`](/vitrivr-engine-index/src/main/kotlin/org/vitrivr/engine/index/aggregators/AllContentAggregator.kt),
as we do not want to have the images aggregator in any way.

```json
{
  "name": "AllContentAggregator",
  "nextExtractor": {}
}
```

Extractors and exporters can be chained as required and are interchangeable.

#### Index Extractor Configuration

The [`ExtractorConfig`](/vitrivr-engine-core/src/main/kotlin/org/vitrivr/engine/core/config/operators/ExtractorConfig.kt)
describes an extractor.
An _extractor_ extracts descriptors from the content, thus, requires a field.

```json
{
  "fieldName": "uniqueFieldNameFromConfig",
  "factoryName": "FactoryClassName",
  "parameters": {},
  "nextExtractor": {},
  "nextExporter": {}
}
```



#### Index Exporter Configuration



#### Complete Sandbox Configuration

After following above's guide on how to build your _schema_ config and your _index pipeline_ config,
the files should be similar as follows.

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


