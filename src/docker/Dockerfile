FROM alpine AS builder

ADD ingrid-interface-*-installer.jar /
RUN unzip -q ingrid-interface-*-installer.jar -d /tmp \
    && export INSTALL_DIR=`ls -d1 /tmp/ingrid-interface-*` \
    && sed -i -r 's/(<AppenderRef.*\/>)/\1<AppenderRef ref="Console" \/>/g' $INSTALL_DIR/conf/log4j2.xml


FROM docker-registry.wemove.com/ingrid-java:jre-17
ENV INGRID_USER=ingrid
ENV RUN_DIRECTLY=true

# user, group ingrid (1000:1000) is created in base image
COPY --chown=1000:1000 --from=builder /tmp/ingrid-interface-search-* /opt/ingrid/ingrid-interface-search

USER ingrid

# forward request and error logs to docker log collector
run mkdir /opt/ingrid/ingrid-interface-search/logs
RUN ln -sf /dev/stdout /opt/ingrid/ingrid-interface-search/logs/log.log

WORKDIR /opt/ingrid/ingrid-interface-search
EXPOSE 8080/tcp

CMD /bin/sh start.sh start
