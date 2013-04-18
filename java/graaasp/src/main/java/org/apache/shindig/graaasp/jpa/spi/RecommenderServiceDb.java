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
package org.apache.shindig.graaasp.jpa.spi;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.common.util.ImmediateFuture;
import org.apache.shindig.protocol.ProtocolException;
import org.apache.shindig.protocol.RestfulCollection;
import org.apache.shindig.graaasp.jpa.spi.JPQLUtils;
import org.apache.shindig.graaasp.jpa.spi.SPIUtils;
import org.apache.shindig.social.core.model.AssetDb;
import org.apache.shindig.social.core.model.FilterCapability;
import org.apache.shindig.social.core.model.FilterSpecification;
import org.apache.shindig.social.core.model.SpaceDb;
import org.apache.shindig.social.core.model.UserDb;
import org.apache.shindig.social.core.model.WidgetDb;
import org.apache.shindig.social.opensocial.model.Space;
import org.apache.shindig.social.opensocial.model.Document;
import org.apache.shindig.social.opensocial.model.App;
import org.apache.shindig.social.opensocial.model.Person;
import org.apache.shindig.social.opensocial.model.Recommender;

import org.apache.shindig.social.opensocial.spi.CollectionOptions;
import org.apache.shindig.social.opensocial.spi.GroupId;
import org.apache.shindig.social.opensocial.spi.RecommenderService;
import org.apache.shindig.social.opensocial.spi.Context;
import org.apache.shindig.social.opensocial.spi.SpaceId;
import org.apache.shindig.social.opensocial.spi.UserId;
import org.apache.shindig.social.opensocial.spi.DocumentId;
import org.apache.shindig.social.opensocial.spi.AppId;


import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.Future;
import java.util.regex.*;
import java.util.Random;


import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.http.HttpServletResponse;

/**
 * Implements the RecommenderService from the SPI binding to the JPA model and providing queries to
 * support the OpenSocial implementation.
 */
public class RecommenderServiceDb implements RecommenderService {

  /**
   * This is the JPA entity manager, shared by all threads accessing this service (need to check
   * that its really thread safe).
   */
  private EntityManager entityManager;

  /**
   * Create the RecommenderServiceDb, injecting an entity manager that is configured with the social
   * model.
   *
   * @param entityManager the entity manager containing the social model.
   */
  @Inject
  public RecommenderServiceDb(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  public Future<Recommender> getRecommendationsForContext(Context context, 
		  CollectionOptions collectionOptions, 
       SecurityToken token) throws ProtocolException {
    
    // get total number of spaces
    String query = "select count(s) from SpaceDb s";
    Query q = entityManager.createQuery(query);
    int spaceNumber = ((Long) q.getSingleResult()).intValue();
    
    Random generator = new Random();
    
    // list of spaces is retrieved for a context
    Set<SpaceId> spaceIds = new HashSet<SpaceId>();
    spaceIds.add(new SpaceId("0")); // has always be here
    String s = Integer.toString(generator.nextInt(spaceNumber)+1);
    spaceIds.add(new SpaceId(s));
    s = Integer.toString(generator.nextInt(spaceNumber)+1);
    spaceIds.add(new SpaceId(s));
    s = Integer.toString(generator.nextInt(spaceNumber)+1);
    spaceIds.add(new SpaceId(s));

    // get total number of documents
    query = "select count(a) from AssetDb a";
    q = entityManager.createQuery(query);
    int documentNumber = ((Long) q.getSingleResult()).intValue();

    // list of documents is retrieved for a context
    Set<DocumentId> documentIds = new HashSet<DocumentId>();
    documentIds.add(new DocumentId("0")); // has always be here
    s = Integer.toString(generator.nextInt(documentNumber)+1);
    documentIds.add(new DocumentId(s));
    s = Integer.toString(generator.nextInt(documentNumber)+1);
    documentIds.add(new DocumentId(s));
    s = Integer.toString(generator.nextInt(documentNumber)+1);
    documentIds.add(new DocumentId(s));

    query = "select count(w) from WidgetDb w";
    q = entityManager.createQuery(query);
    int appNumber = ((Long) q.getSingleResult()).intValue();

    // list of apps is retrieved for a context
    Set<AppId> AppIds = new HashSet<AppId>();
    AppIds.add(new AppId("0")); // has always be here
    s = Integer.toString(generator.nextInt(appNumber)+1);
    AppIds.add(new AppId(s));
    s = Integer.toString(generator.nextInt(appNumber)+1);
    AppIds.add(new AppId(s));
    s = Integer.toString(generator.nextInt(appNumber)+1);
    AppIds.add(new AppId(s));

    query = "select count(a) from UserDb a";
    q = entityManager.createQuery(query);
    int userNumber = ((Long) q.getSingleResult()).intValue();

    // list of people is retrieved for a context
    Set<UserId> userIds = new HashSet<UserId>();
    userIds.add(new UserId(UserId.Type.userId,"0")); // has always be here
    s = Integer.toString(generator.nextInt(userNumber)+1);
    userIds.add(new UserId(UserId.Type.userId,s));
    s = Integer.toString(generator.nextInt(userNumber)+1);
    userIds.add(new UserId(UserId.Type.userId,s));
    s = Integer.toString(generator.nextInt(userNumber)+1);
    userIds.add(new UserId(UserId.Type.userId,s));
    

    // all of the above could equally have been placed into a thread to overlay the
    // db wait times.
    RestfulCollection<Space> spaces = this.getSpaces(spaceIds, collectionOptions);
    RestfulCollection<Document> documents = this.getDocuments(documentIds, collectionOptions);
    RestfulCollection<App> apps = this.getApps(AppIds, collectionOptions);
    RestfulCollection<Person> people = this.getPeople(userIds, collectionOptions);
    
    Recommender r = new Recommender(documents, apps, spaces, people);
    
    // return ImmediateFuture.newInstance(restCollection);
    return ImmediateFuture.newInstance(r);
  }
  
  private RestfulCollection<Space> getSpaces(Set<SpaceId> spaceIds, 
      CollectionOptions collectionOptions) throws ProtocolException {
    // list of spaces is retrieved for a context

    List<Space> plist = null;
    int lastPos = 1;
    Long totalResults = null;

    StringBuilder sb = new StringBuilder();
    // sanitize the list to get the uid's and remove duplicates
    List<String> paramList = SPIUtils.getSpaceList(spaceIds);
    
    sb.append(SpaceDb.JPQL_FINDSPACE);
    lastPos = JPQLUtils.addInClause(sb, "s", "id", lastPos, paramList.size());

    
    plist = JPQLUtils.getListQuery(entityManager, sb.toString(), paramList, collectionOptions);

    if (plist == null) {
    	plist = Lists.newArrayList();
    }
    // FIXME: use JPQLUtils.getTotalResults for it
    totalResults = new Long(plist.size());
    // all of the above could equally have been placed into a thread to overlay the
    // db wait times.
    return new RestfulCollection<Space>(
        plist, collectionOptions.getFirst(), totalResults.intValue(), collectionOptions.getMax());   
  }

  private RestfulCollection<App> getApps(Set<AppId> AppIds, 
      CollectionOptions collectionOptions) throws ProtocolException {
    // list of apps is retrieved for a context

    List<App> plist = null;
    int lastPos = 1;
    Long totalResults = null;

    StringBuilder sb = new StringBuilder();
    // sanitize the list to get the uid's and remove duplicates
    List<String> paramList = SPIUtils.getAppList(AppIds);

    sb.append(WidgetDb.JPQL_FINDWIDGET);
    lastPos = JPQLUtils.addInClause(sb, "w", "id", lastPos, paramList.size());


    plist = JPQLUtils.getListQuery(entityManager, sb.toString(), paramList, collectionOptions);

    if (plist == null) {
    	plist = Lists.newArrayList();
    }
    // FIXME: use JPQLUtils.getTotalResults for it
    totalResults = new Long(plist.size());
    // all of the above could equally have been placed into a thread to overlay the
    // db wait times.
    return new RestfulCollection<App>(
        plist, collectionOptions.getFirst(), totalResults.intValue(), collectionOptions.getMax());   
  }

  private RestfulCollection<Document> getDocuments(Set<DocumentId> documentIds, 
      CollectionOptions collectionOptions) throws ProtocolException {
    // list of documents is retrieved for a context

    List<Document> plist = null;
    int lastPos = 1;
    Long totalResults = null;

    StringBuilder sb = new StringBuilder();
    // sanitize the list to get the uid's and remove duplicates
    List<String> paramList = SPIUtils.getDocumentList(documentIds);

    sb.append(AssetDb.JPQL_FINDDOCUMENT);
    lastPos = JPQLUtils.addInClause(sb, "a", "id", lastPos, paramList.size());


    plist = JPQLUtils.getListQuery(entityManager, sb.toString(), paramList, collectionOptions);

    if (plist == null) {
    	plist = Lists.newArrayList();
    }
    // FIXME: use JPQLUtils.getTotalResults for it
    totalResults = new Long(plist.size());
    // all of the above could equally have been placed into a thread to overlay the
    // db wait times.
    return new RestfulCollection<Document>(
        plist, collectionOptions.getFirst(), totalResults.intValue(), collectionOptions.getMax());   
  }

  private RestfulCollection<Person> getPeople(Set<UserId> userIds, 
      CollectionOptions collectionOptions) throws ProtocolException {
    // list of persons is retrieved for a context

    List<Person> plist = null;
    int lastPos = 1;
    Long totalResults = null;

    StringBuilder sb = new StringBuilder();
    // sanitize the list to get the uid's and remove duplicates
    List<String> paramList = SPIUtils.getUserList(userIds,null);

    sb.append(UserDb.JPQL_FINDPERSON);
    if (paramList.size() > 0) {
      lastPos = JPQLUtils.addInClause(sb, "p", "id", lastPos, paramList.size());      
      plist = JPQLUtils.getListQuery(entityManager, sb.toString(), paramList, collectionOptions);
    }

    if (plist == null) {
    	plist = Lists.newArrayList();
    }
    // FIXME: use JPQLUtils.getTotalResults for it
    totalResults = new Long(plist.size());
    // all of the above could equally have been placed into a thread to overlay the
    // db wait times.
    return new RestfulCollection<Person>(
        plist, collectionOptions.getFirst(), totalResults.intValue(), collectionOptions.getMax());   
  }

}
