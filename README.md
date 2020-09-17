# JavaQuery

## Description
Parses a Java application and allows querying a simplified AST (abstract syntax tree) via Cypher queries.

## Getting started
### Setup on your local machine
1. Download and install Graphviz 2.38 from https://graphviz.org/_pages/Download/Download_windows.html
2. Run via `java -jar javaquery-full.jar`

### Run via docker
1. Start the Neo4J docker container
```
docker run --name neo4j --publish=7474:7474 --publish=7687:7687 --volume=$HOME/neo4j/data:/data --env=NEO4J_AUTH=none neo4j:latest
```
2. Execute javaquery via docker: 
```
docker run \
 --mount type=bind,source="/mnt/c/workspace/jetty.project-jetty-9.4.30.v20200611,target=/source" \
 --mount type=bind,source="/mnt/c/workspace/out,target=/out" \
 --link neo4j \
 piri/javaquery:latest \
 -analyze /source/jetty-http/src/main/java\:/source/jetty-http2/http2-common/src/main/java \
 -query "MATCH (type:Type)-[r*0..1]->(otherType:Type) RETURN type, r, otherType" \
 -stereotype Toggleable -stereotypeQuery "MATCH (type:Type)-[r*0..1]->(otherType:Type) WHERE (type.fullyQualifiedName STARTS WITH 'com.salesmanager.shop.admin.controller.') RETURN type" \
 -databaseUri bolt://neo4j:7687 \
 -out /out/out.svg
```

## Synopsis
```
java -jar javaquery-full.jar 
(-analyze <paths separated with ;>|-query <cypher query>)
[-databaseUri <database URI>]
[-stereotype <name> -stereotypeQuery <cypher query>]*
```

## Options
*-analyze <paths separated with ;>*
Analyze the specified source directories and store the simplified AST in the database. Omit this parameter if the database of the last analysis should be used.

*-query <cypher query>*
Specify the cypher query that should be executed against the simplified AST. Omit this parameter if only the source code should be parsed and stored in the database.

*-databaseUri <database URI>*
If an external database should be used to store/retrieve the simplified AST.

*-stereotype*
The name of the stereotype that should be attached to certain classes.

*-stereotypeQuery*
The query that returns all classes to which the previous specified stereotype should be attached.

*-out*
The path where the generated diagram should be stored.

## Example invocations
```
java -jar javaquery-full.jar
 -analyze C:\workspace
 -query "MATCH (type:Type)-[r*0..1]->(otherType:Type) WHERE (type.fullyQualifiedName STARTS WITH 'com.salesmanager.shop.admin.controller.') RETURN type, r, otherType"
 -stereotype Toggleable -stereotypeQuery "MATCH (type:Type)-[r*0..1]->(otherType:Type) WHERE (type.fullyQualifiedName STARTS WITH 'com.salesmanager.shop.admin.controller.') RETURN type"
```

## Datamodel
A graph data base (Neo4J) is used to store the simplified AST. It contains the following types:

### Nodes
#### Type
A java class.

Properties:
- *name*: The simple name like `HashMap`.
- *qualifiedName*: The fully qualified name like `java.util.HashMap`.

### Relationships
#### INVOKES
Describes that any code within a class invokes any method of another class.

*from*: The type that contains the code that performs the invocation.
*to*: The type that gets invoked.
*invokedMethod*: The name of the method that is invoked.

### INHERITS
Describes that a type inherits another type (class or interface).

*From*: The sub type.
*To*: The type that is inherited.

### HAS_FIELD
Describes that a type has a field of another type.

*from*: The type that has the field.
*to*: The type of the field.
*fieldName*: The name of the field that is invoked.
 
### CREATE_INSTANCE
Describes that any code within a class creates an instance of another class.

*from*: The type that contains the code that performs create instance operation.
*to*: The type of the object that gets created.

### ACCESS_FIELD
Describes that any code within a class access a field of another class.

*from*: The type that contains the code that performs field access.
*to*: The type of the object that contains the field that gets access.
*fieldName*: The name of the field that is accessed.

## Future work
* Consider an alternative java parser: http://spoon.gforge.inria.fr/

