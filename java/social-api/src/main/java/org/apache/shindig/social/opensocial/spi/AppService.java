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
package org.apache.shindig.social.opensocial.spi;

import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.protocol.ProtocolException;
import org.apache.shindig.protocol.RestfulCollection;
import org.apache.shindig.social.opensocial.model.App;

import java.util.Set;
import java.util.concurrent.Future;

/**
 * Interface that defines how shindig gathers spaces information.
 */
public interface AppService {

  /**
   * When used will sort people by the container's definition of top friends. Note that both the
   * sort order and the filter are required to deliver a topFriends response. The PersonService
   * implementation should take this into account when delivering a topFriends response.
   */
  public static String TOP_FRIENDS_SORT = "topFriends";
  /**
   * Retrieves only the user's top friends. The meaning of top and how many top is is defined by the
   * PersonService implementation.
   */
  public static String TOP_FRIENDS_FILTER = "topFriends";
  /**
   * Retrieves all friends with any data for this app.
   * TODO: how is this app defined
   */
  public static String HAS_APP_FILTER = "hasApp";
  /**
   * Retrieves all friends. (ie no filter)
   */
  public static String ALL_FILTER = "all";
  /**
   * Will filter the people requested by checking if they are friends with the given idSpec. The
   * filter value will be set to the userId of the target friend.
   */
  public static String IS_WITH_FRIENDS_FILTER = "isFriendsWith";

  /**
   * Returns a list of apps for the context.
   *
   * @param context A context for which apps to be returned
   * @param collectionOptions How to filter, sort and paginate the collection being fetched
   * @param fields The profile details to fetch. Empty set implies all
   * @param token The gadget token @return a list of people.
   * @param keyFile The key to encrypt token
   * @return Future that returns a RestfulCollection of App
   */
  Future<RestfulCollection<App>> getAppsForContext(Context context,
      CollectionOptions collectionOptions, Set<String> fields, SecurityToken token, String keyFile)
      throws ProtocolException;
  
  /**
   * Returns a list of apps that correspond to the passed in appsIds.
   *
   * @param AppIds A set of app ids
   * @param collectionOptions How to filter, sort and paginate the collection being fetched
   * @param fields The profile details to fetch. Empty set implies all
   * @param token The gadget token @return a list of people.
   * @param keyFile The key to encrypt token
   * @return Future that returns a RestfulCollection of App
   */
  Future<RestfulCollection<App>> getApps(Set<AppId> AppIds,
      CollectionOptions collectionOptions, Set<String> fields, SecurityToken token, String keyFile)
      throws ProtocolException;
  
  /**
   * Returns an app that corresponds to the passed in app id.
   *
   * @param id The app id for which app info to be fetched.
   * @param fields The fields to fetch.
   * @param token The gadget token
   * @param keyFile The key to encrypt token
   * @return an app.
   */
  Future<App> getApp(AppId AppId, Set<String> fields, SecurityToken token, String keyFile)
      throws ProtocolException;
}
