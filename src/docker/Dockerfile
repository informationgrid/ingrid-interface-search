FROM openjdk:8-jre-alpine
ENV INGRID_USER=root
ENV RUN_DIRECTLY=true

ADD ingrid-interface-*-installer.jar /

RUN unzip ingrid-interface-*-installer.jar -d /tmp \
    && mkdir -p /opt/ingrid/ingrid-interface-search \
    && cp -R /tmp/ingrid-interface-search-*/* /opt/ingrid/ingrid-interface-search/ \
    && rm -Rf /tmp/* \
    && rm ingrid-*.jar

# forward request and error logs to docker log collector
RUN ln -sf /dev/stdout /opt/ingrid/ingrid-interface-search/log.log
  # && ln -sf /dev/stderr /var/log/nginx/error.log

WORKDIR /opt/ingrid/ingrid-interface-search
EXPOSE 80

CMD /bin/sh start.sh start
