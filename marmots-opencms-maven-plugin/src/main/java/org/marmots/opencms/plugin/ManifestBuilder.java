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

public class ManifestBuilder {
	private static final Logger logger = LoggerFactory.getLogger(ManifestBuilder.class);
	
	private Document manifest;
	private File folder;

	public ManifestBuilder(File folder) {
		this.folder = folder;
	}

	public Document openManifest() throws Exception {
		if (manifest == null) {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			manifest = builder.parse(folder.getAbsolutePath() + "/manifest.xml");
			// normalize...
			manifest.normalize();
		}
		return manifest;
	}

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

	public void addToManifest(File f, String filename) throws Exception {
		String ext = FilenameUtils.getExtension(filename);
		String name = FilenameUtils.getName(filename);
		boolean isFolder = f.isDirectory();
		String folder = isFolder ? filename : filename.substring(0, filename.length() - (name.length() + 1));

		if (logger.isDebugEnabled()) {
			logger.debug("--------------------");
			logger.debug("folder: " + folder);
			logger.debug("name: " + name);
			logger.debug("ext: " + ext);
		}

		Document doc = openManifest();

		boolean exists = false;
		NodeList files = doc.getDocumentElement().getElementsByTagName("file");
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
			Node root = doc.getDocumentElement().getElementsByTagName("files").item(0);

			Element file = doc.createElement("file");
			root.appendChild(file);

			// Source
			if (!isFolder) {
				append(doc, file, "source", filename.trim());
			}

			// Destination
			append(doc, file, "destination", filename.trim());

			// guess cmstype
			String cmstype = OpencmsUtils.guessCmsFileType(isFolder, folder, name, ext);

			// Type
			append(doc, file, "type", cmstype);

			// uuidstructure
			String uuid = OpencmsUtils.getCmsUUID();
			append(doc, file, "uuidstructure", uuid);
			if (logger.isDebugEnabled()) {
				logger.debug("uuid generated for: /" + filename + " == " + uuid);
			}

			// uuidresource (same)
			if (!isFolder) {
				append(doc, file, "uuidresource", uuid);
			}

			// datelastmodified
			append(doc, file, "datelastmodified", new Date().toString());

			// userlastmodified
			append(doc, file, "userlastmodified", "Admin");

			// datecreated
			append(doc, file, "datecreated", new Date().toString());

			// usercreated
			append(doc, file, "usercreated", "Admin");

			// flags
			append(doc, file, "flags", "0");

			// <properties />
			append(doc, file, "properties", "");

			// <relations />
			append(doc, file, "relations", "");

			// <accesscontrol />
			append(doc, file, "accesscontrol", "");
		}
	}

	private void append(Document doc, Node node, String name, String value) {
		Element elem = doc.createElement(name);
		if (!StringUtils.isEmpty(value)) {
			elem.appendChild(doc.createTextNode(value));
		}
		node.appendChild(elem);
	}
}
