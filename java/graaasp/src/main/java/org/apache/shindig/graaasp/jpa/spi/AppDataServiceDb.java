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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import javax.persistence.EntityManager;

import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.protocol.DataCollection;
import org.apache.shindig.protocol.ProtocolException;
import org.apache.shindig.social.opensocial.spi.AppDataService;
import org.apache.shindig.social.opensocial.spi.GroupId;
import org.apache.shindig.social.opensocial.spi.UserId;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.Futures;
import com.google.inject.Inject;

import org.apache.shindig.social.core.model.AppdataDb;

/**
 *
 */
public class AppDataServiceDb implements AppDataService {

  private EntityManager entityManager;

  @Inject
  public AppDataServiceDb(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  /**
   * {@inheritDoc}
   * TODO: there is a difference in this method compared to the one in samples. Doublecheck and test it
   */
  public Future<Void> deletePersonData(UserId userId, GroupId groupId, String appId,
      Set<String> fields, SecurityToken token) throws ProtocolException {

    if (appId == null) {
        appId = token.getAppId();
    }
    String uid = SPIUtils.getUserList(userId, token);
    String contextType = "User";
    long contextId = 0;

    if (uid.startsWith("s_")) { // for space
        // appdata for a space
        contextId = Long.parseLong(uid.replaceFirst("s_",""));
        contextType = "Space";
    } else { // for person
        contextId = Long.parseLong(uid);
    }

    Map<String,AppdataDb> dataMaps = getDataMap(contextId, contextType, groupId, appId);

     // TODO How should transactions be managed? Should samples be using warp-persist instead?
     if (!entityManager.getTransaction().isActive()) {
       entityManager.getTransaction().begin();
     }

    // only add in the fields
    if (fields == null || fields.isEmpty()) {
        // no keys, then remove all appdata
        for (AppdataDb ad : dataMaps.values()) {
            entityManager.remove(ad);
        }
    } else {
        // remove found keys
        for (String f : fields) {
            if (dataMaps.containsKey(f)) {
                AppdataDb ad = dataMaps.get(f);
                entityManager.remove(ad);
            }
        }
    }

    entityManager.flush();
    entityManager.getTransaction().commit();

    return Futures.immediateFuture(null);
  }

  /**
   * @param userId
   * @param groupId
   * @param appId
   * @param token
   * @return
   */
    private Map<String,AppdataDb> getDataMap(long contextId, String contextType, GroupId groupId, String appId) {
        List<Long> paramList = Lists.newArrayList();
        paramList.add(contextId);

        int lastParam = 1;
        StringBuilder sb = new StringBuilder();

        switch (groupId.getType()) {
            case all:
                // userId translates into all contacts
                sb.append(AppdataDb.FINDBY_ALL_GROUP);
                sb.append(" and ad.contextId = ?").append(lastParam);
                lastParam++;
                break;
            case friends:
                sb.append(AppdataDb.FINDBY_FRIENDS_GROUP);
                sb.append(" and ad.contextId = ?").append(lastParam);
                lastParam++;
                // userId translates into all friends
                break;
            case objectId:
                sb.append(AppdataDb.FINDBY_GROUP_GROUP);
                sb.append(" and ad.contextId = ?").append(lastParam);
                lastParam++;
                sb.append(" and g.id = ?").append(lastParam);
                paramList.add(Long.parseLong(groupId.getObjectId().toString()));
                lastParam++;
                // userId translates into friends within a group
                break;
            default: // including self
                // userId is the user Id
                sb.append(AppdataDb.FINDBY_SELF_GROUP);
                sb.append(" ad.contextId = ?").append(lastParam);
                sb.append(" and ad.contextType = '").append(contextType).append("'");
                lastParam++;
                break;

        }
        sb.append(" and ad.AppId = ?").append(lastParam);
        lastParam++;
        paramList.add(Long.parseLong(appId));
        List<AppdataDb> dataMaps = JPQLUtils.getListQuery(entityManager, sb.toString(), paramList, null);

        Map<String, AppdataDb> results = new HashMap<String, AppdataDb>();

        for (AppdataDb ad : dataMaps) {
            // build the hash "key" => Appdata for the key
            results.put(ad.getName(),ad);
        }

        return results;

    }

    /**
     * Filters contexts list to ids like: "s_1","s_2" -> "1","2"
     */
    private List<Long> removeContextInfo(List<String> contextList) {
        List<Long> idList = Lists.newArrayList();
        for (String c : contextList) {
            idList.add(Long.parseLong(c.replaceFirst("s_","")));
        }
        return idList;
    }

    /**
     * {@inheritDoc}
     */
    public Future<DataCollection> getPersonData(Set<UserId> userIds, GroupId groupId, String appId,
                                                Set<String> fields, SecurityToken token) throws ProtocolException {

        if (appId == null) {
            appId = token.getAppId();
        }
        List<String> contextList = SPIUtils.getUserList(userIds, token);
        // for now all context are defined by the first context in the list
        String firstContext = contextList.get(0);
        String contextType = "User";
        String prefix = "";

        if (firstContext.startsWith("s_")) { // for spaces
            contextType = "Space";
            prefix = "s_";
        }

        List<Long> paramList = removeContextInfo(contextList);
        int lastParam = 1;
        StringBuilder sb = new StringBuilder();

        switch (groupId.getType()) {
            case all:
                // userId translates into all contacts
                sb.append(AppdataDb.FINDBY_ALL_GROUP);
                lastParam = JPQLUtils.addInClause(sb, "ad", "contextId", lastParam, paramList.size());
                break;
            case friends:
                sb.append(AppdataDb.FINDBY_FRIENDS_GROUP);
                lastParam = JPQLUtils.addInClause(sb, "p", "id", lastParam, paramList.size());
                sb.append(')');
                // userId translates into all friends
                break;
            case objectId:
                sb.append(AppdataDb.FINDBY_GROUP_GROUP);
                lastParam = JPQLUtils.addInClause(sb, "ad", "contextId", lastParam, paramList.size());
                sb.append(" and g.id = ?").append(lastParam);
                paramList.add(Long.parseLong(groupId.getObjectId().toString()));
                lastParam++;
                // userId translates into friends within a group
                break;
            default: // including self
                // userId is the user Id
                sb.append(AppdataDb.FINDBY_SELF_GROUP);
                lastParam = JPQLUtils.addInClause(sb, "ad", "contextId", lastParam, paramList.size());
                sb.append(" and ad.contextType = '").append(contextType).append("'");
                break;

        }
        sb.append(" and ad.AppId = ?").append(lastParam);
        lastParam++;
        paramList.add(Long.parseLong(appId));

        // load the map up
        List<AppdataDb> dataMaps = JPQLUtils.getListQuery(entityManager, sb.toString(),
                paramList, null);
        Map<String, Map<String, String>> results = new HashMap<String, Map<String, String>>();

        // only add in the fields
        if (fields == null || fields.isEmpty()) {
            for (AppdataDb ad : dataMaps) {
                String key = prefix + ad.getContextId();
                Map<String, String> m = results.get(key);
                if (m == null) { // create new map and add it to results map
                    m = Maps.newHashMap();
                    results.put(key,m);
                }
                // add key-value to existing map
                m.put(ad.getName(),ad.getValue());
            }
        } else {
            for (AppdataDb ad : dataMaps) {
                if (fields.contains(ad.getName())) { // do manipulations only if key is in fields
                    String key = prefix + ad.getContextId();
                    Map<String, String> m = results.get(key);
                    if (m == null) { // create new map and add it to results map
                        m = Maps.newHashMap();
                        results.put(key,m);
                    }
                    // add key-value to existing map
                    m.put(ad.getName(),ad.getValue());
                }
            }
        }

        DataCollection dc = new DataCollection(results);
        return Futures.immediateFuture(dc);
    }

    /**
     * {@inheritDoc}
     */
    public Future<Void> updatePersonData(UserId userId, GroupId groupId, String appId,
                                         Set<String> fields, Map<String, String> values, SecurityToken token) throws ProtocolException {

        if (appId == null) {
            appId = token.getAppId();
        }
        String uid = SPIUtils.getUserList(userId, token);
        String contextType = "User";
        long contextId = 0;

        if (uid.startsWith("s_")) { // for space
            contextId = Long.parseLong(uid.replaceFirst("s_",""));
            contextType = "Space";
        } else { // for person
            contextId = Long.parseLong(uid);
        }

        Map<String,AppdataDb> dataMaps = getDataMap(contextId, contextType, groupId, appId);

        // TODO How should transactions be managed? Should samples be using warp-persist instead?
        if (!entityManager.getTransaction().isActive()) {
            entityManager.getTransaction().begin();
        }

        // go through all new values and update key-value
        for (String k : values.keySet()) {
            if (dataMaps.containsKey(k)) {
                // update the key
                AppdataDb ad = dataMaps.get(k);
                ad.setValue(values.get(k));
                entityManager.persist(ad);
            } else {
                // create the new key-value
                AppdataDb ad = new AppdataDb();
                ad.setContextId(contextId);
                ad.setContextType(contextType);
                ad.setAppId(Long.parseLong(appId));
                ad.setName(k);
                ad.setValue(values.get(k));
                entityManager.persist(ad);
            }
        }



        // for (AppdataDb adm : dataMaps) {
        //   entityManager.persist(adm);
        // }
        // entityManager.flush();

        entityManager.getTransaction().commit();

        return Futures.immediateFuture(null);
    }

}
