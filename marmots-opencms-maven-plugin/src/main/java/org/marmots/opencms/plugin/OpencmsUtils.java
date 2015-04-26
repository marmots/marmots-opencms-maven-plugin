package org.marmots.opencms.plugin;

import org.apache.commons.io.FilenameUtils;
import org.opencms.util.CmsUUID;

public class OpencmsUtils {
	public static String fixFilename(String filename) {
		return filename.replaceAll(" ", "-").replaceAll("\\(", "-").replaceAll("\\)", "");
	}

	public static String guessCmsFileType(boolean isFolder, String filename) {
		String ext = FilenameUtils.getExtension(filename);
		String name = FilenameUtils.getName(filename);
		String folder = isFolder ? filename : filename.substring(0, filename.length() - (name.length() + 1));
		return guessCmsFileType(isFolder, folder, name, ext);
	}

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
		} else if (folder.contains("i18n")) {
			cmstype = "xmlvfsbundle";
		} else if (ext.equals("xml") || ext.equals("xsd")) {
			cmstype = "plain";
		}
		return cmstype;
	}
	
	public static String getCmsUUID(){
		return new CmsUUID().getStringValue();
	}
}
