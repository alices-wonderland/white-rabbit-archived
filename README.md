# White Rabbit

[![codecov](https://codecov.io/gh/alices-wonderland/white-rabbit/branch/main/graph/badge.svg?token=ITJJebAjWw)](https://codecov.io/gh/alices-wonderland/white-rabbit)
[![CI](https://github.com/alices-wonderland/white-rabbit/actions/workflows/ci.yaml/badge.svg)](https://github.com/alices-wonderland/white-rabbit/actions/workflows/ci.yaml)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=alices-wonderland_white-rabbit&metric=bugs)](https://sonarcloud.io/summary/new_code?id=alices-wonderland_white-rabbit)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=alices-wonderland_white-rabbit&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=alices-wonderland_white-rabbit)

[![SonarCloud](https://sonarcloud.io/images/project_badges/sonarcloud-white.svg)](https://sonarcloud.io/summary/new_code?id=alices-wonderland_white-rabbit)

## Start Up

### Generate HTTPS Certificate

* `keytool -genkeypair -alias wonderland -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore wonderland.p12 -validity 3650`
  * Password could be `password`
* Put `wonderland.p12` into `resources/keystore`
* Add `application-dev.yaml`:
  ```yaml
  spring:
    datasource:
      url: jdbc:postgresql://<real-ip>:5432/white-rabbit?createDatabaseIfNotExist=true
    jpa:
      hibernate:
        ddl-auto: create
      show-sql: true
  server:
    ssl:
      key-store: classpath:keystore/white-rabbit.p12
      key-store-password: <password>
      key-alias: white-rabbit
  ```

### When Building Image

For proxy, set `export BUILD_IMAGE_PROXY=<proxy>` or `$env:BUILD_IMAGE_PROXY="<proxy>"` before building the native image.
The proxy IP should be **the host IP in Docker image**, since the building process are running in the Docker container.

Then run:
```
./gradlew clean test :white-rabbit-endpoint-graphql:bootBuildImage
```

### Check Code Style

```
./gradlew clean spotlessApply :gradleConfiguration:ktlintFormat check :gradleConfiguration:check
```

### Run the Docker Image

```
docker run -it --rm -p 8443:8443 -v <external-keystore>:/keystore/  -v <config-with-secrets>:/external-config/application-prod.yaml -e spring_profiles_active=prod -e "SERVER_SSL_KEY-STORE=/keystore/white-rabbit.p12" -e "SPRING_CONFIG_IMPORT=/external-config/application-prod.yaml"  ukonnra/white-rabbit-endpoint-graphql:latest
```
