# Entity-Model
This document will review and describe the building of the user-domain schema model which is described in GraphQL SDL.

### Data schema
The first step to create an entity-model which is backed by a persistent datastore is to define a schema. Using GraphQL schema files as the definition of the data model.
The schema contains data types and relationships that represent the domain's functionality.

## Entity Model Store
Graphs are powerful tools for modeling many real-world phenomena because they resemble our natural mental models and verbal descriptions of the underlying process.
With GraphQL, you model your business domain as a graph by defining a schema; within your schema, you define different types of nodes and how they connect/relate to one another.

**On the client**, this creates a pattern similar to Object-Oriented Programming: types that reference other types.

**On the server**, since GraphQL only defines the interface, you have the freedom to use it with any backend (new or legacy!).

### Supported Schema Types
The entity model is using GraphQL's vocabulary which supports the following logical elements:
  - Interface
  - Scalar
  - Enum
  - Types
  - Union

#### Interface
Interface is an abstract concept which defines a logical set of fields that can be extended by some concrete types.
Interfaces are useful when you want to return an object or set of objects, but those might be of several different types.

#### Scalar
GraphQL comes with a set of default scalar types out of the box:

 - Int: A signed 32‐bit integer.
 - Float: A signed double-precision floating-point value.
 - String: A UTF‐8 character sequence.
 - Boolean: true or false.
 - ID: The ID scalar type represents a unique identifier, often used to fetch an object or as the key for a cache.

The ID type is serialized in the same way as a String; however, defining it as an ID signifies that it is not intended to be human‐readable.
In most GraphQL service implementations, there is also a way to specify custom scalar types. 

For example, we could define a Date type:

```graphql
scalar Date
```

Then it's up to our implementation to define how that type should be serialized, deserialized, and validated.
For example, you could specify that the Date type should always be serialized into an integer timestamp, and your client should know to expect that format for any date fields.

#### Enum
Enum is an enumeration which can be used to outline possible values for a type field -  
enumeration types are a special kind of scalar that is restricted to a particular set of allowed values. This allows you to:

 - Validate that any arguments of this type are one of the allowed values
 - Communicate through the type system that a field will always be one of a finite set of values
  
Here's what an enum definition might look like in the GraphQL schema language:
```graphql
enum Episode {
    NEWHOPE
    EMPIRE
    JEDI
}
```

This means that wherever we use the type Episode in our schema, we expect it to be exactly one of NEWHOPE, EMPIRE, or JEDI.

#### Type
The most basic components of a GraphQL schema are object types, which just represent a kind of object you can fetch from your service, and what fields it has.
 - **Arguments**
Every field on a GraphQL object type can have zero or more arguments, All arguments are named and are passed by name specifically. 
Arguments can be either required or optional. When an argument is optional, it may define a default value.

Since Types are concrete entities they may implement interfaces and by doing so they must have the interface's fields in addition to their own.

#### Union types
Union types are very similar to interfaces, but they don't get to specify any common fields between the types.

```graphql
union SearchResult = Human | Droid | Animal
```
Wherever we return a SearchResult type in our schema, we might get a Human, a Droid, or an Animal. 
Note that members of a union type need to be concrete object types; you can't create a union type out of interfaces or other unions.

On the above example, if you query a field that returns the SearchResult union type it may return either of these concrete types.

The **__typename** field is a system provided field name which is auto-generated to each type. It resolves to a String which lets you differentiate different data types from each other on the client.

#### Input types
Input types are complex types similar to standard type, but they appear in queries and used to pass complex objects.

This is particularly valuable in the case of mutations, where you might want to pass in a whole object to be created. 

In the GraphQL schema language, input types look exactly the same as regular object types, but with the keyword input instead of type.