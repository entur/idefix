package org.entur.ror.idefix.file;

import java.io.IOException;
import java.nio.file.Path;

public interface FileService {
    Path getTimetableZip(Path tempDir) throws IOException;

    Path getRegistryZip(Path tempDir) throws IOException;

    void publishOutput(Path outputZip) throws IOException;
}
