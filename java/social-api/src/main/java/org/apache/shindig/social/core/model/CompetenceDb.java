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

import java.util.Date;
import java.util.List;

import org.apache.shindig.social.opensocial.model.Competence;
import org.apache.shindig.social.opensocial.model.Person;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.Version;

import javax.persistence.DiscriminatorValue;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * Messages are stored in the message table.
 */
@Entity
@Table(name="person_competences")
public class CompetenceDb implements Competence, DbObject {
  /**
   * The internal object ID used for references to this object. Should be generated
   * by the underlying storage mechanism
   */
  @Id
  @GeneratedValue(strategy=IDENTITY)
  @Column(name="id")
  protected long objectId;
  
  // In Customer class:
  // 
  // @OneToMany(cascade=ALL, mappedBy="customer")
  // public Set getOrders() { return orders; }
  // 
  // In Order class:
  // 
  // @ManyToOne
  // @JoinColumn(name="CUST_ID", nullable=false)
  // public Customer getCustomer() { return customer; }
  
  @ManyToOne(targetEntity=UserDb.class)
  @JoinColumn(name="user_id", referencedColumnName="id")
  private Person person;

  /**
   * model field.
   * @see org.apache.shindig.social.opensocial.model.Message
   */
  @Basic
  @Column(name="topic", length=255)
  protected String topic;

  /**
   * model field.
   * @see org.apache.shindig.social.opensocial.model.Message
   */
  @Basic
  @Column(name="value", length=255)
  protected String value;

  /**
   * create an empty message.
   */
  public CompetenceDb() {
  }

  /**
   * Create a message object with body, title and type.
   * @param topic the body of the message.
   * @param value the title of the message.
   */
  public CompetenceDb(String initTopic, String initValue) {
    this.topic = initTopic;
    this.value = initValue;
  }
  
  /**
   * @return the objectId
   */
  public long getObjectId() {
    return objectId;
  }
  
  /**
   * @return the objectId
   */
  public Person getPerson() {
    return null;
  }

  /**
   * @return the objectId
   */
  public void setPerson(Person person) {
    this.person = person;
  }
  
  /**
   * {@inheritDoc}
   * @see org.apache.shindig.social.opensocial.model.Message#getBody()
   */
  public String getTopic() {
    return this.topic;
  }

  /**
   * {@inheritDoc}
   * @see org.apache.shindig.social.opensocial.model.Message#setBody(java.lang.String)
   */
  public void setTopic(String topic) {
    this.topic = topic;
  }

  /**
   * {@inheritDoc}
   * @see org.apache.shindig.social.opensocial.model.Message#getTitle()
   */
  public String getValue() {
    return this.value;
  }

  /**
   * {@inheritDoc}
   * @see org.apache.shindig.social.opensocial.model.Message#setTitle(java.lang.String)
   */
  public void setValue(String value) {
    this.value = value;
  }

}
