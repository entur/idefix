package org.entur.ror.idefix;

import com.google.cloud.storage.StorageOptions;
import org.entur.ror.idefix.config.Config;
import org.entur.ror.idefix.file.FileService;
import org.entur.ror.idefix.file.GcsFileService;
import org.entur.ror.idefix.file.LocalFileService;
import org.entur.ror.idefix.gcs.GcsClient;
import org.entur.ror.idefix.replacement.QuayRefReplacementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public class App {

    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        LOGGER.info("Starting Idefix...");

        FileService fileService;
        if (args.length >= 2) {
            fileService = new LocalFileService(
                    Path.of(args[0]),
                    Path.of(args[1]),
                    args.length >= 3 ? Path.of(args[2]) : Path.of("output.zip"));
        } else {
            fileService = new GcsFileService(
                    new GcsClient(StorageOptions.getDefaultInstance().getService()),
                    Config.fromEnv());
        }

        try {
            new QuayRefReplacementService().run(fileService);
        } catch (Exception e) {
            LOGGER.error("Idefix failed", e);
            System.exit(1);
        }
    }
}
