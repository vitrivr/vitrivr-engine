FROM gradle:8.5-jdk21 AS build

WORKDIR /src
COPY --chown=gradle:gradle . .

RUN apt-get update && apt-get install -y unzip ffmpeg && rm -rf /var/lib/apt/lists/*

RUN gradle --no-daemon :vitrivr-engine-server:distZip -x test

FROM eclipse-temurin:21-jre

WORKDIR /app

RUN apt-get update && apt-get install -y gettext unzip && rm -rf /var/lib/apt/lists/*

COPY --from=build /src/vitrivr-engine-server/build/distributions/vitrivr-engine-server-*.zip /tmp/
RUN zipfile=$(ls /tmp/vitrivr-engine-server-*.zip | head -n 1) && \
    unzip "$zipfile" -d /app/vitrivr && \
    rm "$zipfile"

COPY entrypoint.sh /app/vitrivr/entrypoint.sh
RUN chmod +x /app/vitrivr/entrypoint.sh

WORKDIR /app/vitrivr
EXPOSE 7070
ENTRYPOINT ["/app/vitrivr/entrypoint.sh"]
