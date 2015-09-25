# UMS-Plugin: Auto-delete after play
This plugin for Universal Media Server will automatically delete a file after having played it.

##Using the plugin
1. Download it from [SourceForge](https://sourceforge.net/projects/ums-mlx/files/ums-plugins/AutoDeleteFileAfterPlay/)
2. Place it in the plugins folder of your Univeral Media Server
3. Start Univeral Media Server
4. In the *Plugin Management* tab click on *Auto-delete after play*
5. Configure the options

All done :sunglasses:

If you use UMS in headless mode, you can configure the settings in the properties file located in<br>
`<profile_directory>\plugins\AutoDeleteFileAfterPlay\configuration.properties`.<br>
The default configuration contains following:

    percentPlayedRequired=80
    autoDeleteFolderPaths=
    moveToRecycleBin=true

## Compiling the plugin
The plugin has a maven dependency onto ums; as it isn't hosted in a maven repository, it has to be installed in the local maven repository by dowing following:

    git clone https://github.com/UniversalMediaServer/UniversalMediaServer.git
    cd UniversalMediaServer
    mvn com.savage7.maven.plugins:maven-external-dependency-plugin:resolve-external
    mvn com.savage7.maven.plugins:maven-external-dependency-plugin:install-external
    mvn install
    
    cd ..
    cd UMS-Plugin-AutoDeleteFileAfterPlay
    git clone https://github.com/taconaut/UMS-Plugin-AutoDeleteFileAfterPlay.git
    cd AutoDeleteFileAfterPlay
    mvn package
    
Make sure the current version of ums is correctly referenced in the pom.xml of the plugin:

		<dependency>
			<groupId>net.pms</groupId>
			<artifactId>ums</artifactId>
			<version>5.2.3-SNAPSHOT</version>
		</dependency>
