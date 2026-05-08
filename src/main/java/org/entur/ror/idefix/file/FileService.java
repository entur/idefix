package org.entur.ror.idefix.file;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public interface FileService {
    List<String> getProviders();

    Path getTimetableZip(Path tempDir, String provider) throws IOException;

    Path getRegistryZip(Path tempDir) throws IOException;

    void publishAggregatedOutput(Path aggregatedZip) throws IOException;
}
