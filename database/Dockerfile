FROM postgres:latest
RUN apt-get update
COPY db_script.sql docker-entrypoint-initdb.d/script.sql
ENV POSTGRES_PASSWORD=postgres
ENV POSTGRES_DB=distributed
RUN mkdir /usr/local/pgsql
RUN mkdir /usr/local/pgsql/data
RUN chown postgres:postgres  /usr/local/pgsql/data

EXPOSE 5432