package org.entur.ror.idefix;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public record AppArguments(Path timetablesDir, Path registryXml, Path outputDir) {

    public static final String USAGE = """
            Usage: java -jar idefix.jar <timetables dir> <registry> <output dir>

              <timetables dir>  Directory with one subdirectory of NeTEx XML files per provider.
                                The subdirectory name is used as the provider name.
              <registry>        Stop-place registry XML file.
              <output dir>      Directory the transformed XML files are written to. Created if missing.""";

    public static AppArguments parseOrExit(String[] args) {
        try {
            return parse(args);
        } catch (InvalidArgumentsException e) {
            System.err.println(e.getMessage());
            System.err.println();
            System.err.println(USAGE);
            System.exit(2);
            // unreachable, but required by the compiler since System.exit is not known to terminate
            throw e;
        }
    }

    public static AppArguments parse(String[] args) {
        List<String> errors = new ArrayList<>();
        if (args.length < 1) errors.add("Missing argument: <timetables dir>");
        if (args.length < 2) errors.add("Missing argument: <registry>");
        if (args.length < 3) errors.add("Missing argument: <output dir>");
        if (args.length > 3) errors.add("Too many arguments: expected 3, got " + args.length);
        if (!errors.isEmpty()) {
            throw new InvalidArgumentsException(errors);
        }

        Path timetablesDir = Path.of(args[0]);
        Path registryXml = Path.of(args[1]);
        Path outputDir = Path.of(args[2]);

        if (!Files.isDirectory(timetablesDir)) {
            errors.add("<timetables dir> is not an existing directory: " + timetablesDir);
        }
        if (!Files.isRegularFile(registryXml)) {
            errors.add("<registry> is not an existing file: " + registryXml);
        }
        if (Files.exists(outputDir) && !Files.isDirectory(outputDir)) {
            errors.add("<output dir> exists but is not a directory: " + outputDir);
        }
        if (!errors.isEmpty()) {
            throw new InvalidArgumentsException(errors);
        }
        return new AppArguments(timetablesDir, registryXml, outputDir);
    }

    public static class InvalidArgumentsException extends RuntimeException {
        public InvalidArgumentsException(List<String> errors) {
            super(String.join(System.lineSeparator(), errors));
        }
    }
}
