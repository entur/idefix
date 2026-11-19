# idefix

Idefix transforms NeTEx timetable XML by replacing QuayRef references with actual ids from a stop-place registry and
aggregates the results into one directory for OTP consumption.

## How it works

Idefix takes a directory of timetable XMLs (one subdirectory per provider) and a stop-place registry XML file as input. It parses the registry to build a mapping from imported QuayRef IDs to their canonical IDs,
then transforms all matching QuayRef elements in each provider's `_shared_data.xml`. All timetable files are written to
the output directory prefixed with the provider's name, together with the registry file as `_stops.xml`. The providers' own `_stops.xml` files are skipped.

## Running idefix

All three arguments are required:

```sh
java -jar target/idefix-*-SNAPSHOT.jar <timetables dir> <registry> <output dir>
```

| Argument           | Description                                                                         |
|--------------------|-------------------------------------------------------------------------------------|
| `<timetables dir>` | Directory with one subdirectory of XML files per provider, e.g. `timetables/skane/` |
| `<registry>`       | Stop-place registry XML file                                                        |
| `<output dir>`     | Directory the result is written to. Created if missing; existing files are replaced |
