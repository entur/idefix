# idefix

Idefix transforms NeTEx timetable XML by replacing QuayRef references with actual ids from a stop-place registry. 

## How it works

Idefix takes a timetable ZIP and a stop-place registry ZIP as input. It parses the registry to build a mapping from imported QuayRef IDs to their canonical IDs, then transforms all matching QuayRef elements in the timetable XML. The result is written to an output ZIP.

## Running idefix locally

You may run idefix locally by providing the paths to the source files as arguments. 

```sh
java -jar target/idefix-*-SNAPSHOT.jar <timetable.zip> <registry.zip> [output.zip]
```

`output.zip` defaults to `output.zip` in the current directory if omitted.

## Running with GCS

Set the following environment variables, then run without arguments:

| Variable | Description |
|---|---|
| `TIMETABLE_BUCKET` | GCS bucket for the timetable ZIP |
| `TIMETABLE_PATH` | Object path within the timetable bucket |
| `REGISTRY_BUCKET` | GCS bucket for the registry ZIP |
| `REGISTRY_PATH` | Object path within the registry bucket |
| `OUTPUT_BUCKET` | GCS bucket for the output ZIP |
| `OUTPUT_PATH` | Object path within the output bucket |

