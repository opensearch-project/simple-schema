# Flights

This document describes some features of the sample Flights API.
The full schema with example queries is described in `simple-schema/schema/flight`.
Flights follow the following schema, of which specific fields to retrieve must be specified:

```json
{
  "id": "1-xiOYQB9D1xjcRD4ick",
  "flightNumber": "NQSM0CW",
  "time": "2022-11-01T15:00:00+0000",
  "origin": {
    "id": "LGW",
    "name": "London Gatwick Airport",
    "city": "London",
    "country": "GB",
    "location": {
      "lat": 51.148,
      "lon": -0.19
    },
    "region": "GB-ENG",
    "weather": "Damaging Wind"
  },
  "destination": {
    "id": "GEG",
    "name": "Spokane International Airport",
    "city": "Spokane",
    "country": "US",
    "location": {
      "lat": 47.62,
      "lon": -117.53
    },
    "region": "US-WA",
    "weather": "Clear"
  },
  "price": 385.45,
  "delay": {
    "duration": "1H5M",
    "type": "Security Delay"
  },
  "distanceKM": 7539.51,
  "distanceMI": 4684.834
}
```

Queries can only be done at `/flight` and `/flights`.
A single `/flight` can only retrieved by ID.
A collection of `/flights` can be filtered by a `FlightFilterInput`,
examples are available in `schema/flight/flight-example-queries.graphql`.

The purpose of this API is to function as a transpiler.
That is, to be able to map logical GraphQL queries to physical index queries in OpenSearch.
For example, the following GraphQL query,

```graphql
query FlightsFromAmsterdamToChicago {
    flights (filter: {
        origin: {
            city: {
                eq: "Amsterdam"
            }
        }
        destination: {
            city: {
                eq: "Chicago"
            }
        }
    }) {
        flightNumber
        time
        price
    }
}
```

May be mapped to something resembling the following SQL query on the OpenSearch flights database.

```sql
SELECT FlightNum AS flightNumber, timestamp AS time, AvgTicketPrice AS price
FROM flights
WHERE originCityName = 'Amsterdam' AND destCityName = 'Chicago';
```

This may be done by, for example, providing a JSON mapping similar to the following:

```json
{
  "flightNumber": "FlightNum",
  "origin/city": "originCityName"
}
```
