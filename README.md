# Setup before the course
**Before attending/reviewing the course**, please perform the setup steps found in **[Setup.md](https://github.com/jrtitko/CheckoutOrderTotalKata/blob/master/Setup.md)**.
We will not be taking class time to perform the setup.  If you have a problem with the setup, please come early.

# Technology
1. Java 11
1. Spock 1.2
1. Lombok
1. Cassandra
1. Mongo
1. SpringBoot 2

# TDD Refactor Kata
## Written by \<Protecting the guilty>

You have been contracted to modify an system that reads a system table from a variety of Cassandra databases that holds the names of all the fields for all of the tables and stores that information into a Mongo database for systems down the line to read.

Over the life of the system, some instances of Cassandra has been updated and as a result the system table that holds this information has had its name and the fields in the table changed.  However there are some instances of Cassandra that is still on the older version so both must be maintained.

The system had no unit tests created and still requires instances of Cassandra and Mongo to be stood up to perform any tests.  However, a couple of characterization tests have been added to get you started, but these should go away as you refactor the code.

The code has quite a few code smells and it seems prudent to clean up the code before we make any changes.

A partial list of code smells include:

1. Fragile code
1. Duplicate Code
1. Large classes
1. Tests rely on outside dependencies

At this point, you are only responsible for cleaning up the code so that modifications can be more easily added in the future.


# Setting up for Unit Tests
In order to run the current unit tests, it is necessary to bring down several docker images.  There are also some common commands to look at the data we will be using.

## Cassandra V2
    $ docker pull cassandra:2
    $ docker run --name cassandra-v2 -p 9043:9042 -d cassandra:2    ==> note port change
    $ docker exec -it cassandra-v2 cqlsh
    > describe table system.schema_columns;
    > select * from system.schema_columns;

## Cassandra V3
    $ docker pull cassandra:3
    $ docker run --name cassandra-v3 -p 9042:9042 -d cassandra:3
    $ docker exec -it cassandra-v3 cqlsh
    > describe table system_schema.columns;
    > select * from system_schema.columns;

## Mongo
    $ docker pull mongo:latest
    $ docker run --name mongo -d -p 27017:27017 mongo --storageEngine wiredTiger
    $ docker exec -it mongo mongo
    > show dbs                  => lists all of the databases
    > use myDb                  => use the specific database
    > show tables               => lists all of the tables in the current database
    > db.schemaData.find()
    > db.schemaData.remove({})  => clears the database

