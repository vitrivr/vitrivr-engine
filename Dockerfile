FROM gradle:jdk17 AS build

COPY --chown=gradle:gradle . /vitrivr-engine-src
WORKDIR /vitrivr-engine-src
RUN gradle --no-daemon distTar
WORKDIR /vitrivr-engine-src/vitrivr-engine-server/build/distributions/
RUN tar xf ./vitrivr-engine-server-0.0.1-SNAPSHOT.tar

FROM eclipse-temurin:17-jre
RUN apt-get update && apt-get install -y screen

COPY --from=build /vitrivr-engine-src/vitrivr-engine-server/build/distributions/vitrivr-engine-server-0.0.1-SNAPSHOT /vitrivr-engine-server

RUN mkdir /vitrivr-engine-config
WORKDIR /vitrivr-engine-server/bin
COPY entrypoint.sh .
RUN chmod +x entrypoint.sh

EXPOSE 7070

ENTRYPOINT ./entrypoint.sh

