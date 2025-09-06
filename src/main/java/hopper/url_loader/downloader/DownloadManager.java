package hopper.url_loader.downloader;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DownloadManager {
	
	private final int maxConcurrent;
	private static final Logger log = LoggerFactory.getLogger(DownloadManager.class);
	
    public DownloadManager(int maxConcurrent) {
        this.maxConcurrent = maxConcurrent;
    }

    public List<DownloadResult> downloadAll(List<String> urls, Path outputDir, long perUrlTimeoutMs)
            throws InterruptedException {

        log.info("Starting download of {} URLs with max {} concurrent tasks", urls.size(), maxConcurrent);

        ExecutorService executor = Executors.newFixedThreadPool(maxConcurrent);
        CompletionService<DownloadResult> completionService = new ExecutorCompletionService<>(executor);

        try {
        	for (String url : urls) { 
                completionService.submit(new DownloadTask(url, outputDir, perUrlTimeoutMs));
            }

            List<DownloadResult> results = new ArrayList<>();
            int received = 0;

            while (received < urls.size()) {
                try {
                    Future<DownloadResult> future = completionService.take();
                    DownloadResult result = future.get(); 
                    results.add(result);

                } catch (ExecutionException e) {
                	 log.error("Download task failed unexpectedly: {}", e.getCause());
                     results.add(new DownloadResult("unknown", null, false, 0, e.getCause()));
                }
                received++;
            }

            return results;

        } finally {
            executor.shutdown();
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        }
    }

}
