# JavaQuery

## Description
Parses a Java application and allows querying a simplified AST (abstract syntax tree) via Cypher queries.

## Prerequisites
Download and install Graphviz 2.38 from https://graphviz.org/_pages/Download/Download_windows.html

## Synopsis
```
java -jar javaquery.jar 
(-analyze <paths separated with ;>|-query <cypher query>)
[-databaseUri <database URI>]
```

## Options
*-analyze <paths separated with ;>*
Analyze the specified source directories and store the simplified AST in the database. Omit this parameter if the database of the last analysis should be used.

*-query <cypher query>*
Specify the cypher query that should be executed against the simplified AST. Omit this parameter if only the source code should be parsed and stored in the database.

*-databaseUri <database URI>*
If an external database should be used to store/retrieve the simplified AST.

## Example invocations
```
javaquery.jar
 -analyze C:\workspace
 -query "MATCH (type:Type)-[r*0..1]->(otherType:Type) WHERE (type.fullyQualifiedName STARTS WITH 'com.salesmanager.shop.admin.controller.') RETURN type, r, otherType"
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

*From*: The type that contains the code that performs the invocation.
*To*: The type that gets invoked.

### INHERITS
Describes that a type inherits another type (class or interface).

*From*: The sub type.
*To*: The type that is inherited.

### HAS_FIELD
Describes that a type has a field of another type.

*From*: The type that has the field.
*To*: The type of the field.
 
 ### CREATE_INSTANCE
Describes that any code within a class creates an instance of another class.

*From*: The type that contains the code that performs the create instance operation.
*To*: The type of the object that gets created.
