package hopper.url_loader.downloader;

import java.nio.file.Path;

public class DownloadResult {
	private final String url;
	private final Path outputFile;
	private final boolean success;
	private final long durationMs;
	private final Throwable exception;

	public DownloadResult(String url, Path outputFile, boolean success, long durationMs, Throwable exception) {
		this.url = url;
		this.outputFile = outputFile;
		this.success = success;
		this.durationMs = durationMs;
		this.exception = exception;
	}

	public String getUrl() {
		return url;
	}

	public Path getOutputFile() {
		return outputFile;
	}

	public boolean isSuccess() {
		return success;
	}

	public long getDurationMs() {
		return durationMs;
	}

	public Throwable getException() {
		return exception;
	}
}
