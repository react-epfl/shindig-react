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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.http.HttpServletResponse;

import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.protocol.ProtocolException;
import org.apache.shindig.protocol.RestfulCollection;

// changet following two imports for graasp
import org.apache.shindig.social.core.model.ActivityEntryDb;
import org.apache.shindig.social.core.model.MediaItemDb;

import org.apache.shindig.social.opensocial.model.ActivityEntry;
import org.apache.shindig.social.opensocial.model.MediaItem;
import org.apache.shindig.social.opensocial.spi.ActivityStreamService;
import org.apache.shindig.social.opensocial.spi.CollectionOptions;
import org.apache.shindig.social.opensocial.spi.GroupId;
import org.apache.shindig.social.opensocial.spi.UserId;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import com.google.inject.Inject;

import org.apache.shindig.graaasp.jpa.spi.JPQLUtils;
import org.apache.shindig.graaasp.jpa.spi.SPIUtils;

/**
 * The Class ActivityServiceDb.
 */
public class ActivityStreamServiceDb implements ActivityStreamService {

  /** The entity manager. */
  private EntityManager entityManager;

  /**
   * Instantiates a new activity service db.
   *
   * @param entityManager the entity manager
   */
  @Inject
  public ActivityStreamServiceDb(EntityManager entityManager) {
    this.entityManager = entityManager;
  }


  public Future<ActivityEntry> createActivityEntry(UserId userId, GroupId groupId, String appId,
      Set<String> fields, ActivityEntry activityEntry, SecurityToken token) throws ProtocolException {
    String uid = SPIUtils.getUserList(userId, token);

    try {
      // Map activity into a new ActivityEntryDb instance
      // TODO Could we use dozer to do this mapping instead, for future-proofing reasons?
      ActivityEntryDb activityEntryDb = new ActivityEntryDb();
      activityEntryDb.setActor(activityEntry.getActor());
      activityEntryDb.setContent(activityEntry.getContent());
      activityEntryDb.setGenerator(activityEntry.getGenerator());
      activityEntryDb.setIcon(activityEntry.getIcon());
      activityEntryDb.setId(activityEntry.getId());
      activityEntryDb.setObject(activityEntry.getObject());
      activityEntryDb.setPublished(activityEntry.getPublished());// TODO put the real date (why String ????) 
      activityEntryDb.setProvider(activityEntry.getProvider());
      activityEntryDb.setTarget(activityEntry.getTarget());
      activityEntryDb.setTitle(activityEntry.getTitle());
      activityEntryDb.setUpdated(activityEntry.getUpdated());// TODO put real date (why String ????) 
      activityEntryDb.setUrl(activityEntry.getUrl());
      activityEntryDb.setVerb(activityEntry.getVerb());

      // TODO How should transactions be managed? Should samples be using warp-persist instead?
      if (!entityManager.getTransaction().isActive()) {
        entityManager.getTransaction().begin();
      }
      entityManager.persist(activityEntryDb);
      entityManager.getTransaction().commit();

    } catch (Exception e) {
      throw new ProtocolException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to create activity entry", e);
    }

    return null;
  }


  public Future<Void> deleteActivityEntries(UserId userId, GroupId groupId, String appId,
      Set<String> activityIds, SecurityToken token) throws ProtocolException {
    // TODO Auto-generated method stub
    return null;
  }


  public Future<RestfulCollection<ActivityEntry>> getActivityEntries(Set<UserId> userIds,
      GroupId groupId, String appId, Set<String> fields,
      CollectionOptions options, SecurityToken token) throws ProtocolException {

    // TODO currently the implementation of this method ignores the fields variable. Is this correct?

    List<ActivityEntry> plist = null;
    int lastPos = 1;

    StringBuilder sb = new StringBuilder();
    // sanitize the list to get the uid's and remove duplicates
    List<String> paramList = SPIUtils.getUserList(userIds, token);
    // select the group Id as this will drive the query
    switch (groupId.getType()) { // TODO : implement friends, useful for Graasp ?
    case all:
      sb.append("");
      lastPos = JPQLUtils.addInClause(sb, "p", "id", lastPos, paramList.size());
      break;
    
    case objectId:
      // select those in the group
      // TODO Needs implementing and then have a unit test created to test it.
      sb.append("");
      lastPos = JPQLUtils.addInClause(sb, "p", "id", lastPos, paramList.size());
      sb.append(" and g.id = ?").append(lastPos);
      lastPos++;
      break;
      
    case self:
      // select self
      sb.append(ActivityEntryDb.JPQL_FINDACTIVITY);
      lastPos = JPQLUtils.addInClause(sb, "a", "userId", lastPos, paramList.size());
      break;
      
    default:
      throw new ProtocolException(HttpServletResponse.SC_BAD_REQUEST, "Group ID not recognized");

    }

    // Get total results, that is count the total number of rows for this query
    Long totalResults = JPQLUtils.getTotalResults(entityManager, sb.toString(), paramList);

    // Execute paginated query
    if (totalResults > 0) {
      plist = JPQLUtils.getListQuery(entityManager, sb.toString(), paramList, options);
    }

    if (plist == null) {
      plist = Lists.newArrayList();
    }

    plist = JPQLUtils.getListQuery(entityManager, sb.toString(), paramList, null);

    if (plist == null) {
      plist = new ArrayList<ActivityEntry>();
    }

    // all of the above could equally have been placed into a thread to overlay the
    // db wait times.
    RestfulCollection<ActivityEntry> restCollection = new RestfulCollection<ActivityEntry>(
        plist, options.getFirst(), totalResults.intValue(), options.getMax());
    return Futures.immediateFuture(restCollection);
  }

  public Future<RestfulCollection<ActivityEntry>> getActivityEntries(UserId userId,
      GroupId groupId, String appId, Set<String> fields,
      CollectionOptions options, Set<String> activityIds, SecurityToken token)
      throws ProtocolException {
    return Futures.immediateFuture(new RestfulCollection<ActivityEntry>(getActivityEntries(userId, activityIds, token)));
  }


  public Future<ActivityEntry> getActivityEntry(UserId userId, GroupId groupId, String appId,
      Set<String> fields, String activityId, SecurityToken token) throws ProtocolException {
    ActivityEntry activity = getActivityEntries(userId, activityId,  token);
    if ( activity != null  ) {
      return Futures.immediateFuture(activity);
    }
    throw new ProtocolException(HttpServletResponse.SC_BAD_REQUEST,"Cant find activity entry");
  }

  /**
   * Updates the specified Activity.
   * TODO
   */
  public Future<ActivityEntry> updateActivityEntry(UserId userId, GroupId groupId, String appId,
      Set<String> fields, ActivityEntry activity, String activityId,
      SecurityToken token) {

      throw new ProtocolException(HttpServletResponse.SC_BAD_REQUEST, "UpdateAvtivityEntry is not implemented.");
  }

  /**
   * Gets the activities.
   *
   * @param userId the user id
   * @param token the token
   * @param activityId the activity id
   *
   * @return the activities
   */
  private ActivityEntry getActivityEntries(UserId userId, String activityId,
      SecurityToken token) {
    Query q = entityManager.createNamedQuery(ActivityEntryDb.FINDBY_ACTIVITY_ID);
    String uid = SPIUtils.getUserList(userId, token);
    //q.setParameter(ActivityEntryDb.PARAM_USERID, uid);
    q.setParameter(ActivityEntryDb.PARAM_ACTIVITY_ID, activityId);
    q.setFirstResult(0);
    q.setMaxResults(1);
    List<?> activities = q.getResultList();
    if ( activities != null && !activities.isEmpty()) {
      return (ActivityEntry) activities.get(0);
    }
    return null;
  }

  /**
   * Gets the activities.
   *
   * @param userId the user id
   * @param token the token
   * @param activityIds the activity ids
   *
   * @return the activities
   */
  private List<ActivityEntry> getActivityEntries(UserId userId, Set<String> activityIds,
      SecurityToken token) {
    StringBuilder sb = new StringBuilder();
    sb.append(ActivityEntryDb.JPQL_FINDBY_ACTIVITIES);
    List<String> paramList = SPIUtils.toList(activityIds);
    String uid = SPIUtils.getUserList(userId, token);
    int lastPos = JPQLUtils.addInClause(sb, "a", "id", 1, paramList.size());
    sb.append(" and a.userid = ?").append(lastPos);
    lastPos++;
    paramList.add(uid);
    List<ActivityEntry> a = JPQLUtils.getListQuery(entityManager, sb.toString(), paramList, null);
    return a;
  }



}