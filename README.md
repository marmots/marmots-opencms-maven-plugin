# marmots-opencms-maven-plugin
A maven plugin that helps with Opencms module development

# Publish goal
publishes modified resources to opencms via JLAN server

configuration properties
```xml
<url>smb://Admin:admin@localhost:1446/OPENCMS/</url>
<module>org.marmots.opencms.samplesite</module>
```

# Package goal
generates opencms module manifest.xml and packages it together with all resources, classes and libs.

configuration properties
```xml
<module>org.marmots.opencms.samplesite</module>
```

# Usage
There are detailed instructions in the wiki, but to start quick:

1. create a module in opencms

2. add content types, formatter, whatever you need

3. publish and export it

4. create a new maven project (packaging jar)

5. create /src/main/opencms folder and extract there the packaged module

6. modify pom.xml to enable the plugin

```xml
...
	<dependencies>
		<dependency>
			<groupId>org.marmots.opencms</groupId>
			<artifactId>marmots-opencms-maven-plugin</artifactId>
			<version>0.0.1</version>
			<type>maven-plugin</type>
		</dependency>
	</dependencies>
...

<build>
		<plugins>
			...
			<!-- only required to export lib -->
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-dependency-plugin</artifactId>
				<version>2.10</version>
				<executions>
					<execution>
						<id>copy-dependencies</id>
						<phase>package</phase>
						<goals>
							<goal>copy-dependencies</goal>
						</goals>
						<configuration>
							<outputDirectory>${project.build.directory}/lib</outputDirectory>
							<overWriteReleases>false</overWriteReleases>
							<overWriteSnapshots>false</overWriteSnapshots>
							<overWriteIfNewer>true</overWriteIfNewer>
							<excludeTransitive>true</excludeTransitive>
							<excludeArtifactIds>marmots-opencms-maven-plugin</excludeArtifactIds>
						</configuration>
					</execution>
				</executions>
			</plugin>

			<plugin>
				<groupId>org.marmots.opencms</groupId>
				<artifactId>marmots-opencms-maven-plugin</artifactId>
				<version>0.0.1</version>
				<executions>
					<execution>
						<id>publish</id>
						<phase>compile</phase>
						<goals>
							<goal>publish</goal>
						</goals>
						<configuration>
							<url>smb://Admin:admin@localhost:1446/OPENCMS/</url>
							<module>org.marmots.opencms.samplesite</module>
						</configuration>
					</execution>
					<execution>
						<id>module</id>
						<phase>package</phase>
						<goals>
							<goal>module</goal>
						</goals>
						<configuration>
							<module>org.marmots.opencms.samplesite</module>
						</configuration>
					</execution>
				</executions>
			</plugin>
		</plugins>

		<pluginManagement>
			<plugins>
				<!--This plugin's configuration is used to store Eclipse m2e settings only. It has no influence on the Maven build itself. -->
				<plugin>
					<groupId>org.eclipse.m2e</groupId>
					<artifactId>lifecycle-mapping</artifactId>
					<version>1.0.0</version>
					<configuration>
						<lifecycleMappingMetadata>
							<pluginExecutions>
								<pluginExecution>
									<pluginExecutionFilter>
										<groupId>
											org.marmots.opencms
										</groupId>
										<artifactId>
											marmots-opencms-maven-plugin
										</artifactId>
										<versionRange>
											[0.0.1,)
										</versionRange>
										<goals>
											<goal>module</goal>
										</goals>
									</pluginExecutionFilter>
									<action>
										<ignore></ignore>
									</action>
								</pluginExecution>
								<pluginExecution>
									<pluginExecutionFilter>
										<groupId>
											org.marmots.opencms
										</groupId>
										<artifactId>
											marmots-opencms-maven-plugin
										</artifactId>
										<versionRange>
											[0.0.1,)
										</versionRange>
										<goals>
											<goal>publish</goal>
										</goals>
									</pluginExecutionFilter>
									<action>
										<execute>
											<runOnIncremental>true</runOnIncremental>
										</execute>
									</action>
								</pluginExecution>
							</pluginExecutions>
						</lifecycleMappingMetadata>
					</configuration>
				</plugin>
			</plugins>
		</pluginManagement>
	</build>
```

# That's it!

* eclipse m2 compile phase will trigger (automatically) publish goal: the plugin will copy all modified resources to opencms via JLAN url (smb://Admin:admin@localhost:1446/OPENCMS/)
* maven install phase will trigger package goal: the plugin will create org.marmots.opencms.samplesite.zip
