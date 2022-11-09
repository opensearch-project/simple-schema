# Supporting GraphQL Schema & Language
This RFC will present the GraphQL Plugin initiative that will allow supporting GraphQL based application development including
 - Schema creation
 - Code Generation
 - Query support
 - Endpoint Generation

## How does it work?

With Open-Search as the search engine database, the GraphQL Library makes it simple for applications to have their data treated natively from the front-end all the way to storage,
avoiding duplicate schema work and ensuring flawless integration between front-end and backend developers.
By supplying the opensearch GraphQL plugin-Library with a set of type definitions describing the schema of your data, it can generate an entire executable schema with all of the additional types needed to execute queries and mutations to interact with your database.
For every query and mutation that is executed against this generated schema, the GraphQL plugin-Library generates a single opensearch query which is executed against the database.

## Features

The GraphQL plugin-Library presents a large feature set for interacting with opensearch database using GraphQL:
 - Automatic generation of Queries and Mutations for CRUD interactions
 - Various Types, including temporal and spatial types
 - Support for both entity and relationship properties
 - Extensibility through the @custom directive and/or Custom Resolvers
 - Extensive Filtering and Sorting options
 - Options for index Autogeneration and Default Values
 - Multiple Pagination options

### Schema Definitions
This section will describe the creation of the schema used for definition and query the data.

#### Directives

 - **@model** - describing a top-level entities, these entities will most likely be used to populate their respective index
 - **@component** - describing a component level entities, these entities will afterward be generated into a component template mapping (components are used for composition by other top level template-mapping containers) 
 - **@key**   - describing the uniqueness and indexing field for an entity, this directive is used also to determine the sorting of the matching index used for storing these entities 
 - **@relation** - describing the relationship between different entities in the schema. Since GraphQL's representation for entities is mostly containment - this directive also covers that in details 
 - **@timestamp** - describes that this entity has a point in time in-which it can be used for calculation of summation and additional time based statistics.  
 - **@alias** - describes the capability of mapping entity's field to an existing index field
 - **@computed** - describes that a field will be dynamically calculated using a specific PPL expression (may also be implemented by opensearch runtime Field) 

### Auto Generation of Indices
When using a GraphQL schema without prior data - we will want that the entire process of creating the indices mapping to be done automatically.
For this purpose the indexProvider abstraction layer will provide a list of mapping strategies to allow custom optimization per use-case and allow customer to utilize the best of opensearch capabilities.

see [mapping entities and relations onto opensearch indices](index-provider.md)

### Mapping for existing Indices

Using existing indices with conjunction to high level graphQL schema requires an intermediate representation layer - the index-provider descriptive language.
This language maps the logical entities/relations into the physical indices.
Once the definition of the logical GraphQL SDL is done - the index provider is generated out of these definitions, the existing indices must be mapped in the index-provider json configuration files to reflect their
current structure.

In some cases using the index-provider configuration is not enough - on such cases the @alias directive helps mapping existing field names onto the logical schematic names. 

### Auto Generation of Mutations
Several Mutations are automatically generated for each type defined in type definitions::
 - **Create** - create documents, and recursively create or connect further documents in the subsequent tree
 - **Update** - update documents, and recursively perform any operations from there
 - **Delete** - delete documents, and recursively delete or disconnect further nodes in the subsequent tree

Let`s define the next entities which will be the example of the above capabilities. 

```graphql
type Profession {
    id: ID! @key
    name: String!
    description: String
}

type Person {
    id: ID! @key
    name: String
    age: Int
    profession: [Profession!]! @relation(mappingType: "foreign")
}
```

_**A note on the relationships**_
The relationship in the following description is implicitly defined as the 'has_profession' that a person holds for each of his professions.
This relationship may have properties which will be embedded in the profession's nesting document representation (as specified by the physical index provider configuration)

As default, the relationship will be defined bi-directional and a subsequent query API will be auto generated for queries for both ways (source to dest & vis-versa). In case only one direction is required -
the bi-directional configuration specification will need to be explicitly false in the index-provider config json specification.

**Query**

   Each entity defined in type definitions will have the following query generated for it:
    - query with two input fields 
      - filter field 
      - aggregation field 
  The response will also return an entity that has two sections:
   - list of entities fulfilling the query
   - list of aggregation results applied in the query


 ```graphql
type Query {
    person(filter: PersonFilter, agg : PersonAggregationFilter): [PersonResult]
    profession(filter: ProfessionFilter, agg: ProfessionAggregationFilter): [ProfessionResult]
}
```

Let's review the auto generated filters and aggregation filter input fields:

```graphql
input PersonFilter {
  id: TextFilter
  name: TextFilter
  age: NumericFilter
  profession: ProfessionFilter
}

input ProfessionFilter {
  id: TextFilter
  name: TextFilter
  description: TextFilter
}
```
Each type of filter (numeric,textual,date,geo) has its own relevant operators (numeric,textual,...) 

In a similar way the aggregation filter input fields are generated for each type
```graphql
input PersonAggregationFilter {
  id: TextAggregationFilter
  name: TextAggregationFilter
  age: NumericAggregationFilter
  professionCount: NumericAggregationFilter
}

input ProfessionAggregationFilter {
  id: TextAggregationFilter
  name: TextAggregationFilter
  description: TextAggregationFilter
}
```

The results for the queries are composed of both the (list) of the result entities and the requested aggregation result

```graphql
type PersonAggregationFilterResult {
  id: TextAggregationFilterResult
  name: TextAggregationFilterResult
  age: NumericAggregationFilterResult
  professionCount: NumericAggregationFilterResult
}

type PersonResult {
  results:[Person]
  aggResults:[PersonAggregationResults]
}

type ProfessionAggregationFilterResult{
  id: TextAggregationFilterResult
  name: TextAggregationFilterResult
  description: TextAggregationFilterResult
}

type ProfessionResult {
  results:[Profession]
  aggResult:[ProfessionAggregationFilterResult]
}
```

This result composite entity will reflect results for complex query that may include both fields regular filter (such as numbrical and textual filters) 
And both aggregation fields filter (such as max, count, average). 

### **Entity Filter**

When querying for entities, a number of operators are available for different types in the filter argument of a Query or Mutation.

 
- **Equality operators**
  All types can be tested for either equality or non-equality.
   
- **Numerical operators**

  The following comparison operators are available for numeric types (Int, Float, Float), Temporal Types and Spatial Types:
  * equal
  * lowerThen
  * lowerThenEqual
  * graterThen
  * graterThenEqual

- **Textual filter operators**

   The following case-sensitive comparison operators are only available for use on String and ID types:
    * startingWith
    * notStartingWith
    * endingWith
    * notEndingWith
    * containing
    * notContaining
    * fuzzyEqual
    * regex

**Examples**  
The next example will show different type of queries that can be called using the code generated: 


```graphql
query {
    person(filter: { id: "044453524134" }) {
        name
        age
    }
}

query {
    person (filter: { age: lowerThen 45 }){
        name
        age
    }
}


```

- **Collection filter operators**

  * in
  * notIn

- **Relationship filter operators**

  TODO



**Entity Aggregation Operation **
   
   TODO


- **Create**
```graphql
directive @relation(mappingType: String) on FIELD_DEFINITION
directive @key(fields: [String], name: String) on OBJECT

type CreatePersonMutationResponse {
    person: Person!
    result: String 
    cause: String
}

type CreateProfessionMutationResponse {
    profession: Profession!
    result: String
    cause: String
}

type Mutation {
    createPersons(input: [Person]): [CreatePersonMutationResponse]
    createProfessions(input: [Profession]): [CreateProfessionMutationResponse]
}

```

### Auto Generation of Queries

### Using GraphQL-Typed-PPL based ingestion rules 
