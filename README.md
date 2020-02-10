ZeeQS - Zeebe Query Service
=========================

A [Zeebe](https://zeebe.io) community extension that provides a GraphQL query API over Zeebe's data. The data is imported from the broker using an exporter (e.g. Hazelcast, Elasticsearch) and aggregated in the service.

![architecture view](docs/ZeeQS.png)

## Usage

The application provides an endpoint `/graphql` for GraphQL queries.

A query can be send via HTTP GET request and a `query` parameter. For example, `localhost:9000/graphql?query={workflows{key,bpmnProcessId,version}}` 

While development, the graph can be explored using the integrated GraphiQL:
http://localhost:9000/graphiql

## Install

### Docker

...

### Manual

1. Start Zeebe broker with Hazelcast Exporter (>= 0.8.0-alpha1)
2. Start ZeeQS application
  `java -jar zeeqs-1.0.0-SNAPSHOT.jar`

### Configuration

```
# application database
spring.datasource.url=jdbc:h2:mem:zeeqs;DB_CLOSE_DELAY=-1
spring.datasource.user=sa
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=create

server.port=9000

# connection to Hazelcast
io.zeebe.hazelcast.connection=localhost:5701

# logging
logging.level.io.zeebe.zeeqs=DEBUG
logging.level.com.hazelcast=WARN
```

## Build from Source

Build with Maven

`mvn clean install`

## Code of Conduct

This project adheres to the Contributor Covenant [Code of
Conduct](/CODE_OF_CONDUCT.md). By participating, you are expected to uphold
this code. Please report unacceptable behavior to code-of-conduct@zeebe.io.

## License

[Apache License, Version 2.0](/LICENSE) 

