package hopper.url_loader.config;

import java.nio.file.Files;
import java.nio.file.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import com.fasterxml.jackson.databind.ObjectMapper;


public class FileConfigProvider implements ConfigProvider {
	
	private static final Logger log = LoggerFactory.getLogger(FileConfigProvider.class);

    private final ConfigValidator validator;

    public FileConfigProvider(ConfigValidator validator) {
        this.validator = validator;
    }

    @Override
    public Config getConfig(String[] args) throws ConfigException {
        Path path = getConfigFilePath(args);
        log.info("Loading configuration from {}", path);
        
        Config config = loadConfigFromFile(path);
        
        validator.validate(config);
        
        return config;
    }

    private Path getConfigFilePath(String[] args) throws ConfigException {
        if (args.length < 1) { 
        	log.error("Usage: java -jar app.jar <config.json>");
        	throw new ConfigException("Usage: java -jar url_loader-0.0.1-SNAPSHOT.jar <config.json>");
        }
        
        Path path = Path.of(args[0]);
        if (!Files.exists(path) || !Files.isRegularFile(path) || !Files.isReadable(path)) {
        	log.error("File does not exist, is a directory, or is not readable: {}", path);
            throw new ConfigException("Cannot read the file: " + path);
        }
        return path;
    }

    private Config loadConfigFromFile(Path path) throws ConfigException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(path.toFile(), Config.class);
        } catch (Exception e) {
        	log.error("Failed to load configuration: {}", e.getMessage(), e);
            throw new ConfigException("Failed to load config: " + e.getMessage(), e);
        }
    }
}

