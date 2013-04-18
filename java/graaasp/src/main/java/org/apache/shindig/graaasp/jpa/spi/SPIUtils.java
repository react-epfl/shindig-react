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
import com.google.common.collect.Maps;

import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.social.opensocial.spi.UserId;
import org.apache.shindig.social.opensocial.spi.SpaceId;
import org.apache.shindig.social.opensocial.spi.AppId;
import org.apache.shindig.social.opensocial.spi.DocumentId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 *
 */
public class SPIUtils {

  /**
   * @param userIds
   * @param token
   * @return
   */
  public static List<String> getUserList(Set<UserId> userIds, SecurityToken token) {
    // TODO What's the use of userIdMap?
    HashMap<String, String> userIdMap = Maps.newHashMap();
    List<String> paramList = Lists.newArrayList();
    for (UserId u : userIds) {
      try {
        String uid = u.getUserId(token);
        if (uid != null) {
          userIdMap.put(uid, uid);
          paramList.add(uid);
        }
      } catch (IllegalStateException istate) {
        // ignore the user id.
      }
    }
    return paramList;
  }
  
  /**
   * @param spaceIds
   * @param token
   * @return
   */
  public static List<String> getSpaceList(Set<SpaceId> spaceIds) {
    // TODO What's the use of userIdMap?
    HashMap<String, String> spaceIdMap = Maps.newHashMap();
    List<String> paramList = Lists.newArrayList();
    for (SpaceId s : spaceIds) {
      try {
    	  String sid = s.getSpaceId(); 
    	  spaceIdMap.put(sid, sid);
    	  paramList.add(sid);
      } catch (IllegalStateException istate) {
        // ignore the space id.
      }
    }
    return paramList;
  }
  
  /**
   * @param AppIds
   * @param token
   * @return
   */
  public static List<String> getAppList(Set<AppId> AppIds) {
    // TODO What's the use of userIdMap?
    HashMap<String, String> AppIdMap = Maps.newHashMap();
    List<String> paramList = Lists.newArrayList();
    for (AppId a : AppIds) {
      try {
    	  String aid = a.getAppId(); 
    	  AppIdMap.put(aid, aid);
    	  paramList.add(aid);
      } catch (IllegalStateException istate) {
        // ignore the application id.
      }
    }
    return paramList;
  }

  /**
   * @param documentIds
   * @param token
   * @return
   */
  public static List<String> getDocumentList(Set<DocumentId> documentIds) {
    // TODO What's the use of userIdMap?
    HashMap<String, String> documentIdMap = Maps.newHashMap();
    List<String> paramList = Lists.newArrayList();
    for (DocumentId d : documentIds) {
      try {
    	  String did = d.getDocumentId(); 
    	  documentIdMap.put(did, did);
    	  paramList.add(did);
      } catch (IllegalStateException istate) {
        // ignore the document id.
      }
    }
    return paramList;
  }   
  /**
   * @param userId
   * @param token
   * @return
   */
  public static String getUserList(UserId userId, SecurityToken token) {
    return userId.getUserId(token);
  }

  public static <T> List<T> toList(Set<T> s) {
    List<T> l = new ArrayList<T>(s.size());
    l.addAll(s);
    return l;
  }

}
