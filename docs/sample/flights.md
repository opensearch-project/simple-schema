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

Flights are accessible through the following queries.
Concrete examples are available in `schema/flight/flight-example-queries.graphql`.

```graphql
query {
    flight (id: ID)
    flights (filter: FlightFilterInput)
}
```

Additionally, a JSON mapping should be provided to map keys between Simple Schema and OpenSearch.
For example, a mapping may contain the following keys:

```json
{
  "flightNumber": "FlightNum",
  "time": "timestamp",
  "price": "AvgTicketPrice",
  "origin/city": "originCityName",
  "destination/city": "destCityName"
}
```

Which would allow Simple Schema to translate a GraphQL query like this,

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

to an appropriate query on OpenSearch to efficiently retrieve the results.
For example, it may generate the following SQL query.

```sql
SELECT FlightNum AS flightNumber, timestamp AS time, AvgTicketPrice AS price
FROM flights
WHERE originCityName = 'Amsterdam' AND destCityName = 'Chicago';
```
