# Docker Images Setup

## Docker Setup
If you have not previously installed Docker on your machine, you will need to do that.

Directions can be found at:<br>
Mac: https://docs.docker.com/docker-for-mac/ <br>
Windows: https://docs.docker.com/docker-for-windows/

## Cassandra V2
```
$ docker run --name cassandra-v2 -p 9043:9042 -d cassandra:2  
$ docker exec -it cassandra-v2 cqlsh
> describe keyspaces;
> describe table system.schema_columns;
> select * from system.schema_columns;
```
Note the port number change (9043) because we are using multiple Cassandra versions on a single machine

## Cassandra V3
```
$ docker run --name cassandra-v3 -p 9042:9042 -d cassandra:3  
$ docker exec -it cassandra-v3 cqlsh
> describe keyspaces;
> describe table system_schema.columns;
> select * from system_schema.columns;
```
## Mongo
```
$ docker run --name mongo -d -p 27017:27017 mongo --storageEngine wiredTiger
$ docker exec -it mongo mongo
> show dbs                  => lists all of the databases
> use myDb                  => use the specific database
> show tables               => lists all of the tables in the current database
> db.schemaData.insert({test:"myTest"})
> db.schemaData.find()
> db.schemaData.remove({})  => clears the database
```
