FROM alpine AS builder

ADD ingrid-interface-*-installer.jar /
RUN unzip ingrid-interface-*-installer.jar -d /tmp


FROM docker-registry.wemove.com/ingrid-java:jre-8-slim
ENV INGRID_USER=ingrid
ENV RUN_DIRECTLY=true

# user, group ingrid (1000:1000) is created in base image
COPY --chown=1000:1000 --from=builder /tmp/ingrid-interface-search-* /opt/ingrid/ingrid-interface-search

USER ingrid

# forward request and error logs to docker log collector
RUN ln -sf /dev/stdout /opt/ingrid/ingrid-interface-search/logs/log.log

WORKDIR /opt/ingrid/ingrid-interface-search
EXPOSE 8080/tcp

CMD /bin/sh start.sh start
