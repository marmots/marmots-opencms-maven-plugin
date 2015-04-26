package org.marmots.opencms.plugin;

import java.io.File;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class handles module configuration file (manifest.xml) creation. 
 * @author marmots
 */
public class ManifestBuilder {
	private static final Logger logger = LoggerFactory.getLogger(ManifestBuilder.class);

	private Document manifest;

	/**
	 * Opens an existing manifest.xml file to append necessary entries<br>
	 * A valid manifest.xml file obtained from module export is required 
	 * @param file the manifest.xml file
	 * @throws Exception parser or io exceptions
	 */
	public void openManifest(File file) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		manifest = builder.parse(file);
		// normalize...
		manifest.normalize();
	}

	/**
	 * Writes manifest.xml file to supplied destination
	 * @param file the file to write
	 * @throws Exception transformer related exceptions
	 */
	public void writeManifest(File file) throws Exception {
		// normalize...
		manifest.normalize();

		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
		DOMSource source = new DOMSource(manifest);
		StreamResult result = new StreamResult(file);
		transformer.transform(source, result);
	}

	/**
	 * Adds supplied file to manifest.xml
	 * @param filename the full name of the file in opencms (system/modules/...)
	 * @param isFolder a boolean indicating if it's a folder
	 * @throws Exception if a problem occurs
	 */
	public void addToManifest(String filename, boolean isFolder) throws Exception {
		if (manifest == null) {
			throw new Exception("Manifest document not initialized, call openManifest with a valid opencms module manifest.xml file");
		}

		String ext = FilenameUtils.getExtension(filename);
		String name = FilenameUtils.getName(filename);
		String folder = isFolder ? filename : filename.substring(0, filename.length() - (name.length() + 1));

		if (logger.isDebugEnabled()) {
			logger.debug("--------------------");
			logger.debug("folder: " + folder);
			logger.debug("name: " + name);
			logger.debug("ext: " + ext);
		}

		boolean exists = false;
		NodeList files = manifest.getDocumentElement().getElementsByTagName("file");
		for (int i = 0; i < files.getLength(); i++) {
			Node file = files.item(i);

			if (file.getNodeType() == Node.ELEMENT_NODE) {
				Element elem = (Element) file;
				String destination = elem.getElementsByTagName("destination").item(0).getChildNodes().item(0).getNodeValue();
				if (destination.equals(filename)) {
					exists = true;

					if (logger.isDebugEnabled()) {
						logger.debug("destination was present in manifest: " + destination);
					}

					break;
				}
			}
		}

		if (!exists) {
			Node root = manifest.getDocumentElement().getElementsByTagName("files").item(0);

			Element file = manifest.createElement("file");
			root.appendChild(file);

			// Source
			if (!isFolder) {
				append(file, "source", filename.trim());
			}

			// Destination
			append(file, "destination", filename.trim());

			// guess cmstype
			String cmstype = OpencmsUtils.guessCmsFileType(isFolder, folder, name, ext);

			// Type
			append(file, "type", cmstype);

			// uuidstructure
			String uuid = OpencmsUtils.getCmsUUID();
			append(file, "uuidstructure", uuid);
			if (logger.isDebugEnabled()) {
				logger.debug("uuid generated for: /" + filename + " == " + uuid);
			}

			// uuidresource (same)
			if (!isFolder) {
				append(file, "uuidresource", uuid);
			}

			// datelastmodified
			append(file, "datelastmodified", new Date().toString());

			// userlastmodified
			append(file, "userlastmodified", "Admin");

			// datecreated
			append(file, "datecreated", new Date().toString());

			// usercreated
			append(file, "usercreated", "Admin");

			// flags
			append(file, "flags", "0");

			// <properties />
			append(file, "properties", "");

			// <relations />
			append(file, "relations", "");

			// <accesscontrol />
			append(file, "accesscontrol", "");
		}
	}

	/**
	 * Appends in node a new element of type name with 
	 * @param node the node to append the element to
	 * @param name the type of the new element
	 * @param value element's value
	 */
	private void append(Node node, String name, String value) {
		Element elem = manifest.createElement(name);
		if (!StringUtils.isEmpty(value)) {
			elem.appendChild(manifest.createTextNode(value));
		}
		node.appendChild(elem);
	}
}
