package github.com.taconaut.plugin.autodeleteafterplay;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.ResourceBundle;

import javax.swing.JComponent;

import org.apache.commons.lang.StringUtils;
import org.fest.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jna.platform.FileUtils;

import net.pms.dlna.DLNAMediaInfo;
import net.pms.dlna.DLNAResource;
import net.pms.dlna.RealFile;
import net.pms.external.StartStopListener;

/**
 * Universal media server plugin used to automatically delete files when they've finished playing.
 */
public class AutoDeleteFileAfterPlayPlugin implements StartStopListener {
	private static final Logger LOGGER = LoggerFactory.getLogger(AutoDeleteFileAfterPlayPlugin.class);

	private static FileUtils fileUtils = FileUtils.getInstance();

	/** Resource file used by the plugin **/
	protected static final ResourceBundle MESSAGES = ResourceBundle.getBundle("autodeleteafterplay-i18n.messages");

	private static final PluginConfiguration CONFIGURATION;

	static {
		CONFIGURATION = new PluginConfiguration();
		try {
			CONFIGURATION.load();
		} catch (IOException e) {
			LOGGER.error("Failed to load global configuration", e);
		}
	}

	private static final int MAX_RETRY_DELETE = 10;
	private static final int RETRY_DELETE_INTERVAL_MILLIS = 1000;

	/** Cache used to keep track of files being played. */
	private final Queue<QueueItem> playCache = new LinkedList<QueueItem>();

	private ConfigurationComponent configurationComponent;

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.pms.external.ExternalListener#config()
	 */
	public JComponent config() {
		if (configurationComponent == null) {
			// Lazy-initialize the configuration panel
			configurationComponent = new ConfigurationComponent(CONFIGURATION, fileUtils.hasTrash());
		}
		return configurationComponent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.pms.external.ExternalListener#name()
	 */
	public String name() {
		return MESSAGES.getString("PluginName");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.pms.external.ExternalListener#shutdown()
	 */
	public void shutdown() {
		playCache.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.pms.external.StartStopListener#donePlaying(net.pms.dlna.DLNAMediaInfo , net.pms.dlna.DLNAResource)
	 */
	public synchronized void donePlaying(DLNAMediaInfo media, DLNAResource resource) {
		if ((resource.getMedia().isVideo() && !CONFIGURATION.isDeleteVideo()) ||
				(resource.getMedia().isAudio() && !CONFIGURATION.isDeleteAudio()) ||
				(resource.getMedia().isImage() && !CONFIGURATION.isDeleteImage())) {
			// Only handle plays for configured file types
			return;
		}

		LOGGER.debug("Done playing " + resource.getName());

		if (!(resource instanceof RealFile)) {
			LOGGER.debug("The DLNAResource isn't a real file and can't be deleted");
			return;
		}

		RealFile realFile = (RealFile) resource;

		int playLengthSec = 0;
		while (true) {
			QueueItem item = playCache.poll();
			if (item == null) {
				break;
			}

			if (item.getResourceId().equals(resource.getInternalId())) {
				if (resource instanceof RealFile) {
					playLengthSec = (int) (new Date().getTime() - item.getStartDate().getTime()) / 1000;
					break;
				}
			}
		}

		if (playLengthSec > 0) {
			String filePath = realFile.getFile().getAbsolutePath();
			int fullLengthSec = (int) media.getDurationInSeconds();
			int minPlayDeleteLengthSec = (int) (fullLengthSec * ((double) CONFIGURATION.getPercentPlayedRequired() / 100));
			LOGGER.debug(String.format("Stopped playing file '%s' after %s seconds. Min play length for deleting is %s seconds (%s%% of %s seconds)",
					filePath, playLengthSec, minPlayDeleteLengthSec, CONFIGURATION.getPercentPlayedRequired(), fullLengthSec));

			// Check if the file has been played long enough to delete it
			if (playLengthSec > minPlayDeleteLengthSec) {
				// Delete file if
				// 1) it is contained in a folder which auto deletes files
				// 2) no folders have been specified
				boolean deleteFile = false;
				String[] autoDeleteFolderPaths = CONFIGURATION.getAutoDeleteFolderPaths();
				if (autoDeleteFolderPaths.length > 0) {
					for (String autoDeleteFolderPath : autoDeleteFolderPaths) {
						if (filePath.startsWith(autoDeleteFolderPath.trim())) {
							deleteFile = true;
							break;
						}
					}
					if (!deleteFile) {
						LOGGER.debug(String.format("The file '%s' won't be deleted because it is not part of the defined folders (%s)",
								filePath, StringUtils.join(autoDeleteFolderPaths, ";")));
					}
				} else {
					deleteFile = true;
				}

				if (deleteFile) {
					boolean deleteSuccess = false;
					IOException lastDeleteException = null;
					for (int nbRetries = 0; nbRetries < MAX_RETRY_DELETE; nbRetries++) {
						try {
							if (CONFIGURATION.isMoveToRecycleBin() && fileUtils.hasTrash()) {
								fileUtils.moveToTrash(Arrays.array(new File(filePath)));
							} else {
								new File(filePath).delete();
							}
							deleteSuccess = true;
							break;
						} catch (IOException ex) {
							lastDeleteException = ex;

							try {
								Thread.sleep(RETRY_DELETE_INTERVAL_MILLIS);
							} catch (InterruptedException e) {
								LOGGER.error("Sleep aborted", e);
							}
						}
					}

					if (deleteSuccess) {
						if (CONFIGURATION.isMoveToRecycleBin()) {
							LOGGER.info(String.format("Moved file '%s' to the recycle bin after having played it for %s seconds. Minimum play length for deleting is %s seconds (%s%% of %s seconds)",
									filePath, playLengthSec, minPlayDeleteLengthSec, CONFIGURATION.getPercentPlayedRequired(), fullLengthSec));
						} else {
							LOGGER.info(String.format("Permanently deleted file '%s' after having played it for %s seconds. Minimum play length for deleting is %s seconds (%s%% of %s seconds)",
									filePath, playLengthSec, minPlayDeleteLengthSec, CONFIGURATION.getPercentPlayedRequired(), fullLengthSec));
						}
					} else {
						if (CONFIGURATION.isMoveToRecycleBin()) {
							LOGGER.warn(String.format("Failed to move file '%s' to the recycle bin after %s retries", filePath, MAX_RETRY_DELETE), lastDeleteException);
						} else {
							LOGGER.warn(String.format("Failed to permanently delete file '%s' after %s retries", filePath, MAX_RETRY_DELETE), lastDeleteException);
						}
					}
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.pms.external.StartStopListener#nowPlaying(net.pms.dlna.DLNAMediaInfo, net.pms.dlna.DLNAResource)
	 */
	public synchronized void nowPlaying(DLNAMediaInfo media, DLNAResource resource) {
		if ((resource.getMedia().isVideo() && !CONFIGURATION.isDeleteVideo()) ||
				(resource.getMedia().isAudio() && !CONFIGURATION.isDeleteAudio()) ||
				(resource.getMedia().isImage() && !CONFIGURATION.isDeleteImage())) {
			// Only handle plays for configured file types
			return;
		}

		LOGGER.debug(String.format("Started playing %s", resource.getName()));
		playCache.add(new QueueItem(resource.getInternalId(), new Date()));
	}

	/**
	 * Private class used internally to keep track of played files.
	 */
	private class QueueItem {
		private String resourceId;
		private Date startDate;

		/**
		 * The Constructor.
		 *
		 * @param resourceId the resource id
		 * @param startDate the start date
		 */
		public QueueItem(String resourceId, Date startDate) {
			this.resourceId = resourceId;
			this.startDate = startDate;
		}

		/**
		 * Gets the resource id.
		 *
		 * @return the resource id
		 */
		public String getResourceId() {
			return resourceId;
		}

		/**
		 * Gets the start date.
		 *
		 * @return the start date
		 */
		public Date getStartDate() {
			return startDate;
		}
	}
}
