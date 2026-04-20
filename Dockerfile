FROM amazoncorretto:17

LABEL maintainer="https://crafted-code.org"

ENV TZ=Europe/Berlin

COPY /target/dependencies/ ./
COPY /target/spring-boot-loader/ ./
COPY /target/application/ ./

HEALTHCHECK --interval=60s --timeout=30s --start-period=30s --retries=3 CMD if [ "$(curl -f http://localhost:8098/actuator/health)" != '{"status":"UP"}' ]; then exit 1; fi

ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom org.springframework.boot.loader.launch.JarLauncher" ]