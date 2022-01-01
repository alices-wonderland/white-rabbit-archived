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
      url: jdbc:postgresql://<database-host>:5432/white-rabbit?createDatabaseIfNotExist=true
      username: <database-username>
      password: <database-password>
    security:
      oauth2:
        resourceserver:
          jwt:
            issuer-uri: <issuer-uri-from-oidc-provider>
    jpa:
      hibernate:
        ddl-auto: create
      show-sql: true
  server:
    ssl:
      key-store: /keystore/wonderland.p12
      key-store-password: <password>
      key-alias: wonderland
  ```

### Check Code Style

```
./gradlew clean spotlessApply :gradleConfiguration:ktlintFormat check :gradleConfiguration:check
```

### Run the Docker Image

```
docker run -it --rm -p 8443:8443 \
  -v <external-keystore>:/keystore/ \
  -v <config-with-secrets>:/external-config/application-dev.yaml \
  -e spring_profiles_active=dev \
  -e "SPRING_CONFIG_IMPORT=/external-config/application-dev.yaml" \
  --network=<the-database-network> \
  ukonnra/white-rabbit-endpoint-graphql:latest
```
