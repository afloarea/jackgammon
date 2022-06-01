FROM gradle:jdk17 AS builder

COPY --chown=gradle:gradle . /home/gradle/app
WORKDIR /home/gradle/app
RUN gradle test distTar

FROM eclipse-temurin:17-jre-alpine
RUN mkdir /opt/app/
COPY --from=builder /home/gradle/app/build/distributions/app*.tar /opt/app/

WORKDIR /opt/app
RUN tar -xvf app*.tar --strip-components=1 && rm app*.tar

ENTRYPOINT ["bin/app"]
