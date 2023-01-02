# Opensearch relationships 

The next document will describe the different storage options for managing relationships inside opensearch and how they are 
used in the context of modeling a graph storage.

## Index Mapping 
Index mapping allows the creation of a structure for an index. This structure can be static or dynamic and allows different representations
of data according to specific needs.

During the next examples we will discuss how to represent relationship between two document - 

**Object type** —

This type of objects embedding allows you to have an object (with its own fields and values) as the value of a field in your document.
For example, your address field for an event could be an object with its own fields:
    city, postal code, street name, and so on.
You could even have an array of addresses if the same event happens in multiple cities.



**Nested documents**—

The problem you may have with the object type storage is that all the data is stored in the same document, so matches for a search can go across sub-documents.
For example, city=Paris AND street_name=Broadway could return an event that’s hosted in New York and Paris at the same time, even though there’s no Broadway street in Paris.

Nested documents allow you to index the document in a manner that will keep your addresses in separate Lucene documents, making  searches like city=New York & street_name=Broadway return the expected result.


**Parent-child relationships between documents**—

This type of storage schema allows you to use a completely separate documents for different types of data, like events and groups, but still define a relationship between them.
For example, you can have groups as parents of events to indicate which event hosts which group.

This type of mapping allow you to search for events hosted by groups or for groups that host events about some popular fashion item.


**Denormalizing** —

This is a general technique for duplicating data in order to represent relationships.

In opensearch, you’re likely to employ it to represent many-to-many relationships because other options work only on one-to-one / one-to-many.
For example, all groups have members, and members could belong to multiple groups. You can duplicate one side of the relationship by including all the members of a group in that group’s document.

This technique is also useful the direction of the search is unknown in advanced - the duplication allows for the entire search to be resolved in a single index - 
no matter which side of the relation the query has begun.


**Application-side joins**—

This is another general technique where you deal with relationships from outside the engine scope.
In this technique an many-to-many index is created that stores both the id's of each relation - similar to how we represent many-to-many relationship in a relational DB

We run two queries: first, on the first side to filter those matching member criteria, Then we take their IDs and include them in the search criteria for next side - this is done against the man-to-may index
in order to fetch the other's side ids. Once its done - the last query fetches the actual content of the other joined elements once again filtering those matching some given criteria.


## Typical Use Cases

### Object type
The most common way this case would represent a one to one relationship with its containing object

This allows you to put a JSON object or an array of JSON objects as the value of your field, like the following example:

```json
{
    "name": "open source Meetings group",
        "events": [{"date": "2021-12-22","title": "Introduction to opensearch"},
                   {"date": "2022-06-20","title": "Using apache Pinot"}]
}

````

If you want to search for a group meeting with events that are about opensearch, you can search the events.title field.

Under the hood, opensearch isn’t aware of the structure of each object - it will flatten the hierarchy into dot seperated fields.

The document ends up being indexed as if it looked like this:

```json
{
    "name": "open source Meetings group",
    "events.date": ["2021-12-22", "2022-06-20"],
    "events.title": ["Introduction to opensearch", "Using apache Pinot"]
}
````

Because of how they’re indexed, object mapping search works as expected when you need to query only one field of the object at a time (generally one-to-one relationships),
When querying multiple fields (as is generally the case with one-to-many relationships), you might get unexpected results.

For example, let’s say you want to filter groups events hosting Apache's Pinot meetings for the period of December 2021.

```json
"bool": {
    "must": [
        {
          "term": {
            "events.title": "Pinot"
                }
        },
        {
          "range": {
            "events.date": {
                "from": "2021-12-01",
                "to":   "2021-12-31"
              }
            }
        }
    ]
}
````


This will match the document because it has a title that matches Pinot and a date that’s in the specified range - obviously this wasn't our intention.  

The object mapping type isn't aware of the boundaries between documents - it groups the (internal) documents according to their fields and embeds them as properties in the containing documents...


### Nested type

Making sure such cross-object search matches mistake don’t happen, we need the nested type, which will index the group's meeting events in separate Lucene documents.
The group’s document will look exactly the same, and applications will index them in the same way. The difference mapping will instruct opensearch to index nested inner objects as separate Lucene documents (within the same lucene index - O/S shard).
When searching, we use nested filters and queries. Nested queries search in all those Lucene documents.


The main disadvantage of this technique is that once indexing a meeting group and all its nesting meetings, every new addition of a nested meeting will trigger the reindexing of the entire group (the containing document and its nested documents)
This can hurt performance and concurrency, depending on how big those documents get and how often those operations are done.


### Parent-child relationships

With parent-child relationships, we use a completely different opensearch documents by putting them in different types and defining their relationship in the mapping of each type.

For example, we can use events in one mapping type and groups in another, and we specify in the mapping that groups are parents of events.

Also, when indexing an event - we point it to its origin group.

At search time, we have the next possible queries:
 - **_has_parent_**  
 - **_has_child_**

Different types of opensearch documents can have parent-child relationships - the disadvantage here is that an index can only have one relationship mapping defined within it.

Another drawback is the "distance" the documents are compared to the nested mapping where both documents are in the same lucene index.  


### Applicative Joins

With a similar notion to the relational DB, applicative joins are joins that are not performed as part of the core DSL.
This use case includes the two sides of the relationship and a mapping index where each document contains the two ids of the participating sides.
Using this join is subject to lower performance in compare with the prior cases where the locality of the relationships played an important role.

This is a common case for many to many mapping between entities.
