/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.apache.shindig.social.core.model;

import static javax.persistence.GenerationType.IDENTITY;

import org.apache.shindig.social.core.model.UserDb;
import org.apache.shindig.social.opensocial.model.Attachment;
import org.apache.shindig.social.opensocial.model.Document;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.persistence.Transient;
import javax.persistence.JoinColumn;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.util.List;

/**
 * Attachment storage, stored in "address", may be joined with other subclasses and if so
 * "address_usage" will contain the subclass discriminatory value. 
 * This might also be "sharedaddress" if this address is shared.
 */
@Entity
@Table(name = "attachments")
public class AttachmentDb implements Attachment, DbObject {

  public static final String GRAASP_URL = System.getProperty("shindig.container_url");

  /**
   * The internal object ID used for references to this object. Should be generated by the
   * underlying storage mechanism
   */
  @Id
  @GeneratedValue(strategy = IDENTITY)
  @Column(name = "id")
  private long objectId;

  @Basic
  @Column(name = "filename", length = 255)
  private String filename;

  @Basic
  @Column(name = "size", length = 255)
  private String size;

  @Basic
  @Column(name = "content_type", length = 255)
  private String contentType;

  @OneToOne(targetEntity = AssetDb.class)
  @JoinColumn(name = "asset_id", referencedColumnName = "id")
  private Document document;

  public String getUrl() {
    return GRAASP_URL+"/attachment/download/file/"+String.valueOf(objectId);  
  }

  public Document getDocument() {
    return null;
  }

  /** {@inheritDoc} */
  public void setDocument(Document document) {
    this.document = document;
  }

  public String getFilename() {
    return filename;
  }

  /** {@inheritDoc} */
  public void setFilename(String filename) {
    this.filename = filename;
  }

  public String getContentType() {
    return contentType;
  }

  /** {@inheritDoc} */
  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  public String getSize() {
    return size;
  }

  /** {@inheritDoc} */
  public void setSize(String size) {
    this.size = size;
  }


  /**
   * @return the objectId
   */
  public long getObjectId() {
    return objectId;
  }

}