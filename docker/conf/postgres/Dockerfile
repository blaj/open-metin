FROM postgres:17.0-alpine

RUN mkdir -p /etc/ssl/postgres

COPY certs/postgres.crt /etc/ssl/postgres/server.crt
COPY certs/postgres.key /etc/ssl/postgres/server.key

USER root
RUN chown -R 70:70 /etc/ssl/postgres && \
    chmod 600 /etc/ssl/postgres/server.key && \
    chmod 644 /etc/ssl/postgres/server.crt

USER postgres

CMD ["postgres", \
     "-c", "ssl=on", \
     "-c", "ssl_cert_file=/etc/ssl/postgres/server.crt", \
     "-c", "ssl_key_file=/etc/ssl/postgres/server.key" \
    ]