docker run `
 --mount type=bind,source="c:\workspace\javaquery\,target=/out" `
 --link neo4j `
 piri/javaquery:latest `
 -query "MATCH (type:Type)-[r*0..1]->(otherType:Type) RETURN type, r, otherType" `
 -databaseUri bolt://neo4j:7687 `
 -out /out/out.svg
