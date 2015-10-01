package github.com.taconaut.plugin.autodeleteafterplay;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Properties;

import net.pms.PMS;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Holds the configuration of the plugin.
 */
public class PluginConfiguration {
	private static final Logger LOGGER = LoggerFactory.getLogger(PluginConfiguration.class);

	private final Properties properties = new Properties();
	private final String configurationFilePath = Paths.get(PMS.getConfiguration().getProfileDirectory(), "plugins", "AutoDeleteFileAfterPlay", "configuration.properties").toString();

	private static final String KEY_PERCENT_PLAY_REQUIRED = "percentPlayedRequired";
	private static final String KEY_AUTO_DELETE_FOLDER_PATHS = "autoDeleteFolderPaths";
	private static final String KEY_MOVE_TO_RECYCLEBIN = "moveToRecycleBin";

	/**
	 * Gets the configuration file path.
	 *
	 * @return the configuration file path
	 */
	public String getConfigurationFilePath() {
		return configurationFilePath;
	}

	/**
	 * Saves the properties to the specified file path.<br>
	 * Sub-directories will be created automatically if needed
	 *
	 * @param propertiesFilePath the save file path
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void save() throws IOException {
		LOGGER.debug(String.format("Saving configuration to '%s'", getConfigurationFilePath()));

		// make sure the save directory exists
		File saveFile = new File(getConfigurationFilePath());
		File saveDir = new File(saveFile.getParent());
		if (!saveDir.isDirectory()) {
			LOGGER.debug(String.format("The directory '%s' to store the plugin configuration doesn't exist. Create it!", saveDir));
			saveDir.mkdirs();
			LOGGER.info(String.format("Created directory '%s' to store the plugin configuration.", saveDir));
		}

		FileOutputStream configStream = new FileOutputStream(getConfigurationFilePath());
		properties.store(configStream, "");

		LOGGER.debug(String.format("Saved configuration to '%s'", getConfigurationFilePath()));
	}

	/**
	 * Saves the properties to the specified file path.
	 *
	 * @param propertiesFilePath the file to load the properties from
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void load() throws IOException {
		if (new File(getConfigurationFilePath()).exists()) {
			LOGGER.debug(String.format("Restoring configuration from '%s'", getConfigurationFilePath()));
			FileInputStream configStream = new FileInputStream(getConfigurationFilePath());
			properties.load(configStream);
			LOGGER.info(String.format("Loaded configuration from '%s'", getConfigurationFilePath()));
		} else {
			LOGGER.info(String.format("The plugin configuration could not be loaded. No configuration file found at '%s'", getConfigurationFilePath()));
		}
	}

	/**
	 * Sets the value for the given key.
	 *
	 * @param key the key
	 * @param value the value
	 */
	protected void setValue(String key, Object value) {
		if (key != null && value != null) {
			properties.put(key, value.toString());
		}
	}

	/**
	 * Gets the value of type T for the given key. If no value can be found, the default value will be returned
	 *
	 * @param <T> the generic type. Supported types are String, Integer, Boolean, Enum
	 * @param key the key
	 * @param defaultValue the default value
	 * @return the value
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected <T> T getValue(String key, T defaultValue) {
		Object value = properties.get(key);
		if (value != null && defaultValue != null) {
			if (defaultValue instanceof Integer) {
				return (T) (Integer) Integer.parseInt(value.toString());
			} else if (defaultValue instanceof Boolean) {
				return (T) (Boolean) Boolean.parseBoolean(value.toString());
			} else if (defaultValue instanceof Enum) {
				return (T) Enum.valueOf((Class) defaultValue.getClass(), value.toString());
			} else if (defaultValue.getClass().isAssignableFrom(value.getClass())) {
				return (T) value;
			}
		}
		return defaultValue;
	}

	/**
	 * Gets the percent played required before a file gets automatically deleted.
	 *
	 * @return the percent played required
	 */
	public int getPercentPlayedRequired() {
		return getValue(KEY_PERCENT_PLAY_REQUIRED, 80);
	}

	/**
	 * Sets the percent played required before a file gets automatically deleted.
	 *
	 * @param percentPlayedRequired the percent played required
	 */
	public void setPercentPlayedRequired(int percentPlayedRequired) {
		setValue(KEY_PERCENT_PLAY_REQUIRED, percentPlayedRequired);
	}

	/**
	 * Gets an array of folders in which files get automatically deleted.
	 *
	 * @return the auto delete folder paths
	 */
	public String[] getAutoDeleteFolderPaths() {
		String autoDeleteFolderPathsString = getValue(KEY_AUTO_DELETE_FOLDER_PATHS, "");
		return autoDeleteFolderPathsString.split(";");
	}

	/**
	 * Sets an array of folders in which files get automatically deleted.
	 *
	 * @param autoDeleteFolderPaths the auto delete folder paths
	 */
	public void setAutoDeleteFolderPaths(String[] autoDeleteFolderPaths) {
		setValue(KEY_AUTO_DELETE_FOLDER_PATHS, StringUtils.join(autoDeleteFolderPaths, ";"));
	}

	/**
	 * Gets a value indicating if the file should be moved to the recycle bin.
	 *
	 * return true, if a deleted file should be moved to the recycle bin
	 */
	public boolean isMoveToRecycleBin() {
		return getValue(KEY_MOVE_TO_RECYCLEBIN, true);
	}

	/**
	 * Sets a value indicating if the file should be moved to the recycle bin.
	 *
	 * @param moveToRecycleBin true, if a deleted file should be moved to the recycle bin
	 */
	public void setMoveToRecycleBin(boolean moveToRecycleBin) {
		setValue(KEY_MOVE_TO_RECYCLEBIN, moveToRecycleBin);
	}
}
