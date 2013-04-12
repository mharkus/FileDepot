/*
 * Copyright (C) 2013 Marc Lester Tan
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

package com.sap.sapfiledepotandroid.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.Repository;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;

public class CMISUtil {
	public static Session login(String username, String password, String url) throws RepositoryNotFoundException {
		SessionFactory sessionFactory = SessionFactoryImpl.newInstance();
		Map<String, String> parameter = new HashMap<String, String>();
		parameter.put(SessionParameter.USER, username);
		parameter.put(SessionParameter.PASSWORD, password);
		parameter.put(SessionParameter.ATOMPUB_URL, url);
		parameter.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());

		List<Repository> repositories = sessionFactory.getRepositories(parameter);
		if (repositories.size() > 0) {
			return repositories.get(0).createSession();
		}

		throw new RepositoryNotFoundException();
	}

	public static void deleteDocument(Document document) {
		deleteDocument(document, false);
	}

	public static void deleteDocument(Document document, boolean deleteAllVersions) {
		document.delete(deleteAllVersions);
	}

	public static void purgeFolder(Folder folder) {
		purgeFolder(folder, false, false);
	}

	public static void purgeFolder(Folder folder, boolean deleteAllVersions, boolean continueOnFailure) {
		folder.deleteTree(deleteAllVersions, UnfileObject.DELETE, continueOnFailure);
	}

	public static Folder createFolder(Folder parent, String folderName) {

		if (folderName == null || folderName.trim().length() == 0) {
			throw new IllegalArgumentException("folderName cannot be empty");
		}

		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
		properties.put(PropertyIds.NAME, folderName);
		return parent.createFolder(properties);

	}

	public static ContentStream createContentStream(Session session, String fileName, String mimeType, File file) throws FileNotFoundException {
		return createContentStream(session, fileName, mimeType, file.length(), new FileInputStream(file));
	}

	public static ContentStream createContentStream(Session session, String fileName, String mimeType, String content) {
		ByteArrayInputStream input = null;
		byte[] buf = null;

		buf = content.getBytes();
		input = new ByteArrayInputStream(buf);

		return createContentStream(session, fileName, mimeType, buf.length, input);

	}

	public static ContentStream createContentStream(Session session, String fileName, String mimeType, long len, InputStream input) {
		return session.getObjectFactory().createContentStream(fileName, len, mimeType, input);

	}

	public static Document createContent(Session session, Folder parent, String fileName, ContentStream contentStream) throws CmisBaseException {
		Map<String, Object> properties = new HashMap<String, Object>();
		properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
		properties.put(PropertyIds.NAME, fileName);

		Document doc = parent.createDocument(properties, contentStream, VersioningState.MAJOR);

		return doc;
	}

}
