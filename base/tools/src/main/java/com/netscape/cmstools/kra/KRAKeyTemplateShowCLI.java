package com.netscape.cmstools.kra;

import java.io.FileOutputStream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.dogtagpki.cli.CommandCLI;

import com.netscape.certsrv.key.KeyArchivalRequest;
import com.netscape.certsrv.util.JSONSerializer;
import com.netscape.cmstools.cli.MainCLI;

public class KRAKeyTemplateShowCLI extends CommandCLI {

    public static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(KRAKeyTemplateShowCLI.class);

    public KRAKeyCLI keyCLI;

    public KRAKeyTemplateShowCLI(KRAKeyCLI keyCLI) {
        super("template-show", "Get request template", keyCLI);
        this.keyCLI = keyCLI;
    }

    @Override
    public void printHelp() {
        formatter.printHelp(getFullName() + " <Template ID> [OPTIONS...]", options);
    }

    @Override
    public void createOptions() {
        Option option = new Option(null, "output", true, "Location to store the template.");
        option.setArgName("output file");
        options.addOption(option);
    }

    @Override
    public void execute(CommandLine cmd) throws Exception {

        String[] cmdArgs = cmd.getArgs();

        if (cmdArgs.length < 1) {
            throw new Exception("No Template ID specified.");
        }

        String templateId = cmdArgs[0];
        String writeToFile = cmd.getOptionValue("output");
        String templateDir = "/usr/share/pki/key/templates/";
        String templatePath = templateDir + templateId + ".xml";
        KeyArchivalRequest data = JSONSerializer.fromJSON(templatePath, KeyArchivalRequest.class);

        if (writeToFile != null) {
            try (FileOutputStream fOS = new FileOutputStream(writeToFile)) {
                byte[] bytesArray = data.toJSON().getBytes();
                fOS.write(bytesArray);
            }
        } else {
            MainCLI.printMessage(data.getAttribute("description"));
            System.out.print(data.toJSON());
        }
    }
}
