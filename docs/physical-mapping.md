# Physical Mapping

This document reviews the strategies that the mapping component uses for translating the logical
entities & relations composition into a physical mapping schema used be opensearch.

The physical mapping strategies is strongly coupled with how opensearch stores documents inside its indices.
For additional information on this see [entities & relationships](EntitiesAndRelationships.md)

### Simple Schema

In our basic samples and tutorial we will use the following Author->Books schema model to represent
different entities and their relationships.

````graphql
type Book {
    ISBN: ID!
    title: String!
    author: Author!
    description: Text
    published: DateTime!
    genre:Genre

}
type Author {
    id: ID!
    name: String!
    born: DateTime!
    age: Int
    died: DateTime
    nationality: String!
    books: [Book] 
}

enum Genre {
    AdventureStories
    Classics
    Crime
    FairyTales
    Fantasy
    HistoricalFiction
    Horror
    Humour
}
````
_See [simple.graphql](../schema/sample/simple.graphql)_

---

## Directives

The directives are 'hints' assertions that allow the user to specifically instruct the (mapping) compiler on how to
translate the logical component into its physical manifestation.

### Types of Directives

### @Model

- #### Applies:_[ Entities, Relations]_

```graphql
directive @model on OBJECT
```

This directive instructs the compiler regarding the component (entity / relation) that it should be represented in its
own
independent physical index.

#### Example

````graphql
type Author @model{
    name: String!
    born: DateTime!
    age: Int
    died: DateTime
    nationality: String!
    books: [Book]
}
````

Here the 'author' entity would have a physical index named author representing it.


### @Relation

- #### Applies:_[ Fields]_
- #### Arguments:
  - _mappingType_
  - _name_

```graphql
directive @relation(mappingType: String!, name: String) on FIELD_DEFINITION
```

This directive instructs the compiler regarding an entity's fields - these fields are actually representing
a relationship to some other entities and should have these relations represented in the following manner:

#### Example

````graphql
type Author @model{
    id: ID!
    name: String!
    born: DateTime!
    age: Int
    died: DateTime
    nationality: String!
    books: [Book]  @relation(mappingType: "child", name: "written" )
}
````

Here the 'author' books field is actually a relationship representing the Author's written books.
The relationship argument **mappingType** instructs the compiler on how to physically represent this relation to the
Book entity. The second argument **name** indicates the physical name of the relationship.

### Arguments

- **_MappingType_**:

Relation has a 'mappingType' argument which can accept the next values:

- EMBEDDED
- NESTED
- CHILD
- FOREIGN
- JOIN_INDEX_FOREIGN
- REVERSE

_See [PhysicalEntityRelationsDirectiveType](../core/src/main//java/org/opensearch/schema/ontology/PhysicalEntityRelationsDirectiveType.java)_

### EMBEDDED

The physical entities (documents) are **embedded** in the parent document - fields are flattened (according to the dot
separation)     

Let's review the next example:
````graphql
type Author @model{
    id: ID!
    name: String!
    born: DateTime!
    age: Int
    died: DateTime
    nationality: String!
    books: [Book]  @relation(mappingType: "embedded", name: "written")
}
````
In this case, the book entity would be mapped as an embedded document inside the outer Author document.

This is the outcome of the index mapping generation for this schema:

```json
todo
```

_**NOTE**:_ 
- This type can't be defined if the relation entity (Book) is defined as @Model - this will raise a schema validation error.

### NESTED
The physical entities (documents) are **nested** in the parent document - fields are nested (
documents are indexed as separately inside the same index)

Let's review the next example:
````graphql
type Author @model{
    id: ID!
    name: String!
    born: DateTime!
    age: Int
    died: DateTime
    nationality: String!
    books: [Book]  @relation(mappingType: "embedded", name: "written")
}
````
In this case, the book entity would be mapped as a nested document inside the outer Author document (stored in the same index).

This is the outcome of the index mapping generation for this schema:

```json
todo
```

_**NOTE**:_
- This type can't be defined if the relation entity (Book) is defined as @Model - this will raise a schema validation error.

### CHILD
The physical entities (documents) are **child** to the parent document - fields are stored in a parent-child document mapping strategy

_see [parent-child join](https://opensearch.org/docs/1.3/opensearch/supported-field-types/join/)_

Let's review the next example:
````graphql
type Author @model{
    id: ID!
    name: String!
    born: DateTime!
    age: Int
    died: DateTime
    nationality: String!
    books: [Book]  @relation(mappingType: "child", name: "written")
}
````
In this case, the book entity would be mapped as a child document of the outer Author document.

This is the outcome of the index mapping generation for this schema:

```json
todo
```

_**NOTE**:_
- This type can't be defined if the relation entity (Book) is defined as @Model - this will raise a schema validation error.

### FOREIGN
The physical entities (documents) are **foreign** to the parent document - fields actually reference another document which stored in a different index

Let's review the next example:
````graphql
type Author @model{
    id: ID!
    name: String!
    born: DateTime!
    age: Int
    died: DateTime
    nationality: String!
    books: [Book]  @relation(mappingType: "foreign")
}

type Book @model{
  ISBN: ID!
  title: String!
  author: Author!  @relation(mappingType: "foreign")
  description: Text
  published: DateTime!
  genre:Genre
}
````
In this case, the book reference would be mapped as a pointer (containing the FK to the books) to the book real document
The book document would be stored in its own index and is expected to have a @Model directive.

The relationships are stored on each side of the relationship:
 - **Author->Book** has a mapping books field that stores the FK of the remote books entities 
 - **Book->Author** has a mapping books field that stores the FK of the remote Author entity 

This is the outcome of the index mapping generation for this schema:

```json
todo
```

_**NOTE**:_
- This type is the only type allowed in case the relation entity (Book) is defined as @Model

### JOIN_INDEX_FOREIGN
The physical entities (documents) are **join_index_foreign** to the parent document - fields actually reference another document which stored in a different join index
that stores the relationships as documents

Let's review the next example:
````graphql
type Author @model{
    id: ID!
    name: String!
    born: DateTime!
    age: Int
    died: DateTime
    nationality: String!
    books: [Book]  @relation(mappingType: "join_index_foreign", name: "author_books")
}

type Book @model{
  ISBN: ID!
  title: String!
  author: Author!  @relation(mappingType: "join_index_foreign", name: "author_books")
  description: Text
  published: DateTime!
  genre:Genre
}
````
In this case, both author & books would need to be @model since they both reside in their own independent index.
The difference from the former FOREIGN relationship type is that the relationship has its own index named **_author_books_** where each document represents a relationship between the entities.

This is the outcome of the index mapping generation for this schema:

```json
todo
```

_**NOTE**:_
- This type is the only type allowed in case the relation entity (Book) is defined as @Model

### REVERSE

The **reverse** mappingType indicates that the target entity (book in our case) is logically pointing back to its containing
entity (Author)

Let's review the next example:
````graphql
type Author {
    id: ID!
    name: String!
    born: DateTime!
    age: Int
    died: DateTime
    nationality: String!
    books: [Book]  @relation(mappingType: "nested")
}

type Book {
  ISBN: ID!
  title: String!
  author: Author!  @relation(mappingType: "reverse")
  description: Text
  published: DateTime!
  genre:Genre
}

````

This specific relation has no actual physical representation - it only marks this logical type of relation so that 
the query engine can infer how to query based on the parent's type of containment (nested / parent-child )

_**NOTE**:_
- This type can only be defined if the source relation entity (Author) is defined as @Model and the target entity (Book) not defined as a @Model
meaning the relation mappingType is either EMBEDDED / NESTED / CHILD

### Arguments

- **_Name_**:

Relation has a _'name'_ argument which can accept a string value. This value represents the name of the relationship.
In case the name doesn't exist it would receive the template name has_$target where the target is the destination of the relationship.
In the **Author->Book** the relationship will be named **_has_Book_**.

