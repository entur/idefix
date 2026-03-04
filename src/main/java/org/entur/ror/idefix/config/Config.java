package org.entur.ror.idefix.config;

public record Config(
        String timetableBucket,
        String timetablePath,
        String registryBucket,
        String registryPath,
        String outputBucket,
        String outputPath
) {
    public static Config fromEnv() {
        String timetableBucket = requireEnv("TIMETABLE_BUCKET");
        String timetablePath = requireEnv("TIMETABLE_PATH");
        String registryBucket = requireEnv("REGISTRY_BUCKET");
        String registryPath = requireEnv("REGISTRY_PATH");
        String outputBucket = requireEnv("OUTPUT_BUCKET");
        String outputPath = requireEnv("OUTPUT_PATH");
        return new Config(timetableBucket, timetablePath, registryBucket, registryPath, outputBucket, outputPath);
    }

    private static String requireEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Required environment variable " + name + " is not set");
        }
        return value;
    }
}
