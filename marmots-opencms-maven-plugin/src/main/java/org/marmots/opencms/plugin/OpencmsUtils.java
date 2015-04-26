package org.marmots.opencms.plugin;

import org.apache.commons.io.FilenameUtils;
import org.opencms.util.CmsUUID;

public class OpencmsUtils {
	/**
	 * Fixes filenames in order to be able to publish them in opencms
	 * @param filename the filename to fix
	 * @return filename fixed
	 */
	public static String fixFilename(String filename) {
		return filename.replaceAll(" ", "-").replaceAll("\\(", "-").replaceAll("\\)", "");
	}

	/**
	 * Determines opencms element type based on filename information (the only way we have)
	 * @param isFolder a boolean indicating if it's a folder
	 * @param filename the (full path) filename of the file in the opencms VFS
	 * @return the guessed opencms type
	 */
	public static String guessCmsFileType(boolean isFolder, String filename) {
		String ext = FilenameUtils.getExtension(filename);
		String name = FilenameUtils.getName(filename);
		String folder = isFolder ? filename : filename.substring(0, filename.length() - (name.length() + 1));
		return guessCmsFileType(isFolder, folder, name, ext);
	}

	/**
	 * Determines opencms element type based on filename information (the only way we have)
	 * @param isFolder a boolean indicating if it's a folder
	 * @param folder the name of the folder containing the file to be guessed
	 * @param name the name of the file to be guessed
	 * @param ext extension of the file to be guessed
	 * @return opecnsm type 
	 */
	public static String guessCmsFileType(boolean isFolder, String folder, String name, String ext) {
		String cmstype = "binary";
		if (isFolder) {
			cmstype = "folder";
		} else if (name.equals(".config")) {
			cmstype = "module_config";
		} else if (ext.equals("jsp")) {
			cmstype = "jsp";
		} else if (ext.equals("xml") && folder.endsWith("functions")) {
			cmstype = "function";
		} else if (ext.equals("xml") && folder.endsWith("formatters")) {
			cmstype = "formatter_config";
		} else if (ext.equals("xml") && folder.endsWith("elementviews")) {
			cmstype = "elementview";
		} else if (folder.contains("i18n")) {
			cmstype = "xmlvfsbundle";
		} else if (ext.equals("xml") || ext.equals("xsd")) {
			cmstype = "plain";
		}
		return cmstype;
	}
	
	/**
	 * creates a new UUID (using opencms API)
	 * @return the created uuid
	 */
	public static String getCmsUUID(){
		return new CmsUUID().getStringValue();
	}
}
