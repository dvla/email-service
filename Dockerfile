FROM dtr.int-ac.dvla.gov.uk/dvla-base-images/openjre:8u212-jre-1.0.0

ADD target/email-service-*fat.jar /app/application.jar
ADD src/main/resources/config-k8s.yaml /app/config.yaml

EXPOSE 9430 9431

CMD ["-XX:+UnlockExperimentalVMOptions", "-XX:+UseCGroupMemoryLimitForHeap", "-XX:MaxRAMFraction=2", "-jar", "application.jar", "server", "/app/config.yaml"]