/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.shindig.social.opensocial.service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.io.File;
import org.apache.commons.lang.StringUtils;

import org.apache.shindig.common.util.ImmediateFuture;
import org.apache.shindig.config.ContainerConfig;
import org.apache.shindig.protocol.HandlerPreconditions;
import org.apache.shindig.protocol.Operation;
import org.apache.shindig.protocol.ProtocolException;
import org.apache.shindig.protocol.RequestItem;
import org.apache.shindig.protocol.RestfulCollection;
import org.apache.shindig.protocol.Service;
import org.apache.shindig.social.opensocial.model.Person;
import org.apache.shindig.social.opensocial.spi.CollectionOptions;
import org.apache.shindig.social.opensocial.spi.GroupId;
import org.apache.shindig.social.opensocial.spi.PersonService;
import org.apache.shindig.social.opensocial.spi.UserId;
import org.apache.shindig.social.opensocial.spi.Context;

import org.apache.shindig.common.crypto.BasicBlobCrypter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.google.common.base.Objects;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.Futures;

import com.google.inject.Inject;

/**
 * RPC/REST handler for all /people requests
 */
@Service(name = "people", path = "/{userId}+/{groupId}/{personId}+")
public class PersonHandler {
  private final PersonService personService;
  private final ContainerConfig config;


  // Return a future for the first item of a collection
  private static <T> Future<T> firstItem(Future<RestfulCollection<T>> collection) {
    Function<RestfulCollection<T>, T> firstItem = new Function<RestfulCollection<T>, T>() {
      @Override
      public T apply(RestfulCollection<T> c) {
        if (c != null && c.getTotalResults() > 0) {
          return c.getList().get(0);
        }
        return null;
      };
    };
    return Futures.lazyTransform(collection, firstItem);
 }

  @Inject
  public PersonHandler(PersonService personService, ContainerConfig config) {
    this.personService = personService;
    this.config = config;
  }

  /**
   * Allowed end-points /people/{userId}+/{groupId} /people/{userId}/{groupId}/{optionalPersonId}+
   *
   * examples: /people/john.doe/@all /people/john.doe/@friends /people/john.doe/@self
   */
  @Operation(httpMethods = "GET")
  public Future<?> get(SocialRequestItem request) throws ProtocolException {
    GroupId groupId = request.getGroup();
    Set<String> optionalPersonId = ImmutableSet.copyOf(request.getListParameter("personId"));
    Set<String> fields = request.getFields(Person.Field.DEFAULT_FIELDS);
    Set<UserId> userIds = request.getUsers();

    // Preconditions
    HandlerPreconditions.requireNotEmpty(userIds, "No userId specified");
    if (userIds.size() > 1 && !optionalPersonId.isEmpty()) {
      throw new IllegalArgumentException("Cannot fetch personIds for multiple userIds");
    }

    CollectionOptions options = new CollectionOptions(request);

    if (userIds.size() == 1) {

      // hack to get all public users in the system
      String uid = userIds.iterator().next().getUserId();
      if ("@all".equals(uid)) {
        return personService.getPeople(userIds, groupId, options, fields, request.getToken());
      }

      if (optionalPersonId.isEmpty()) {
        if (groupId.getType() == GroupId.Type.self) {
            // If a filter is set then we have to call getPeople(), otherwise use the simpler getPerson()
          if (options.getFilter() != null) {
            Future<RestfulCollection<Person>> people = personService.getPeople(
                userIds, groupId, options, fields, request.getToken());
            return firstItem(people);
          } else {
            return personService.getPerson(userIds.iterator().next(), fields, request.getToken());
          }
        } else {
          return personService.getPeople(userIds, groupId, options, fields, request.getToken());
        }
      } else if (optionalPersonId.size() == 1) {
        String param = optionalPersonId.iterator().next();
        if (param.equals("@person") || param.equals("@space")) {
          // TODO: hack - in this case optionalPersonId id is treated as contextType
          Context context = new Context(userIds.iterator().next().getUserId().toString(),param);
          
          return personService.getPeopleForContext(context, options, fields, request.getToken());
        } else {
          // TODO: Add some crazy concept to handle the userId?
          Set<UserId> optionalUserIds = ImmutableSet.of(
              new UserId(UserId.Type.userId, optionalPersonId.iterator().next()));

          Future<RestfulCollection<Person>> people = personService.getPeople(
              optionalUserIds, new GroupId(GroupId.Type.self, null),
              options, fields, request.getToken());
          return FutureUtil.getFirstFromCollection(people);
      } else {
        ImmutableSet.Builder<UserId> personIds = ImmutableSet.builder();
        for (String pid : optionalPersonId) {
          personIds.add(new UserId(UserId.Type.userId, pid));
        }
        // Every other case is a collection response of optional person ids
        return personService.getPeople(personIds.build(), new GroupId(GroupId.Type.self, null),
            options, fields, request.getToken());
      }
    }

    // Every other case is a collection response.
    return personService.getPeople(userIds, groupId, options, fields, request.getToken());
  }

  /**
   * Allowed end-points /people/{userId}/{groupId}
   *
   * examples: /people/john.doe/@all /people/john.doe/@friends /people/john.doe/@self
   */
  @Operation(httpMethods = "PUT", bodyParam = "person")
  public Future<?> update(SocialRequestItem request) throws ProtocolException {
    Set<UserId> userIds = request.getUsers();

    // Enforce preconditions - exactly one user is specified
    HandlerPreconditions.requireNotEmpty(userIds, "No userId specified");
    HandlerPreconditions.requireSingular(userIds, "Multiple userIds not supported");

    UserId userId = userIds.iterator().next();

    // Update person and return it
    return personService.updatePerson(Iterables.getOnlyElement(userIds),
        request.getTypedParameter("person", Person.class),
        request.getToken());
  }

  // Previous version of the method above 
  //   @Operation(httpMethods = "PUT", bodyParam = "person")
  // public Future<?> update(SocialRequestItem request) throws ProtocolException {
  //   Set<String> fields = request.getFields(Person.Field.DEFAULT_FIELDS);
  //   Set<UserId> userIds = request.getUsers();
    
  //   // Enforce preconditions - exactly one user is specified
  //   HandlerPreconditions.requireNotEmpty(userIds, "No userId specified");
  //   HandlerPreconditions.requireSingular(userIds, "Multiple userIds not supported");
    
  //   UserId userId = userIds.iterator().next();
    
  //   // Update person and return it
  //   return personService.updatePerson(Iterables.getOnlyElement(userIds), 
  //       request.getTypedParameter("person", Person.class), 
  //       fields, 
  //       request.getToken());
  // }

  @Operation(httpMethods = "GET", path="/@supportedFields")
  public List<Object> supportedFields(RequestItem request) {
    // TODO: Would be nice if name in config matched name of service.
    String container = Objects.firstNonNull(request.getToken().getContainer(), "default");
    return config.getList(container,
        "${Cur['gadgets.features'].opensocial.supportedFields.person}");
  }

  // encrypts a passed parameter string (to have ecrypted security token for graasp)
  // example: /crypted_security_token?tokens=1:2:4,11:22:44
  @Operation(httpMethods = "GET", path="/crypted_security_token")
  public Future<?> cryptedSecurityToken(RequestItem request) throws Exception {
    // get key file used for token encryption
    String keyFile = config.getString("default", "gadgets.securityTokenKeyFile");
    BasicBlobCrypter crypter = new BasicBlobCrypter(new File(keyFile));
    // get a list of tokens from request params
    List<String> tokens = request.getListParameter("tokens");
    List<String> output = Lists.newArrayList();

    for (String t : tokens) {
      Map<String, String> str = Maps.newHashMap();
      String[] s = StringUtils.split(t, ':');
      str.put("o", s[0]); // owner
      str.put("v", s[1]); // viewer
      str.put("g", s[2]); // appId = appUrl

      output.add("default:" + crypter.wrap(str)); // security token requires "default:" before
    }

    return ImmediateFuture.newInstance(output);
  }
}
