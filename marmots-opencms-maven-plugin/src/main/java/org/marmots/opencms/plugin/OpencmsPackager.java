package org.marmots.opencms.plugin;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Goal which packages as opencms module.
 *
 * @goal module
 * 
 * @phase package
 */
public class OpencmsPackager extends AbstractMojo {
	private static final Logger logger = LoggerFactory.getLogger(OpencmsPackager.class);

	/**
	 * Location of the file.
	 * 
	 * @parameter property="project.build.directory"
	 * @required
	 */
	private File outputDirectory;

	/**
	 * Location of resources directory
	 * 
	 * @parameter property="project.build.sourceDirectory"
	 */
	private File sourceDirectory;

	/**
	 * Location of resources directory
	 * 
	 * @parameter property="module"
	 */
	private String module;

	private File opencmsDirectory;
	private ManifestBuilder manifestBuilder;

	private File opencmsDirectory() {
		if (opencmsDirectory == null) {
			// Set opencms directory
			opencmsDirectory = new File(sourceDirectory.getParent() + "/opencms");

			if (logger.isInfoEnabled()) {
				logger.info("using opencms directory: " + opencmsDirectory.getAbsolutePath());
			}
		}
		return opencmsDirectory;
	}

	private void addToZip(ZipOutputStream output, File file, String folder) throws Exception {
		// Fix folder name
		if (!StringUtils.isEmpty(folder)) {
			folder += "/";
		}

		// File name
		String name = OpencmsUtils.fixFilename(folder + file.getName());
		logger.info("processing file: " + name);

		// Module config
		if (FilenameUtils.getName(name).equals("module-config.xml")) {
			name = FilenameUtils.getPath(name) + ".config";
			logger.info("module config file: " + name);
		}

		// Check if it's manifest
		if (!name.equals("manifest.xml")) {
			manifestBuilder.addToManifest(file, name);
		}

		// Process entry
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (File f : files) {
				addToZip(output, f, name);
			}
		} else {
			output.putNextEntry(new ZipEntry(name));
			FileInputStream input = new FileInputStream(file);
			IOUtils.copy(input, output);
			IOUtils.closeQuietly(input);
		}
	}

	public void execute() throws MojoExecutionException {
		FileOutputStream zip = null;
		try {
			File f = outputDirectory;

			logger.info("packaging opencms module...");

			if (!f.exists()) {
				f.mkdirs();
			}

			logger.info("instantiating manifest builder...");
			manifestBuilder = new ManifestBuilder(opencmsDirectory());

			zip = new FileOutputStream(outputDirectory.getAbsolutePath() + "/" + module + ".zip");
			ZipOutputStream output = new ZipOutputStream(zip);

			logger.info("packaging opencms files...");
			File[] opencmsFiles = opencmsDirectory().listFiles();
			for (File file : opencmsFiles) {
				if (file.isDirectory()) {
					logger.info("packaging folder: " + file.getName());
					addToZip(output, file, "");
				}
			}

			logger.info("packaging class files...");
			File classes = new File(outputDirectory.getAbsolutePath() + "/classes");
			manifestBuilder.addToManifest(classes, "system/modules/" + module + "/classes");
			File[] classFiles = classes.listFiles();
			for (File file : classFiles) {
				if (file.isDirectory()) {
					logger.info("packaging folder: " + file.getName());
					addToZip(output, file, "system/modules/" + module + "/classes");
				}
			}

			try {
				logger.info("packaging jar files...");
				addToZip(output, new File(outputDirectory.getAbsolutePath() + "/lib"), "system/modules/" + module);
			} catch (Exception exc) {
				logger.info("no jar dependencies found");
			}

			File file = new File(outputDirectory.getAbsolutePath() + "/manifest.xml");
			manifestBuilder.writeManifest(file);
			addToZip(output, file, "");

			IOUtils.closeQuietly(output);

			logger.info("opencms module packaged successfully.");

		} catch (Exception e) {
			throw new MojoExecutionException("Error creating module package", e);
		} finally {
			IOUtils.closeQuietly(zip);
		}
	}
}
