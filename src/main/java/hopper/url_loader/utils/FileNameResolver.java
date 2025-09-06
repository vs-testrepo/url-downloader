package hopper.url_loader.utils;

import java.net.URI;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class FileNameResolver {
	
	 private static final Map<String, String> EXTENSIONS = new HashMap<>();
	    static {
	        EXTENSIONS.put("text/plain", ".txt");
	        EXTENSIONS.put("text/html", ".html");
	        EXTENSIONS.put("text/css", ".css");
	        EXTENSIONS.put("text/javascript", ".js");
	        EXTENSIONS.put("application/javascript", ".js");
	        EXTENSIONS.put("application/json", ".json");
	        EXTENSIONS.put("application/pdf", ".pdf");
	        EXTENSIONS.put("application/zip", ".zip");
	        EXTENSIONS.put("application/gzip", ".gz");
	        EXTENSIONS.put("image/jpeg", ".jpg");
	        EXTENSIONS.put("image/png", ".png");
	        EXTENSIONS.put("image/gif", ".gif");
	        EXTENSIONS.put("image/webp", ".webp");
	        EXTENSIONS.put("image/svg+xml", ".svg");
	        EXTENSIONS.put("audio/mpeg", ".mp3");
	        EXTENSIONS.put("audio/wav", ".wav");
	        EXTENSIONS.put("audio/ogg", ".ogg");
	        EXTENSIONS.put("video/mp4", ".mp4");
	        EXTENSIONS.put("video/webm", ".webm");
	    }
	    
	    public static String  getInitialFileNameFromURL(String url) {
			Path path = Paths.get(URI.create(url).getPath());
			return path.getFileName() != null ? path.getFileName().toString() : "file_" + System.nanoTime();
		}
	    
	    public static String getFinalFileNameWithExtension(String fileName, HttpResponse<Path> response) {
			String contentType = response.headers().firstValue("Content-Type").orElse("application/octet-stream");
			String mimeType = contentType.toLowerCase().split(";")[0].trim();
			String extension = EXTENSIONS.getOrDefault(mimeType, "");
			return fileName.endsWith(extension) ? fileName : fileName + extension;
		}

}
