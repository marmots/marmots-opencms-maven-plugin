package org.marmots.opencms.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;

import jcifs.smb.SmbFile;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Goal which packages as opencms module.
 *
 * @goal publish
 * 
 * @phase compile
 */
public class OpencmsPublisher extends AbstractMojo {
	private static final Logger logger = LoggerFactory.getLogger(OpencmsPublisher.class);
	
	/**
	 * Location of resources directory
	 * 
	 * @parameter property="project.build.sourceDirectory"
	 */
	private File sourceDirectory;

	/**
	 * Location of resources directory
	 * 
	 * @parameter property="project.build.directory"
	 */
	private File buildDirectory;

	/**
	 * Location of resources directory
	 * 
	 * @parameter property="project.build.resources[0].directory"
	 */
	private File resourcesDirectory;

	/**
	 * Location of resources directory
	 * 
	 * @parameter property="url"
	 */
	private String url;

	/**
	 * Location of resources directory
	 * 
	 * @parameter property="module"
	 */
	private String module;

	private File opencmsDirectory;

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

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			logger.info("publishing files...");

			// opencms files
			File[] files = opencmsDirectory().listFiles();
			for (File file : files) {
				if (file.isDirectory()) {
					copy(file, "");
				}
			}

			// class files
			File[] classFiles = new File(buildDirectory.getAbsolutePath() + "/classes").listFiles();
			for (File file : classFiles) {
				if (file.isDirectory()) {
					copy(file, "system/modules/" + module + "/classes");
				}
			}

			// resource files
			if (resourcesDirectory != null) {
				File[] resourceFiles = resourcesDirectory.listFiles();
				if (resourceFiles != null) {
					for (File file : resourceFiles) {
						if (file.isDirectory()) {
							copy(file, "system/modules/" + module + "/classes");
						}
					}
				}
			}

		} catch (Exception e) {
			throw new MojoExecutionException("Error publishing files", e);
		}
	}

	public void copy(File file, String folder) throws Exception {
		// Fix folder name
		if (!StringUtils.isEmpty(folder)) {
			folder += "/";
		}

		// File name
		String name = OpencmsUtils.fixFilename(folder + file.getName());
		logger.info("publishing file: " + name);

		// Module config
		if (FilenameUtils.getName(name).equals("module-config.xml")) {
			name = FilenameUtils.getPath(name) + ".config";
			logger.info("module config file: " + name);
		}

		SmbFile smbFile = new SmbFile(url + name);
		if (file.isDirectory()) {
			if (!smbFile.exists()) {
				smbFile.mkdirs();
			}

			File[] files = file.listFiles();
			for (File f : files) {
				copy(f, name);
			}
		} else {
			if (smbFile.getLastModified() < file.lastModified()) {
				if (logger.isDebugEnabled()) {
					logger.debug("file modified, copying to " + smbFile.getPath());
				}
				OutputStream output = smbFile.getOutputStream();
				IOUtils.copy(new FileInputStream(file), output);
				IOUtils.closeQuietly(output);
			} else if (logger.isDebugEnabled()) {
				logger.debug("not modified");
			}
		}
	}

}
