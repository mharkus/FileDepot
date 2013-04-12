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

package com.sap.sapfiledepotandroid.data;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.chemistry.opencmis.client.api.CmisObject;

public class LocalDocument implements Serializable {
	private long contentStreamLength;
	private String contentStreamId;
	private String objectTypeId;
	private String parentIds;
	private String name;
	private String contentStreamMimeType;
	private Date creationDate;
	private int changeToken;
	private String lastModifiedBy;
	private String createdBy;
	private String owner;
	private String objectId;
	private String baseTypeId;
	private String contentStreamFileName;
	private Date lastModificationDate;

	/**
	 * 
	 */
	public LocalDocument(CmisObject obj) {

		// Sat Apr 13 03:04:08 GMT+08:00 2013
		SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
		if (obj.getProperty("cmis:contentStreamLength") != null) {
			contentStreamLength = Long.parseLong(obj.getProperty("cmis:contentStreamLength").getValueAsString());
		}

		if (obj.getProperty("cmis:contentStreamId") != null) {
			contentStreamId = obj.getProperty("cmis:contentStreamId").getValueAsString();
		}

		objectTypeId = obj.getProperty("cmis:objectTypeId").getValueAsString();
		parentIds = obj.getProperty("sap:parentIds").getValueAsString();
		name = obj.getProperty("cmis:name").getValueAsString();

		if (obj.getProperty("cmis:contentStreamMimeType") != null) {
			contentStreamMimeType = obj.getProperty("cmis:contentStreamMimeType").getValueAsString();
		}

		changeToken = Integer.parseInt(obj.getProperty("cmis:changeToken").getValueAsString());
		lastModifiedBy = obj.getProperty("cmis:lastModifiedBy").getValueAsString();
		createdBy = obj.getProperty("cmis:createdBy").getValueAsString();
		owner = obj.getProperty("sap:owner").getValueAsString();
		objectId = obj.getProperty("cmis:objectId").getValueAsString();
		baseTypeId = obj.getProperty("cmis:baseTypeId").getValueAsString();

		if (obj.getProperty("cmis:contentStreamFileName") != null) {
			contentStreamFileName = obj.getProperty("cmis:contentStreamFileName").getValueAsString();
		}

		try {
			creationDate = sdf.parse(obj.getProperty("cmis:creationDate").getValueAsString());
			lastModificationDate = sdf.parse(obj.getProperty("cmis:lastModificationDate").getValueAsString());
		} catch (ParseException e) {
			e.printStackTrace();
		}

	}

	public long getContentStreamLength() {
		return contentStreamLength;
	}

	public void setContentStreamLength(long contentStreamLength) {
		this.contentStreamLength = contentStreamLength;
	}

	public String getContentStreamId() {
		return contentStreamId;
	}

	public void setContentStreamId(String contentStreamId) {
		this.contentStreamId = contentStreamId;
	}

	public String getObjectTypeId() {
		return objectTypeId;
	}

	public void setObjectTypeId(String objectTypeId) {
		this.objectTypeId = objectTypeId;
	}

	public String getParentIds() {
		return parentIds;
	}

	public void setParentIds(String parentIds) {
		this.parentIds = parentIds;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getContentStreamMimeType() {
		return contentStreamMimeType;
	}

	public void setContentStreamMimeType(String contentStreamMimeType) {
		this.contentStreamMimeType = contentStreamMimeType;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public int getChangeToken() {
		return changeToken;
	}

	public void setChangeToken(int changeToken) {
		this.changeToken = changeToken;
	}

	public String getLastModifiedBy() {
		return lastModifiedBy;
	}

	public void setLastModifiedBy(String lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getObjectId() {
		return objectId;
	}

	public void setObjectId(String objectId) {
		this.objectId = objectId;
	}

	public String getBaseTypeId() {
		return baseTypeId;
	}

	public void setBaseTypeId(String baseTypeId) {
		this.baseTypeId = baseTypeId;
	}

	public String getContentStreamFileName() {
		return contentStreamFileName;
	}

	public void setContentStreamFileName(String contentStreamFileName) {
		this.contentStreamFileName = contentStreamFileName;
	}

	public Date getLastModificationDate() {
		return lastModificationDate;
	}

	public void setLastModificationDate(Date lastModificationDate) {
		this.lastModificationDate = lastModificationDate;
	}

}
