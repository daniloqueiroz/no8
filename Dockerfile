FROM java:openjdk-8
COPY ./build/install/no8/ /opt/no8
RUN chown -R daemon. /opt/no8
WORKDIR /opt/no8
USER daemon
ENTRYPOINT exec '/opt/no8/bin/no8'

