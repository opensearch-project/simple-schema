# Schema Compilation Workflow

To use a GraphQL Schema with data, there are two main steps:

- Adding Entities: Creating `schemaEntityType`s that will make up the Schema
- Compilation: Generating a `schemaDomainType`

## Adding Entities

Entities can be created, updated, modified, and deleted through the `/_plugins/_simpleschema/object` endpoint.
Creation is done by `POST`ing to this endpoint with a Json object.
The object must have a `type` of `schemaEntityType`.

```json
{
  "schemaEntityType": {
    "name": "Author",
    "objectId": "testAuthorObject",
    "type": "schemaEntityType",
    "catalog": ["library"],
    "content": "GraphQL defining the Author type"
  }
}
```

When the object is created, if one is not provided, it will be assigned an `objectId` primary key.

## Compilation

Compiled GraphQL Schemas are stored as `Domain` resources,
available at the `/_plugins/_simpleschema/domain` endpoint.
As `Domain`s must be uniquely named, they are given an `objectId` instead of a `name`.

Provide the entity list as a list of `objectId`s to be used in compilation.

```json
{
  "objectId": "testSchema",
  "catalog": ["library"],
  "entityList": ["testAuthorObject", "testBookObject"]
}
```
