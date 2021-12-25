FROM eclipse-temurin:17-jre-alpine

ARG USER=whiterabbit

ARG GROUP=wonderland

ARG UID=1000

ARG GID=1000

ARG HOME=/home/$USER

RUN addgroup -g $GID -S $GROUP

RUN adduser -h $HOME -g "" -G $GROUP -D $USER

USER $USER

WORKDIR $HOME

COPY --chown=$USER:$GROUP white-rabbit-endpoint-graphql.jar app.jar

ENTRYPOINT [ "java", "-jar", "app.jar" ]
