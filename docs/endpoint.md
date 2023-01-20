# Endpoint
This document will describe how the user show generate and interact with simple-schema graphQL endpoint.

The next access URL support RESTFULL Web API [Representational state transfer](https://en.wikipedia.org/wiki/Representational_state_transfer) 

## General Entry point
### **[https://userdomain/simpleschema]()**

This url is the general base entry point for the beginning of interacting with the simple schema object store.

### **[/object]()** URL 
This endpoint offers the next functionality :

 - [POST]() :  Create one of the following entities: 
   - GraphQL template type
   - GraphQL query template type
   - GraphQL subscription template type
   - GraphQL schema type:
     - This action has the side-effect of generating a dedicated URI (Resource) named according to the schema (see next section for more details)
---
 - [DELETE]() : Delete any of the existing types:
     - GraphQL template type
     - GraphQL query template type
     - GraphQL subscription template type

---
 - [GET]() : Read by id / object-type any of the following:
     - GraphQL template type
     - GraphQL query template type
     - GraphQL subscription template type
     - GraphQL schema type
---


## Schema Entry Point
### **[https://userdomain/simpleschema/$domainSchema]()**

This url is the domain specific schema access point which is auto generated once the former create API for schema was called.

#### _Note_
   _Once a schema url is created it is immutable and its structure and type cannot be changed, for updating the schema we will provide a schema evolution utilities._

### [/ontology]() URL
This endpoint offers the next functionality :
   - GET :  get the associated ontology based on this schema  

### [/index]() URL
This endpoint offers the next functionality :

- POST :  generate a new indices according to the index mapping that is associated with this schema - **this is a single time operation and once called can't be initiated again.**


- GET:    receive the index mapping configuration file used to map the logical schema into its physical indices

### [/graphql]() URL
This endpoint offers the next functionality :

   - POST:  Query / Mutate the datastore by submitting graphQL query/mutation 
   - GET :  GQL Schema introspection request  

### [/graphql/explain]() URL
This endpoint offers the next functionality :

   - POST:  Explain the given Query/Mutation by returning the following details
     - Compiled query
     - Target physical indices
     - Additional metadata

### [/graphql/visualize]() URL
This endpoint offers the next functionality :

   - POST:  [Visualize](https://graphviz.org/) the given Query/Mutation by returning an image visually describing the query structure

