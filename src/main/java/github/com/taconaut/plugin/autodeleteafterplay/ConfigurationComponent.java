package github.com.taconaut.plugin.autodeleteafterplay;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JSpinner;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.pms.newgui.components.CustomJCheckBox;
import net.pms.newgui.components.CustomJTextField;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Swing component used to configure the plugin properties.
 */
public class ConfigurationComponent extends JComponent {
	private static final long serialVersionUID = -404029004613557444L;
	private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationComponent.class);
	
	private PluginConfiguration configuration;
	
	private JSpinner sPercentPlayedRequired;
	private CustomJTextField tfAutoDeleteFolderPaths;
	private CustomJCheckBox cbMoveToRecycleBin;
	
	/**
	 * The Constructor.
	 *
	 * @param configuration the plugin configuration
	 */
	public ConfigurationComponent(PluginConfiguration configuration) {
		this.configuration = configuration;
		
		initialize();
		build();
	}

	/**
	 * Initializes the required components.
	 */
	private void initialize() {
		setLayout(new GridLayout());
		
		// Initialize PercentPlayedRequired
		sPercentPlayedRequired = new JSpinner(new SpinnerIntModel(configuration.getPercentPlayedRequired(), 0, 100, 5));
		sPercentPlayedRequired.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				updateAndSaveMinPlayPercent();
			}
		});
		sPercentPlayedRequired.setToolTipText(AutoDeleteFileAfterPlayPlugin.MESSAGES.getString("ConfigurationComponent.sPercentPlayedRequired.ToolTip"));

		// Initialize AutoDeleteFolderPaths
		tfAutoDeleteFolderPaths = new CustomJTextField(StringUtils.join(configuration.getAutoDeleteFolderPaths(), ";"));
		tfAutoDeleteFolderPaths.getDocument().addDocumentListener(new DocumentListener() {
			
			@Override
			public void removeUpdate(DocumentEvent e) {
				updateAndSaveAutoDeleteFolderPaths();
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				updateAndSaveAutoDeleteFolderPaths();
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				updateAndSaveAutoDeleteFolderPaths();				
			}
		});
		tfAutoDeleteFolderPaths.setToolTipText(AutoDeleteFileAfterPlayPlugin.MESSAGES.getString("ConfigurationComponent.tfAutoDeleteFolderPaths.ToolTip"));

		// Initialize MoveToRecycleBin
		cbMoveToRecycleBin = new CustomJCheckBox(AutoDeleteFileAfterPlayPlugin.MESSAGES.getString("ConfigurationComponent.cbMoveToRecycleBin"));
		cbMoveToRecycleBin.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				configuration.setMoveToRecycleBin(cbMoveToRecycleBin.isSelected());
				trySaveConfiguration();
			}
		});
		cbMoveToRecycleBin.setToolTipText(AutoDeleteFileAfterPlayPlugin.MESSAGES.getString("ConfigurationComponent.cbMoveToRecycleBin.ToolTip"));
		cbMoveToRecycleBin.setSelected(configuration.isMoveToRecycleBin());
	}

	/**
	 * Builds the component.
	 */
	private void build() {
		// Set basic layout
		FormLayout layout = new FormLayout("5px, p, 5px, f:300:g, 5px, p, 5px", //columns
				"5px, p, 5px, p, 5px, p, 5px"); //rows
		PanelBuilder builder = new PanelBuilder(layout);
		builder.opaque(true);

		CellConstraints cc = new CellConstraints();
		
		builder.addLabel(AutoDeleteFileAfterPlayPlugin.MESSAGES.getString("ConfigurationComponent.lPercentPlayedRequired"), cc.xy(2, 2, CellConstraints.RIGHT, CellConstraints.DEFAULT));
		builder.add(sPercentPlayedRequired, cc.xy(4, 2));
		builder.addLabel("%", cc.xy(6, 2));

		builder.addLabel(AutoDeleteFileAfterPlayPlugin.MESSAGES.getString("ConfigurationComponent.lAutoDeleteFolderPaths"), cc.xy(2, 4, CellConstraints.RIGHT, CellConstraints.DEFAULT));
		builder.add(tfAutoDeleteFolderPaths, cc.xyw(4, 4, 3));
		
		builder.add(cbMoveToRecycleBin, cc.xyw(2, 6, 5));
		
		add(builder.getPanel());
	}
	
	/**
	 * Updates and saves the auto delete folder paths.
	 */
	private void updateAndSaveAutoDeleteFolderPaths(){
		String[] autoDeleteFolderPaths = tfAutoDeleteFolderPaths.getText().split(";");
		configuration.setAutoDeleteFolderPaths(autoDeleteFolderPaths);
		trySaveConfiguration();
	}
	
	/**
	 * Updates and saves minimum play percent.
	 */
	private void updateAndSaveMinPlayPercent(){
		int percentPlayedRequired = (int) sPercentPlayedRequired.getValue();
		configuration.setPercentPlayedRequired(percentPlayedRequired);
		trySaveConfiguration();
	}
	
	/**
	 * Tries to save the configuration.
	 *
	 * @return true, if the configuration could be saved; otherwise false
	 */
	private boolean trySaveConfiguration() {
		try {
			configuration.save();
			return true;
		} catch (IOException ex) {
			LOGGER.error("Failed to save configuration", ex);
			return false;
		}
	}
}
