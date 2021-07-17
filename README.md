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
 -outSvg /out/out.svg
 -outPdf /out/out.pdf
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

*-outSvg*
The path where the generated diagram as SVG should be stored.

*-outPdf*
The path where the generated diagram as PDF should be stored.

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
A java class/interface/enum.

Properties:
- *name*: The simple name like `HashMap`.
- *qualifiedName*: The fully qualified name like `java.util.HashMap`.

#### Constructor
A constructor of a class.

Properties:
- *name*: The simple name like `<init>(String)`.
- *qualifiedName*: The fully qualified name like `pkg.HashMap.<init>(java.lang.String)`.

#### Method
A method of a class.

Properties:
- *name*: The simple name like `do(String)`.
- *qualifiedName*: The fully qualified name like `pkg.HashMap.do(java.lang.String)`.

#### Field
A field of a class.

Properties:
- *name*: The simple name like `isRed`.
- *qualifiedName*: The fully qualified name like `pkg.HashMap.isRed`.

### Relationships
#### ACCESS
Describes that a method accesses a field.

*accessingExecutable*: The method/constructor that contains the code that performs field access.
*field*: The field that gets accessed.

#### CREATE_INSTANCE
Describes that any code within a class creates an instance of another class.

*invokingExecutable*: The method/constructor that contains the code that performs create instance operation.
*invokedConstructor*: The constructor of the object that gets instantiated.

#### HAS_CONSTRUCTOR
Describes that a given class has a constructor

*declaringType*: The type that is created by this constructor. 
*constructor*: The constructor.

#### HAS_FIELD
Describes that a type has a field.

*declaringType*: The type that has the field.
*field*: The field itself.

#### HAS_METHOD
Describes that a given class has a method

*declaringType*: The type that has the method defined.
*method*: The method.

#### INHERITS
Describes that a type inherits another type (class or interface).

*subType*: The sub type.
*superType*: The type that is inherited.

#### INVOKE
Describes that any method/constructor is invokes within any other method.

*invokingExecutable*: The type that contains the code that performs the invocation.
*invokedExecutable*: The type that gets invoked.

#### OF_TYPE
Describes that a field is of a given type.

*field*: The field for which the type is specified.
*fieldType*: The type of the field.


## Not supported features
- Local class definitions (they are ignored)
- Generic type declarations in methods are not part of the fully qualified method name (they are used in the parameter list but not defined)
- Enum constant declarations (they are ignored)
- Annotation declarations (they are ignored)
- Static initializers (they are ignored)

## Future work
* Consider an alternative java parser: http://spoon.gforge.inria.fr/

