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

import org.apache.shindig.protocol.model.ExtendableBean;
import org.apache.shindig.protocol.model.ExtendableBeanImpl;
import org.apache.shindig.social.opensocial.model.ActivityEntry;
import org.apache.shindig.social.opensocial.model.ActivityObject;
import org.apache.shindig.social.opensocial.model.MediaLink;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.GenerationType.IDENTITY;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.Version;

import com.google.inject.name.Named;
import com.google.inject.Inject;

@Entity
@Table(name = "activities")
@NamedQueries(value = {
    @NamedQuery(name = ActivityEntryDb.FINDBY_ACTIVITY_ID,
        query = "select a from ActivityEntryDb a where a.id = :id")
  })
public class ActivityEntryDb implements ActivityEntry, DbObject {

  public static final String FINDBY_ACTIVITY_ID = "q.activities.findbyactivityentryid";
  
  public static final String PARAM_ACTIVITY_ID = "id";

  //public static final String PARAM_USERID = "initiator_user_id";

  public static final String JPQL_FINDBY_ACTIVITIES = null;

  public static final String JPQL_FINDACTIVITY = "select a from ActivityEntryDb a where ";

  /**
   * The internal object ID used for references to this object. Should be generated by the
   * underlying storage mechanism
   */
  @Id
  @GeneratedValue(strategy = IDENTITY)
  @Column(name = "id")
  private long objectId;

  @Basic
  @Column(name = "id", length = 255, insertable = false, updatable = false)
  protected String id;

  /*
  @ManyToOne(targetEntity = UserDb.class, cascade = ALL)
  @JoinColumn(name = "initiator_user_id", referencedColumnName = "id")
  */
  @Transient
  private ActivityObject actor;
  

  @Transient
  private String content;

  @Transient
  private ActivityObject generator;

  @Transient
  private MediaLink icon;

  @Transient
  private ActivityObject object;

  @Transient
  private String published;

  @Transient
  private ActivityObject provider;

  @Transient
  private ActivityObject target;

  @Transient
  private String title;

  @Transient
  private String updated;

  @Transient
  private String url;

  @Transient
  private String verb;

  @Transient
  private ExtendableBean openSocial;
  
  @Transient
  private ExtendableBean extensions;


  /**
   * Create a new empty ActivityEntry
   */
  public ActivityEntryDb() { }

  public ActivityEntryDb(String id, ActivityObject actor) {
    this.id = id;
    this.actor = actor;
  }

  public ActivityObject getActor() {
    return actor;
  }

  /** {@inheritDoc} */
  public void setActor(ActivityObject actor) {
    this.actor = actor;
  }

  /** {@inheritDoc} */
  public String getContent() {
    return content;
  }

  /** {@inheritDoc} */
  public void setContent(String content) {
    this.content = content;
  }

  /** {@inheritDoc} */
  public ActivityObject getGenerator() {
    return generator;
  }

  /** {@inheritDoc} */
  public void setGenerator(ActivityObject generator) {
    this.generator = generator;
  }

  /** {@inheritDoc} */
  public MediaLink getIcon() {
    return icon;
  }

  /** {@inheritDoc} */
  public void setIcon(MediaLink icon) {
    this.icon = icon;
  }

  /** {@inheritDoc} */
  public String getId() {
    return id;
  }

  /** {@inheritDoc} */
  public void setId(String id) {
    this.id = id;
  }

  /** {@inheritDoc} */
  public ActivityObject getObject() {
    return object;
  }

  /** {@inheritDoc} */
  public void setObject(ActivityObject object) {
    this.object = object;
  }

  /** {@inheritDoc} */
  public String getPublished() {
    return published;
  }

  /** {@inheritDoc} */
  public void setPublished(String published) {
    this.published = published;
  }

  /** {@inheritDoc} */
  public ActivityObject getProvider() {
    return provider;
  }

  /** {@inheritDoc} */
  public void setProvider(ActivityObject provider) {
    this.provider = provider;
  }

  /** {@inheritDoc} */
  public ActivityObject getTarget() {
    return target;
  }

  /** {@inheritDoc} */
  public void setTarget(ActivityObject target) {
    this.target = target;
  }

  /** {@inheritDoc} */
  public String getTitle() {
    return title;
  }

  /** {@inheritDoc} */
  public void setTitle(String title) {
    this.title = title;
  }

  /** {@inheritDoc} */
  public String getUpdated() {
    return updated;
  }

  /** {@inheritDoc} */
  public void setUpdated(String updated) {
    this.updated = updated;
  }

  /** {@inheritDoc} */
  public String getUrl() {
    return url;
  }

  /** {@inheritDoc} */
  public void setUrl(String url) {
    this.url = url;
  }

  /** {@inheritDoc} */
  public String getVerb() {
    return verb;
  }

  /** {@inheritDoc} */
  public void setVerb(String verb) {
    this.verb = verb;
  }

  public long getObjectId() {
    return objectId;
  }

  public int compareTo(ActivityEntry that) {
    if (this.getPublished() == null && that.getPublished() == null) {
      return 0;   // both are null, equal
    } else if (this.getPublished() == null) {
      return -1;  // this is null, comes before real date
    } else if (that.getPublished() == null) {
      return 1;   // that is null, this comes after
    } else {      // compare publish dates in lexicographical order
      return this.getPublished().compareTo(that.getPublished());
    }
  }

}