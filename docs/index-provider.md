# Index-Provider
This document will describe in details the physical implications of the index template auto generation from GraphQL's Entity Model.
It will review different directives that change how the physical indices are generated to reflect different performance and maintenance concerns.


## Index Provider Explained
Index Provider purpose is to define a low level schematic structure configuration for the underlying physical store

The store will implement the index provider instructions according to its store architecture and capabilities


### Indexing Policy
Opensearch offers the index data structure as its main indexing facility that represents a document class
in general this is a virtual concept which has diverse meaning over the years - see documentation
- https://www.elastic.co/blog/index-vs-type
- https://medium.com/@mena.meseha/understand-the-parent-child-relationship-in-elasticsearch-3c9a5a57f202)

### Entity-Relation-Hierarchy
We can clearly define the notion of entity that is coupled with the notion of an index, this notion can be expanded using
the different tools available on OpenGraph such as:

- Redundancy  - Type that contains redundant fields from other index (mostly a relational typed index)
- Embedding   - Type that holds an embedded type (single instance) that appear as a property field inside the entity
- Nested      - Type that holds a nested type (possible for list of nexted elements) that appear as a property field/s inside the entity
- Partition   - Type that is partitioned according to some field (mostly time based) and is storde acros multiple partitions indices

These Abilities exist to provide the user tools to reflect the diverse use cases he may face.

Since each specific use-case may require different performance from the underlying system, the above options allow to express
these concerns in an explicit way.
 
-------------

### Entities & Relations
OpenGraph logical ontology allows the definition of three different categories:
- entities
- relationships
- enumerations

#### Entities
Entities are a logical structure that represents an entity with its attributes. An entity may hold primitive data types such as
integer fields, date fields, text fields and so...

#### In Addition

Entity also may contain enumerated dictionary or even a subtype entity which is embedded inside the entity structure.

#### Example
Example entity:
```json
        gender_enum: {
            Male,Female,Unknown    
        }

      Person: {
             id:  string
             name: string
             age:  int
             birth: date
             location: geo
             status: bool
             gender: gender_enum
             profession: Profession
           }
     
     Profession: {
            id: string
            name: string
            description: text
            since: date 
            salary:int
        }

```    

We can observe that Person has both simple primitive fields such as string, date, int but also complex fields such as the
gender enumeration, the profession type fields and the geolocation struct.

#### Relations
In a structured schema, entities are connected to each other using meaningfull relationships.
These relationships can be materialized in the physical schema in a number of meaningful ways - depending on time & space considerations.

##### Mapping relations in opensearch

In opensearch there are 4 ways of mapping the relations between entities - these ways actually correspond to the RDBMS / NoSQL concepts and disciplines.
Lets take the former person-profession schema to demonstrate these concepts:


* **Normalization**

In this case the relation itself will be an index names hasProfession that will contain both id (PK) of each side of the relation and possible some
meaningful fields on each relation.

```text
  ##########
  # Person #
  #  id    #
  #  name  #                       #####################    
  #  ....  #                       #    hasProfession  # 
  #        #                       #    since          #                   ##################### 
  #        # --------------------- #    personId       # ----------------- #   Profession      #
  #        #                       #    personName     #                   #   id              #
  ##########                       #    ProfId         #                   #   name            #
                                   #    ProfName       #                   #   rank            #
                                   #                   #                   #   salary          #
                                   ######################                  #                   #
                                                                           #####################  
```
In this case in order to view joined information of both a person and his profession one must explicitly join the indices
based on the joining hasProfession index keys.

* **Embedding**
  In this case the relation will be an embedded object inside the source side index. In case this is a b-directional relationship - both of the
  opposing sides of the relationships will be embedded on the other side's index.

- In this case the Person index will look like this:

```text
  ##########
  # Person #
  #  id    #
  #  name  #
  #  ....  #             
  # has:[ Profession ] # 
  #    | Id            # 
  #    | name          # 
  #    | rank          # 
  #    | salary        # 
  ###################### 

```
- If the relation is b-directional, the Profession index will look like this:

```text
  ##############
  # Profession #
  #  id        #
  #  name      #
  #  rank      #
  #  ....      #            
  # heldBy:[ Person ]  # 
  #    | Id            # 
  #    | name          # 
  #    | age           # 
  #    | ...           # 
  ###################### 

```


Since opensearch flattens the object's internal structure - this solution fails for the one->many type of relationships.
On such case the nesting type of mapping will be selected.

see - [https://www.elastic.co/guide/en/elasticsearch/reference/current/object.html]()

* **Nesting**

Nesting is a similar case to the embedding option but it is selected for the case of the relation being one to many. Due to the nature of
object field mapping in opensearch, has no concept of inner objects. Therefore, it flattens object hierarchies into a simple list of field names and values.

see - [https://www.elastic.co/guide/en/elasticsearch/reference/current/nested.html]()

The nesting mapping of the relationship will look very similar to the embedded nature with the additional penalty of document storage per relation.

* **Parent-Child**

- **TODO**

* **Referencing**

In this case, we have a one-to-many relationships patterns with many more relationships than entities.

Here will store the relationship in two manners:
- One as nested relationship similar to the nexted case - the only difference is that the inner nested relationship representation will have bare minimal
  fields beside the FK to the other side of the relationship entity.
- The second will be the opposing side of the relationship in case of many-to-many or just the embedding part in the case of one-to-many

```text
  ##########
  # Person #
  #  id    #
  #  name  #
  #  ....  #                                        ##################
  # has: [ Profession ]#                            #   Profession   #
  #             | Id   # -------------------------- #    ID          #
  #             | name #                            #    name        #
  ######################                            #    salary      #
                                                    #    .....       #
                                                    # heldBy: [Person]   #
                                                    #           | ID     #
                                                    #           | name   #
                                                    ######################
```

* **Mixing**

This use case will combine a user selected relationship materialization - mainly to overcome performance issues. This case is the typical de-normalizations projections
of the entity->relationship according to pre-defined queries. This is also known as materialized pre-calculated joins pattern.


-------------

### Schema Store
The 'opensearch' index provider offers the next possibilities to store (Index) Person / Profession entities :

##### Static
Direct mapping of the entity type to a single index
```json
       {
           "type": "Person",
           "partition": "static",  => shcematic mapping type
           "mapping": "Index",      
           "props": {
             "values": ["person"]  => this is the name of the physical index
           },
           "nested": [
             {
               "type": "Profession",  => this is the inner type belonging to the person entity
               "mapping": "child",    => inner type store as an embedded entity (other option in nested) 
               "partition": "nested",
               "props": {
                 "values": ["profession"]   => this is the name of the physical index
               }
             }
           ]
         }             
  ```   

##### Partitioned
Mapping of the entity type to a multiple indices where each index is called after some partitioned based field

 ```json
        {
           "type": "Person",    => this is the name of the physical index
           "partition": "time", => this is the partitioning type of the index
           "mapping":"Index",
           "symmetric":true
           ],
           "props": {
             "partition.field": "birth", => the partitioned field
             "prefix": "idx_person",     => the inedx common name
             "index.format": "idx_person_%s", => the incremental index naming pattern
             "date.format": "YYYY",           => the date format for the naming pattern
             "values": ["1900", "1950", "2000","2050"] => the indices incremental time buckets
           }
         }
  ```

##### Unified
Mapping of all the entities types to form a consolidated index for all types of entities 

 ```json
        {
           "type": "Unified",    => this is the name of the physical index
           "mapping":"Index",
           "symmetric":true
           ],
            "props": {
              "values": ["unified"]  => this is the name of the physical index
            }
         }
  ```

##### Nested
Mapping the entity (sub)type to an index containing as an embedded/nested/child document

The mapping field can have one of the following nesting options:
 - Embedded
 - Nested
 - Child

In this example the Profession is the nested entity here ...

```json
    {
      "type": "Person",
      "partition": "unified", => shcematic mapping type
      "mapping": "Index",
      "props": {
        "values": ["ontology"] => the unified index name
      },
      "nested": [
        {
          "type": "Profession",
          "mapping": "child", => "child" represents nested and "embedded" represents embedding the document insde the index
          "partition": "nested",
          "props": {
            "values": ["ontology"]
          }
        }
      ]
    }
```   

-------------

#### Relationships

Relationships are a logical structure that represents a relationship between two entity with its attributes.
A relationship may hold primitive data types such as integer fields, date fields, text fields and so...

##### Storing relationships
Relationships always connect two entities (this scope doesn't include start type relationships) and therefor identified
by the two unique ids of each side of the relation. Let's take the former example with the two entities Person and Profession.

_Storage Options:_

If we would like to store this simple schema (Person,Profession) into opensearch - we can use one of the following index compositions:

1) Store each entity in a different Index: PersonIndex & ProfessionIndex and an additional index for the relationships (similar to an RSBMS noarmalized tables schema)
2) Store only one entity in an Index: PersonIndex & the second entity (Profession) will be nested inside that index - expressing mainly the first side of the relationship.
3) Store each entity in a different Index: PersonIndex & ProfessionIndex and each index will contain the other side of the relation as a nested document (relationship directionality has to be expressed as well).

While the first option is very typical to relational databased - it is tuned for space efficiency, the third option is typical to NoSql databased & it is tuned for search efficiency.

Another option that attempts to mitigate both concerns will be to create an index for each entity and store only the relation as a nested document.

- Person will have an index with (in addition to its own fields) a nested relationship document containing only Profession ID & possibly some additional redundant field.
- The other side of the relation (Profession) will also hold the skeleton version of the Person with the Id & some minimal basic fields (redundant fields representation).

This approach becomes highly efficient when there are **far more relationships than entities** and each side of the relation has many fields.

**_ redundant fields explained in the following section_**

```text
  ##########
  # Person #
  #  id    #
  #  name  #
  #  ....  #                                        ##################
  #  [ Profession ]    #                            #   Profession   #
  #             | Id   # -------------------------- #    ID          #
  #             | name #                            #    name        #
  ######################                            #    salary      #
                                                    #    .....       #
                                                    #   [Person]         #
                                                    #           | ID     #
                                                    #           | name   #
                                                    ######################
```  
Adding such capabilities extends the entities-relationship model implementations options and allow handling additional low-level optimization.

---
##### Redundancy
Redundancy is the ability to store redundant data on the relationship element that represents the information residing on either the side(s) of the relation.

**Example:**

Lets consider a 'Call' relationship type between two **person** entity

* **SideA** - is the left Side of the relationship  - a Person
* **SideB** - is the right Side of the relationship - a Person

```json
    {
        "type": "Call",
        "partition": "time", 
        "mapping":"Index",
        "symmetric":true,
        "redundant": [  => this section states which fields of the related entities are stored on the relation itself
          {
            "side":["entityA","entityB"], => indicate the side that the fields are taken from
            "redundant_name": "name",     => the field redundant name - in the relation index 
            "name": "name",               => the field original name - in the entity index
            "type": "string"              => the field type
          },
          {
            "side":["entityA","entityB"],
            "redundant_name": "number",
            "name": "number",
            "type": "string"
          }
        ],
        "props": {
          "partition.field": "date",
          "prefix": "idx_call",
          "index.format": "idx_call_%s",
          "date.format": "YYYY",
          "values": ["2001", "2002", "2003","2004"]
        }
      }
```

We are explicitly stating that name & number fields are saved (duplicated from their original index) for redundancy reasons to allow search acceleration.
The ability to query the same index for the 'remote' side of the relationship boosts performance on the expense of additional storage.

### Composition of template mappings

This capability is supported by the composable index mapping template - it allows for creation of small template-mapping building blocks. These building blocks allow
the composition of different composite indices which are based on different consumption use-cases.

For example - if we would like to generate a dashboard view which is backed by an index which projects some composable structure of elements (similar to views in RDBMS), the
composable template mapping allows this capability exactly.

A typical usage pattern will be to generate a component template mapping per each (top level) entity in the schema - each such component can be afterwards composed together
with additional components to reflect some specific portion of the schema that is used for some pre-defined purpose.

**IndexProvider** - syntax offers support for this capability using the 'template' directive (**TODO**)