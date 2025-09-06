package hopper.url_loader.config;

public interface ConfigProvider {
	
	Config getConfig(String[] args) throws ConfigException;
	
}
