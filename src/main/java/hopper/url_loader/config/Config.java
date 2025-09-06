package hopper.url_loader.config;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public class Config {
	
	
	@NotEmpty(message = "URLs list must not be empty")
	@JsonProperty("urls")
	private List<String> urls;
	
	@NotBlank(message = "Output directory must not be blank")
	@JsonProperty("outputDir")
	private String outputDir;
	
	
	 @Min(value = 1, message = "maxConcurrent must be > 0")
	@JsonProperty("maxConcurrent")
	private int maxConcurrent;	
		
	 @Min(value = 1, message = "perUrlTimeoutMs must be > 0")
	@JsonProperty("perUrlTimeoutMs")
	private long perUrlTimeoutMs;
	

	


	
	
	public List<String> getUrls() {
		return urls;
	}

	public void setUrls(List<String> urls) {
		this.urls = urls;
	}

	public long getPerUrlTimeoutMs() {
		return perUrlTimeoutMs;
	}

	public void setPerUrlTimeoutMs(long perUrlTimeoutMsl) {
		this.perUrlTimeoutMs = perUrlTimeoutMsl;
	}

	public String getOutputDir() {
		return outputDir;
	}

	public void setOutputDir(String outputDirPath) {
		this.outputDir = outputDirPath;
	}

	public int getMaxConcurrent() {
		return maxConcurrent;
	}

	public void setMaxConcurrent(int maxConcurrent) {
		this.maxConcurrent = maxConcurrent;
	}

}
