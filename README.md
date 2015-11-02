# UMS-Plugin: Auto-delete after play
This plugin for Universal Media Server (UMS) will automatically delete a file after having been played.

##Using the plugin
1. Download it from [SourceForge](https://sourceforge.net/projects/ums-mlx/files/ums-plugins/AutoDeleteFileAfterPlay/)
2. Place it in the plugins folder of your Univeral Media Server
3. Start Univeral Media Server
4. In the *Plugin Management* tab click on *Auto-delete after play*
5. Configure the properties

All done :sunglasses:

Following properties can be set in the plugin configuration:<br>
![AutoDeleteFileAfterPlay configuration](http://i.imgur.com/kW6RU3w.png)

If you use UMS in headless mode, you can configure the settings in the properties file located in<br>
`<profile_directory>\plugins\AutoDeleteFileAfterPlay\configuration.properties`<br>
(create it if it doesn't exist).

The default configuration contains following:

    percentPlayedRequired=80
    autoDeleteFolderPaths=
    moveToRecycleBin=true
    isDeleteVideo=true
    isDeleteAudio=false
    isDeleteImage=false

## Compiling the plugin
The plugin has a maven dependency onto UMS; as it isn't hosted in a maven repository, it has to be installed in the local repository by doing following:

    git clone https://github.com/UniversalMediaServer/UniversalMediaServer.git
    cd UniversalMediaServer
    mvn com.savage7.maven.plugins:maven-external-dependency-plugin:resolve-external
    mvn com.savage7.maven.plugins:maven-external-dependency-plugin:install-external
    mvn install
    
Then you can build the plugin:

    git clone https://github.com/taconaut/UMS-Plugin-AutoDeleteFileAfterPlay.git
    cd UMS-Plugin-AutoDeleteFileAfterPlay
    mvn package
    
Make sure the version of UMS you've just installed is correctly referenced in the pom.xml of the plugin:

		<dependency>
			<groupId>net.pms</groupId>
			<artifactId>ums</artifactId>
			<version>5.2.4-SNAPSHOT</version>
		</dependency>
