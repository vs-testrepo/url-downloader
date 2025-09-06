package hopper.url_loader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import hopper.url_loader.config.Config;
import hopper.url_loader.config.ConfigException;
import hopper.url_loader.config.ConfigProvider;
import hopper.url_loader.config.ConfigValidator;
import hopper.url_loader.config.FileConfigProvider;
import hopper.url_loader.downloader.DownloadManager;
import hopper.url_loader.downloader.DownloadResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UrlLoaderAppl {
	private static final Logger log = LoggerFactory.getLogger(UrlLoaderAppl.class);

	public static void main(String[] args) {
		log.info("Application started");
		ConfigValidator validator = new ConfigValidator();
		ConfigProvider provider = new FileConfigProvider(validator);

		Config config = null; 

		try {
			config = provider.getConfig(args);
			validator.validate(config); 
			createOutputDirIfNeeded(config.getOutputDir()); 
			log.info("Config loaded and validated successfully: URLs - {}, max {} concurrent tasks, max download time per URL {}, to directory {}", 
					config.getUrls().size(), config.getMaxConcurrent(), config.getPerUrlTimeoutMs(), config.getOutputDir());
		} catch (ConfigException e) {
			log.error("Failed to load/validate config. Exception: {}", e.getMessage());
			System.exit(1);
		}

		DownloadManager manager = new DownloadManager(config.getMaxConcurrent());
		long startWallClock = System.currentTimeMillis();

		List<DownloadResult> results = new ArrayList<>();
		try {
			results = manager.downloadAll(config.getUrls(), Path.of(config.getOutputDir()),
					config.getPerUrlTimeoutMs());
		} catch (InterruptedException e) {
			log.error("Download process was interrupted");
			printResults(results); 
			return;
		}
		long totalWallClock = System.currentTimeMillis() - startWallClock;

		printResults(results);
		log.info("Total wall-clock time: {} ms", totalWallClock);
	}

	private static void createOutputDirIfNeeded(String outputDir) throws ConfigException {
		Path path = Paths.get(outputDir);
		try {
			if (!Files.exists(path)) {
				Files.createDirectories(path);
			}
		} catch (Exception e) {
			log.error("Failed to create output directory {}: {}", path, e.getMessage());
			throw new ConfigException("Failed to create output directory: " + path, e);
		}
	}

	private static void printResults(List<DownloadResult> results) {
		log.info("***************** Results ********************");
		for (DownloadResult r : results) {
			if (r.isSuccess()) {
				log.info("Downloaded {} --> {} in {} ms", r.getUrl(), r.getOutputFile(), r.getDurationMs());
			} else {
				log.error("Failed to download {} after {} ms. Exception: {}", r.getUrl(), r.getDurationMs(), r.getException());
			}
		}
	}
}
