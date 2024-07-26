<a id="btop"></a>
<!-- README setup inspired by https://github.com/othneildrew/Best-README-Template -->

<!-- === PROJECT LOGO === -->
<br />
<div align="center"><!-- github does not allow css, hence we use html -->
  <a href="">
    <img src="images/vitrivr_512.png" width="512" alt="vitrivr log"/><br /><br /><br />
    <img src="images/vengine-256.png" alt="vitrivr-engine logo"/>
  </a>
 <br/>
 <br/>

[![GitHub Release](https://img.shields.io/github/release/vitrivr/vitrivr-engine?include_prereleases=&sort=semver&color=blue&style=for-the-badge&label=Release)](https://github.com/vitrivr/vitrivr-engine/releases)
[![License](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)](#license)
[![issues - vitrivr-engine](https://img.shields.io/github/issues/vitrivr/vitrivr-engine?style=for-the-badge)](https://github.com/vitrivr/vitrivr-engine/issues)

[vitrivr](https://vitrivr.org)'s next-generation retrieval engine.

[Read The Docs](https://github.com/vitrivr/vitrivr-engine/wiki)

</div>

## vitrivr-engine

vitrivr-engine is [vitrivr](https://vitrivr.org)'s next generation retrieval engine with a flexible, modular architecture.
Based on the experiences with its predecessor, [Cineast](https://github.com/vitrivr/cineast),
vitrivr engine's data model, ingestion pipeline and retrieval logic have been reworked from the ground.
Essentially, vitrivr-engine enables the analysis (i.e. ingestion) and querying (i.e. retrieval ) of
multimedia data.

## Built With

* [Kotlin](https://kotlinlang.org) for the JVM, e.g. [OpenJDK](https://openjdk.org/)
* [OpenApi](https://www.openapis.org/)
* [CottontailDB](https://github.com/vitrivr/CottontailDB)
* ... and more ...

## Getting Started

See [Getting Started](https://github.com/vitrivr/vitrivr-engine/wiki/Getting-Started)

## Usage

See [Example](https://github.com/vitrivr/vitrivr-engine/wiki/Example)

## Project Structure

The project is set up as a multi-module Kotlin project:

| Module                                                                                                                | Description                                                                                                                                                                                                                              | Maven Dependency |
|-----------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|------------------|
| [`vitrivr-engine-core`](https://github.com/vitrivr/vitrivr-engine//vitrivr-engine-core)                               | The core library of the project, which provides basic interfaces & classes.                                                                                                                                                              | Yes              |
| [`vtirivr-engine-query`](https://github.com/vitrivr/vitrivr-engine//vitrivr-engine-query)                             | Query / retrieval related extension to the core library with various retrievers and data manipulation operators.                                                                                                                         | Yes              |
| [`vitrivr-engine-index`](https://github.com/vitrivr/vitrivr-engine//vitrivr-engine-index)                             | Indexing / ingestion related extension to the core library with various decoders and segmenters.                                                                                                                                         | Yes              |
| [`vitrivr-engine-module-cottontaildb`](https://github.com/vitrivr/vitrivr-engine//vitrivr-engine-plugin-cottontaildb) | The database driver for the [CottontailDB](https://github.com/vitrivr/cottontaildb) database, used for NNNS and other queries.                                                                                                           | Yes              |
| [`vitrivr-engine-module-features`](https://github.com/vitrivr/vitrivr-engine//vitrivr-engine-plugin-features)         | Extension that contains specific indexing and retrieval implementations such as fulltext, colour, etc.                                                                                                                                   | Yes              |
| [`vitrivr-engine-module-m3d`](https://github.com/vitrivr/vitrivr-engine//vitrivr-engine-module-m3d)                   | Extension related to 3d model indexing and retrieval. Contains various feature modules and capability to process meshes.                                                                                                                 | Yes              |
| [`vitrivr-engine-module-fes`](https://github.com/vitrivr/vitrivr-engine//vitrivr-engine-module-fes)                   | Extension that can be used to harnes feature extraction provided by an external ML model server. **Requires local generation of bindings:** `./gradlew :vitrivr-engine-module-fes:generateFESClient`                                     | Yes              |
| [`vitrivr-engine-server`](https://github.com/vitrivr/vitrivr-engine//vitrivr-engine-server)                           | A [Javalin](https://javalin.io) powered server providing an [OpenApi](https://openapis.org) [documented REST API](vitrivr-engine-server/doc/oas.json) for both, ingestion and querying and a CLI, essentially the runtime of the project | No               |

## Contributing

We welcome contributors. Please fork the repo and open a pull-request with your work.
A good starting point are the 'good first issue' issues.

## Contributors

* @ppanopticon
* @lucaro
* @sauterl
* @net-csscience-raphel
* @rahelarnold98
* @faberf

## Citation

See [Citation](https://github.com/vitrivr/vitrivr-engine/wiki/Home#Citation)
