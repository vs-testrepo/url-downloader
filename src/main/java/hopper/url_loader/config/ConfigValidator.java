package hopper.url_loader.config;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import org.apache.commons.validator.routines.UrlValidator;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

public class ConfigValidator {

	private static final Logger log = LoggerFactory.getLogger(ConfigValidator.class);
	private static final UrlValidator urlValidator = new UrlValidator(new String[] { "http", "https" });

	public void validate(Config config) throws ConfigException {
		validateFields(config);
		removeDuplicateUrls(config);
		removeDuplicateFileNames(config); 
		validateUrls(config);
		validateOutputDir(config.getOutputDir());
	}

	private void validateFields(Config config) throws ConfigException {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		Validator validator = factory.getValidator();

		Set<ConstraintViolation<Config>> violations = validator.validate(config);
		if (!violations.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			for (ConstraintViolation<Config> v : violations) {
				sb.append(v.getPropertyPath()).append(": ").append(v.getMessage()).append("\n");
			}
			log.error("Invalid configuration: {}", sb);
			throw new ConfigException("Config validation failed:\n" + sb);
		}
	}

	private void removeDuplicateUrls(Config config) {
		Set<String> urlSet = new HashSet<>();
		List<String> uniqueUrls = new ArrayList<>();
		for (String url : config.getUrls()) {
			if (urlSet.add(url)) {
				uniqueUrls.add(url);
			} else {
				log.warn("Duplicate URL removed: {}", url);
			}
		}
		if (urlSet.size() < config.getUrls().size()) {
			log.info("Removed {} duplicate URLs, proceeding with {} unique URLs", 
					config.getUrls().size() - urlSet.size(), urlSet.size());
			config.setUrls(uniqueUrls);
		}
	}

	private void removeDuplicateFileNames(Config config) {
		Set<String> fileNames = new HashSet<>();
		List<String> uniqueUrls = new ArrayList<>();
		for (String url : config.getUrls()) {
			String fileName = Paths.get(URI.create(url).getPath()).getFileName() != null 
				? Paths.get(URI.create(url).getPath()).getFileName().toString() 
				: "file_" + System.nanoTime();
			if (fileNames.add(fileName)) {
				uniqueUrls.add(url);
			} else {
				log.warn("URL with duplicate file name removed: {} (file: {})", url, fileName);
			}
		}
		if (fileNames.size() < config.getUrls().size()) {
			log.info("Removed {} URLs with duplicate file names, proceeding with {} unique URLs", 
					config.getUrls().size() - uniqueUrls.size(), uniqueUrls.size());
			config.setUrls(uniqueUrls);
		}
	}

	private void validateUrls(Config config) throws ConfigException {
		for (String url : config.getUrls()) {
			if (!urlValidator.isValid(url)) {
				log.error("Invalid URL: {}", url);
				throw new ConfigException("Invalid URL: " + url);
			}
		}
	}

	private void validateOutputDir(String outputDir) throws ConfigException {
		Path path = Paths.get(outputDir);
		try {
			if (Files.exists(path)) {
				if (!Files.isDirectory(path)) {
					log.error("Output path {} is not a directory", path);
					throw new ConfigException("Output path is not a directory: " + path);
				}
				if (!Files.list(path).findAny().isEmpty()) {
					log.error("Output directory {} is not empty", path);
					throw new ConfigException("Output directory is not empty: " + path);
				}
			}
			if (Files.exists(path) && !Files.isWritable(path)) {
				log.error("Output directory is not writable: {}", path);
				throw new ConfigException("Output directory is not writable: " + path);
			}
		} catch (Exception e) {
			log.error("Invalid output directory {}: {}", path, e.getMessage());
			throw new ConfigException("Invalid output directory: " + path, e);
		}
	}
}
