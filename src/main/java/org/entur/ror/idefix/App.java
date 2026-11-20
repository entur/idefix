package org.entur.ror.idefix;

import org.entur.ror.idefix.replacement.QuayRefReplacementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {

    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        AppArguments arguments = AppArguments.parseOrExit(args);

        LOGGER.info("Starting Idefix...");
        try {
            new QuayRefReplacementService().run(
                    arguments.timetablesDir(), arguments.registryXml(), arguments.outputDir());
        } catch (Exception e) {
            LOGGER.error("Idefix failed", e);
            System.exit(1);
        }
    }
}
