# idefix

Idefix transforms NeTEx timetable XML by replacing QuayRef references with actual ids from a stop-place registry. The output
has the same layout as the input.

## How it works

Idefix takes a directory of timetable XMLs (one subdirectory per provider) and a stop-place registry XML file as input. It parses the registry to build a mapping from imported QuayRef IDs to their canonical IDs,
then transforms all matching QuayRef elements in each provider's `_shared_data.xml`. The output directory mirrors the input: one subdirectory per provider containing the
transformed `_shared_data.xml` and all other XML files copied unchanged, with the same file names.

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
