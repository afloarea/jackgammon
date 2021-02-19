FROM gradle:jdk11 AS builder

COPY --chown=gradle:gradle . /home/gradle/app
WORKDIR /home/gradle/app
RUN gradle test distTar

FROM openjdk:11.0.10-jre
RUN mkdir app/
COPY --from=builder /home/gradle/app/build/distributions/app*.tar /app

WORKDIR /app
RUN tar -xvf app*.tar --strip-components=1 && rm app*.tar

ENTRYPOINT ["bin/app"]
