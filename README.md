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

## Example invocations
```
javaquery.jar
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

*from*: The type that contains the code that performs the create instance operation.
*to*: The type of the object that gets created.
