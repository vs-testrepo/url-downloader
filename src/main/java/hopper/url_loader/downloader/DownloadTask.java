package hopper.url_loader.downloader;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hopper.url_loader.utils.FileNameResolver;

public class DownloadTask implements Callable<DownloadResult> {

	private static final Logger log = LoggerFactory.getLogger(DownloadTask.class);
	private static final HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL)
			.build();

	private final String url;
	private final Path outputDir;
	private final long timeoutMs;

	public DownloadTask(String url, Path outputDir, long timeoutMs) {
		this.url = url;
		this.outputDir = outputDir;
		this.timeoutMs = timeoutMs;
	}

	@Override
	public DownloadResult call() {
		long startTime = System.currentTimeMillis();
		try {
			String initialFileName =FileNameResolver.getInitialFileNameFromURL(url);
			HttpResponse<Path> response = performDownload(initialFileName);
			String finalFileName = FileNameResolver.getFinalFileNameWithExtension(initialFileName, response);
			Path outputFile = saveFile(response.body(), finalFileName);
			long duration = System.currentTimeMillis() - startTime;
			log.info("Downloaded {} to {} in {} ms", url, outputFile, duration);
			return new DownloadResult(url, outputFile, true, duration, null);
		} catch (Exception e) {
			long duration = System.currentTimeMillis() - startTime;
			log.error("Failed to download {} in {} ms. Error: {}", url, duration, e.getMessage());
			return new DownloadResult(url, null, false, duration, e);
		}
	}

	

	private HttpResponse<Path> performDownload(String initialFileName) throws Exception {
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).timeout(Duration.ofMillis(timeoutMs)).GET()
				.build();
		Path tempFile = outputDir.resolve("temp_" + System.nanoTime());
		HttpResponse<Path> response = client.send(request, HttpResponse.BodyHandlers.ofFile(tempFile));
		if (response.statusCode() >= 400) {
			Files.deleteIfExists(tempFile);
			throw new Exception("HTTP error: " + response.statusCode());
		}
		return response;
	}

	

	private Path saveFile(Path tempFile, String finalFileName) throws Exception {
		Path outputFile = outputDir.resolve(finalFileName);
		Files.move(tempFile, outputFile);
		return outputFile;
	}


}
