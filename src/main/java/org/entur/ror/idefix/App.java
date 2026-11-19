package org.entur.ror.idefix;

import org.entur.ror.idefix.replacement.QuayRefReplacementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {

    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
        AppArguments arguments;
        try {
            arguments = AppArguments.parse(args);
        } catch (AppArguments.InvalidArgumentsException e) {
            System.err.println(e.getMessage());
            System.err.println();
            System.err.println(AppArguments.USAGE);
            System.exit(2);
            return;
        }

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
