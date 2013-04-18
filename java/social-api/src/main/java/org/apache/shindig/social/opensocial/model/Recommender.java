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
package org.apache.shindig.social.opensocial.model;

import org.apache.shindig.protocol.model.Enum;
import org.apache.shindig.protocol.model.Exportablebean;
import org.apache.shindig.protocol.RestfulCollection;

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.inject.ImplementedBy;

import org.apache.shindig.social.opensocial.model.Space;
import org.apache.shindig.social.opensocial.model.Document;
import org.apache.shindig.social.opensocial.model.App;
import org.apache.shindig.social.opensocial.model.Person;

import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * see <a href="http://www.opensocial.org/Technical-Resources/opensocial-spec-v081/opensocial-reference#opensocial.RecommenderItem.Field">
 * http://www.opensocial.org/Technical-Resources/opensocial-spec-v081/opensocial-reference#opensocial.RecommenderItem.Field</a>
 * for all field meanings. All fields are represented in the js api at this time except for lastUpdated.
 * This field is currently only in the RESTful spec.
 *
 */
 /**
  * Data structure representing a recommender
  */
 public class Recommender {

   private RestfulCollection<Document>  documents;
   private RestfulCollection<App>  apps;
   private RestfulCollection<Space>  spaces;
   private RestfulCollection<Person>  people;

   public Recommender(RestfulCollection<Document> documents, RestfulCollection<App> apps, 
		   RestfulCollection<Space> spaces, RestfulCollection<Person> people) {
     this.documents = documents;
     this.apps = apps;
     this.spaces = spaces;
     this.people = people;
   }

   public RestfulCollection<Document> getDocuments() {
     return this.documents;
   }

   public RestfulCollection<Space> getSpaces() {
     return this.spaces;
   }

   public RestfulCollection<App> getApps() {
     return this.apps;
   }

   public RestfulCollection<Person> getPeople() {
     return this.people;
   }


 }