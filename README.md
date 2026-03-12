# idefix

Idefix transforms NeTEx timetable XML by replacing QuayRef references with actual ids from a stop-place registry.

## How it works

Idefix takes one or more timetable ZIPs and a stop-place registry ZIP as input. It parses the registry to build a
mapping from imported QuayRef IDs to their canonical IDs, then transforms all matching QuayRef elements in each
timetable XML. The result is written to one output ZIP per provider.

## Running idefix locally

You may run idefix locally by providing the paths to the source files as arguments.

```sh
java -jar target/idefix-*-SNAPSHOT.jar <timetable.zip> <registry.zip> [output.zip]
```

`output.zip` defaults to `output.zip` in the current directory if omitted.

## Running with GCS

Set the following environment variables, then run without arguments:

| Variable              | Description                                                              |
|-----------------------|--------------------------------------------------------------------------|
| `TIMETABLE_BUCKET`    | GCS bucket for the timetable ZIPs                                        |
| `TIMETABLE_PROVIDERS` | Comma-separated list of providers (e.g. `provider1,provider2,provider3`) |
| `REGISTRY_BUCKET`     | GCS bucket for the registry ZIP                                          |
| `REGISTRY_PATH`       | Object path within the registry bucket                                   |
| `OUTPUT_BUCKET`       | GCS bucket for the output ZIPs                                           |

Timetable ZIPs are fetched from `{TIMETABLE_BUCKET}/{YYYY/MM/DD}/timetable/{provider}.zip` using today's date. Output
ZIPs are uploaded to the same date-based path in `OUTPUT_BUCKET`.
