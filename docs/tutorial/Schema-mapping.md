# Schema Mapping
This tutorial will review the steps of creating a sample schema and generating a mapping for that schema.
It will review several use cases for mapping the logical schema into physical indices.

### Entities and Relations
The GraphQL schema is composed of types, fields and enumerations.
The types are related to one another via containment and special named directive (**@relation**) that dictate the type of 
relationships between entities.


#### Using the  Author - Book example
Lets review the next simple schema composed of an Author and books:

```graphql
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
```

We would like to generate an appropriate index mapping to store this schema.
Using the **_Directives_** we can instruct the compiler to generate the mapping according to different strategies.

---

### Strategy 1: Single Index
In this strategy the Books are Nested objects inside the Author document - they are all mapped into a single index:

```text
  ##############
  # Author #
  #  id          #
  #  name        #
  #  born        #
  #  nationality #
  #  ....        #            
  # has_Book:[ Book ]  # 
  #    | ISBN          # 
  #    | title         # 
  #    | genre         # 
  #    | ...           # 
  ###################### 

```

The Books are **_nested_** document so that they can reside very close to the Author document 
See [Entities And Relationships in opensearch](EntitiesAndRelationships.md)

#### Directives
The proper way instructing the compiler to use this strategy is by using the **@model** & **@relation** directive in the following way:

```graphql
type Book {
    ISBN: ID!
    title: String!
    author: Author! @relation(mappingType: "reverse")
    description: Text
    published: DateTime!
    genre:Genre

}
type Author @model {
    id: ID!
    name: String!
    born: DateTime!
    age: Int
    died: DateTime
    nationality: String!
    books: [Book] @relation(mappingType: "nested")
}
```

We will mark the Author with **@model** directive to instruct the compiler to dedicate an index for this entity.

We will use @relation directive for the Author's Books field:  
```graphql
    books: [Book] @relation(mappingType: "nested")
```
This relation directive has a **mappingType** argument for the relationship resolver to deduce how to implement the relationship physically.
In this case it would be implemented as a nested document within the Author document.

The Book's Author field is also marked with the @relation directive:
```graphql
    author: Author! @relation(mappingType: "reverse")
```
This time the **mappingType** argument states the relationship from the Book to the Author is a **reverse** meaning it only has a logical representation and no physical one.
It will be used by the Query Translator to build a reveres query from the book to an author.

### Strategy 2: Two Indexes
In this strategy both Books and Authors have a dedicated index - they will reference each other using foreign keys.

```text
  ################
  # Author       #
  #  id          #
  #  name        #
  #  born        #
  #  nationality #
  #  ....        #            
  # has_Book: [ Books ] #                            ###-Book-#########
  #           | ISBN    # ------------------------   #    ISBN        #
  ######################                             #    title       #
                                                     #    genre       # 
                                                     #    ...         # 
                                                     #    has_Author: [Person]   #
                                                     #           | ID            #
                                                     #############################
```

#### Directives
The proper way instructing the compiler to use this strategy is by using the **@model** & **@relation** directive in the following way:

```graphql
type Book  @model {
    ISBN: ID!
    title: String!
    author: Author! @relation(mappingType: "foreign")
    description: Text
    published: DateTime!
    genre:Genre

}
type Author @model {
    id: ID!
    name: String!
    born: DateTime!
    age: Int
    died: DateTime
    nationality: String!
    books: [Book] @relation(mappingType: "foreign")
}
```
We will mark both the Author & Book with **@model** directive to instruct the compiler to dedicate an index for these entities.

We will use @relation directive for the Author & Books field:
```graphql
    books: [Book] @relation(mappingType: "foreign")
```
```graphql
    author: Author! @relation(mappingType: "foreign")
```

This relation directive has a **mappingType** argument for the relationship resolver to deduce how to implement the relationship physically.
In this case it would be implemented as a foreign reference to the remote document. The FK by which the reference is pointing is derived using the ID field of the referenced entity

### Strategy 3: Three Indexes
In this strategy both Books and Authors have a dedicated index - they will both reference a third join index representing the relationships between them.

Here the relation itself will be an index names 'written' (according to the explicit name the user stated in the name argument) that will contain both id (PK) of each side of the relation and possible some
meaningful fields on each relation.

```text
  ################
  #-Author-      #
  #  id          #
  #  name        #
  #  born        #
  #  nationality #                 #####################    
  #  ....  #                       #   -written-       # 
  #        #                       #    id   (author)  #                   ##################### 
  #        # --------------------- #    ISBN (book)    # ----------------- #  -Book-           #
  #        #                       #   ............    #                   #   ISBN            #
  ##########                       #                   #                   #   title           #
                                   #                   #                   #   genre           #
                                   #                   #                   #   ......          #
                                   ######################                  #                   #
                                                                           #####################  
```
In this case in order to view joined information of both an Author and his books one must explicitly join the indices based on the joining index keys.

#### Directives
The proper way instructing the compiler to use this strategy is by using the **@model** & **@relation** directive in the following way:

```graphql
type Book  @model {
    ISBN: ID!
    title: String!
    author: Author! @relation(mappingType: "join_index_foreign", name: "written")
    description: Text
    published: DateTime!
    genre:Genre

}
type Author @model {
    id: ID!
    name: String!
    born: DateTime!
    age: Int
    died: DateTime
    nationality: String!
    books: [Book] @relation(mappingType: "join_index_foreign", name: "written")
}
```
We will mark both the Author & Book with **@model** directive to instruct the compiler to dedicate an index for these entities.

We will use @relation directive for the Author & Books field:
```graphql
    books: [Book] @relation(mappingType: "join_index_foreign", name: "written")
```
```graphql
    author: Author! @relation(mappingType: "join_index_foreign", name: "written")
```
This relation directive has a **mappingType** argument for the relationship resolver to deduce how to implement the relationship physically.
In this case it would be implemented as an independent join index foreign reference to the remote document - the **name** argument states that both relations share the same join index named ''written'.

The foreign keys by which the reference is pointing is derived using the ID field of the referenced entities (_id_ for **Author** and _ISBN_ for **books**)

---

## Conclusion

We've seen the 3 strategies we can employ to physically store the logical schema in the storage engine.
For additional information see
 - [physical mapping](../physical-mapping.md)
 - [index provider](../index-provider.md)