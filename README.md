## Simple Schema for Open Search

The purpose of this Plugin is to enhance open-search engine with user-domain schema by using GraphQL.  

These capabilities include:
- **Schema creation**
- **Code Generation**
- **Query support**
- **Endpoint Generation**

## How does it work?

With Open-Search as the search engine database, the simple-search plugin employs GraphQL Library to allow applications to have their data treated natively from the front-end all the way to storage,
avoiding duplicate schema work and ensuring flawless integration between front-end and backend developers.

The simple-schema opensearch plugin-Library allows describing the schema of your data, it can generate an entire executable schema with all the additional types needed to execute queries and mutations to interact with your database.

## Features

The simple-schema plugin-Library presents a large feature set for interacting with opensearch database using GraphQL:
- Automatic generation of Queries and Mutations for CRUD interactions
- Various Types, including temporal and spatial types
- Support for both entity and relationship properties
- Extensibility through the @custom directive and/or Custom Resolvers
- Extensive Filtering and Sorting options
- Options for index Autogeneration and Default Values
- Multiple Pagination options

### Documentation

Read the [documentation](https://github.com/opensearch-project/simple-schema/docs)

## Quick start

Once you have installed opensearch, you can install simple-schema from a remote URL or a local file.

1. Browse the [releases](https://github.com/opensearch-project/simple-schema/releases).
2. Find a release that matches your version of opensearch. Copy the name of the .zip file.
3. Install the plugin using the `opensearch-plugin` script that comes with opensearch.

**Example**:

`opensearch-plugin install https://github.com/opensearch-project/simple-schema/releases-0.1-opensearch-2.4.zip`

Read the [installation](https://github.com/opensearch-project/simple-schema/docs/installation) docs for more details.

-------------

## Next steps

Read the [documentation](https://github.com/opensearch-project/simple-schema/docs/basic-usage) to learn about 
 - [entity models](https://github.com/opensearch-project/simple-schema/docs/entity-models)
 - [index auto generation](https://github.com/opensearch-project/simple-schema/docs/index-provider)
 - [query syntax](https://github.com/opensearch-project/simple-schema/docs/query-sytax)
 - [endpoint](https://github.com/opensearch-project/simple-schema/docs/endpoint)


## Security

See [CONTRIBUTING](CONTRIBUTING.md#security-issue-notifications) for more information.

## License

This project is licensed under the Apache-2.0 License.

