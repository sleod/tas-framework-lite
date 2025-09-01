package io.github.sleod.tas.web;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.sleod.tas.common.IOUtils.FileOperation;
import io.github.sleod.tas.configuration.PropertyResolver;
import io.github.sleod.tas.core.json.ObjectMapperSingleton;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ChromeUserPreference {

    public static Path getUserDataDir() {
        Path userDataDir = Paths.get(PropertyResolver.getBrowserProfileDir());
        // Create preferences file
        Path profileFile = userDataDir.resolve("Default").resolve("Preferences");
        profileFile.getParent().toFile().mkdirs();
        FileOperation.writeStringToFile(getPreferenceContent(), profileFile.toFile());

        return userDataDir;
    }

    private static String getPreferenceContent() {
        ObjectMapper objectMapper = ObjectMapperSingleton.mapper();
        ObjectNode profileNode = objectMapper.createObjectNode();
        ObjectNode download = objectMapper.createObjectNode().put("prompt_for_download", false)
                .put("open_pdf_in_system_reader", PropertyResolver.isOpenPDFInSystemReader());
        ObjectNode plugins = objectMapper.createObjectNode().put("always_open_pdf_externally", true);
        profileNode.set("download", download);
        profileNode.set("plugins", plugins);
        return profileNode.toPrettyString();
    }
}
