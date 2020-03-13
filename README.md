ZeeQS - Zeebe Query Service
=========================

A [Zeebe](https://zeebe.io) community extension that provides a GraphQL query API over Zeebe's data. The data is imported from the broker using an exporter (e.g. Hazelcast, Elasticsearch) and aggregated in the service.

![architecture view](docs/ZeeQS.png)

## Usage

The application provides an endpoint `/graphql` for GraphQL queries.

A query can be send via HTTP POST request and a JSON body containing the `query`. For example:

```
curl \
  -X POST \
  -H "Content-Type: application/json" \
  --data '{ "query": "{ workflows { key } }" }' \
  http://localhost:9000/graphql
```

While development, the graph can be explored using the integrated GraphiQL:
http://localhost:9000/graphiql

### Example Queries:

Get all workflows:

```graphql
{
  workflows {
    key
    bpmnProcessId
    version    
  }
}
```

Get all workflow instances that are active (i.e. not completed or terminated):

```graphql
{
  workflowInstances(stateIn: [ACTIVATED]) {
    key
    state
    workflow {
      bpmnProcessId
    }
    elementInstances(stateIn: [ACTIVATED]) {
      elementId
      elementName
      bpmnElementType
      state
    }
    variables {
      name
      value
    }
  }
}
```

Get all jobs that are activate (i.e. not completed or canceled) and have one of the given job types:

```graphql
{
  jobs(stateIn: [ACTIVATABLE, ACTIVATED], jobTypeIn: ["task-1", "task-2", "task-3"]) {
    key    
    jobType
    state    
    elementInstance {
      elementId
      elementName      
      workflowInstance {
        key        
        workflow {
          key
          bpmnProcessId
        }
      }
    }
  }
}
```

## Install

### Docker

...

### Manual

1. Download the latest [Zeebe distribution](https://github.com/zeebe-io/zeebe/releases) _(zeebe-distribution-%{VERSION}.tar.gz
)_

1. Download the latest [Hazelcast exporter JAR](https://github.com/zeebe-io/zeebe-hazelcast-exporter/releases) _(zeebe-hazelcast-exporter-%{VERSION}-jar-with-dependencies.jar)_ (>= 0.8.0-alpha1)

1. Copy the JAR into the broker folder `~/zeebe-broker-%{VERSION}/exporters`

1. Add the exporter to the broker configuration `~/zeebe-broker-%{VERSION}/conf/zeebe.cfg.toml`.
    ```
    [[exporters]]
    id = "hazelcast"
    className = "io.zeebe.hazelcast.exporter.HazelcastExporter"
    jarPath = "exporters/zeebe-hazelcast-exporter-%{VERSION}-jar-with-dependencies.jar"
    ```
   
   For version >= 0.23.0-alpha2 `~/zeebe-broker-%{VERSION}/conf/zeebe.cfg.yaml`
   
    ```yaml
   exporters:
     hazelcast:
       className: io.zeebe.hazelcast.exporter.HazelcastExporter
       jarPath: exporters/zeebe-hazelcast-exporter-%{VERSION}-jar-with-dependencies.jar
   ```   

1. Start the broker
    `~/zeebe-broker-%{VERSION}/bin/broker`
    
1. Download the latest [ZeeQS application JAR](https://github.com/zeebe-io/zeeqs/releases)    

1. Start the application
    `java -jar zeeqs-{VERSION}.jar`

### Configuration

The following configuration properties can be changes via environment variables or application.yaml file (see [Spring Boot Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-external-config)) 

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

