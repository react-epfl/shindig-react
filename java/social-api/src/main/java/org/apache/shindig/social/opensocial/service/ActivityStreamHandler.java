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
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.Future;

import org.apache.shindig.config.ContainerConfig;
import org.apache.shindig.protocol.HandlerPreconditions;
import org.apache.shindig.protocol.Operation;
import org.apache.shindig.protocol.ProtocolException;
import org.apache.shindig.protocol.RequestItem;
import org.apache.shindig.protocol.Service;
import org.apache.shindig.social.opensocial.model.ActivityEntry;
import org.apache.shindig.social.opensocial.spi.ActivityStreamService;
import org.apache.shindig.social.opensocial.spi.CollectionOptions;
import org.apache.shindig.social.opensocial.spi.UserId;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;

import org.json.JSONObject;
import java.io.File;
import java.io.*;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.common.util.concurrent.Futures;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.nio.charset.Charset;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.ContentBody;

import org.apache.commons.fileupload.FileItem;


/**
 * <p>ActivityStreamHandler class.</p>
 */
@Service(name = "activitystreams", path="/{userId}+/{groupId}/{appId}/{activityId}+")
public class ActivityStreamHandler {

  private final ActivityStreamService service;
  private final ContainerConfig config;
  public static final String GRAASP_URL = System.getProperty("shindig.container_url");
  private final String GRAASP_TOKEN = System.getProperty("graasp.token");

  /**
   * <p>Constructor for ActivityStreamHandler.</p>
   *
   * @param service a {@link org.apache.shindig.social.opensocial.spi.ActivityStreamService} object.
   * @param config a {@link org.apache.shindig.config.ContainerConfig} object.
   */
  @Inject
  public ActivityStreamHandler(ActivityStreamService service, ContainerConfig config) {
    this.service = service;
    this.config = config;
  }

  /**
   * Not implemented. No implementation planed for Graasp.
   */
  @Operation(httpMethods="DELETE")
  public Future<?> delete(SocialRequestItem request)
      throws ProtocolException {
    return Futures.immediateFuture(null);
  }

  /**
   * Not implemented.
   */
  @Operation(httpMethods="PUT", bodyParam = "activity")
  public Future<?> update(SocialRequestItem request) throws ProtocolException {
    return Futures.immediateFuture(null);
  }

  /**
   * Allowed end-points /activitystreams/{userId}/@self/{appId}
   *
   * Examples: /activitystreams/john.doe/@self/{appId} - postBody is an activity object
   *
   * @param request a {@link org.apache.shindig.social.opensocial.service.SocialRequestItem} object.
   * @return a {@link java.util.concurrent.Future} object.
   * @throws org.apache.shindig.protocol.ProtocolException if any.
   */
  @Operation(httpMethods="POST", bodyParam = "activity")
  public Future<?> create(SocialRequestItem request) throws ProtocolException {
    try {
      Set<UserId> userIds = request.getUsers();
      List<String> activityIds = request.getListParameter("activityId");

      HandlerPreconditions.requireNotEmpty(userIds, "No userId specified");
      HandlerPreconditions.requireSingular(userIds, "Multiple userIds not supported");
      HandlerPreconditions.requireEmpty(activityIds, "Cannot specify activity ID in create");

      String activity = request.getParameter("activity");
      JSONObject test = new JSONObject(activity);
      activity = test.toString();
      String viewerId = request.getToken().getViewerId();
    
      //HandlerPreconditions.requireNotEmpty(viewerId, "No viewerId is specified");
    
      String output = "";
      HttpClient client = new DefaultHttpClient();
      HttpPost post = new HttpPost(GRAASP_URL+"/rest/activitystreams/"+userIds.iterator().next()+"/@self/@app?token="+GRAASP_TOKEN+"&user="+viewerId);
      post.getParams().setParameter("http.protocol.expect-continue", false);

      MultipartEntity entity = new MultipartEntity();
      entity.addPart("activity", new StringBody(activity,"application/json", Charset.forName("UTF-8")));
      post.setEntity(entity);
      
      // return back the response
      HttpResponse response = client.execute(post);

      BufferedReader rd = new BufferedReader(new InputStreamReader(
          response.getEntity().getContent()));
      String line = "";
      while ((line = rd.readLine()) != null) {
        output = line;
      }
      JSONObject jsonOutput = new JSONObject(output);
      return  Futures.immediateFuture(jsonOutput);

    } catch (Exception e) {
      return  Futures.immediateFuture(e);
    }
  }

  /**
   * ADAPTED FOR GRAASP
   *
   * Allowed end-points:
   *   /activitystreams/{userId}/{groupId}/
   *   /activitystreams/{userId}/{groupId}/{appId}/{contextType}
   *
   *
   * @param request a {@link org.apache.shindig.social.opensocial.service.SocialRequestItem} object.
   * @return a {@link java.util.concurrent.Future} object.
   * @throws org.apache.shindig.protocol.ProtocolException if any.
   */
  @Operation(httpMethods="GET")
  public Future<?> get(SocialRequestItem request)
      throws ProtocolException {

    //Set<String> fields = request.getFields(ActivityEntry.Field.DEFAULT_FIELDS);
    Set<String> contextIds = request.getContextIds();
    String contextType = request.getContextType();
    String viewerId = request.getToken().getViewerId();
    Map<String, String> params = new HashMap<String, String>();
    params.put("count", request.getParameter("count"));
    params.put("filterBy", request.getParameter("filterBy"));
    params.put("filterOp", request.getParameter("filterOp"));
    params.put("filterValue", request.getParameter("filterValue"));
    params.put("sortOrder", request.getParameter("sortOrder"));
    params.put("startIndex", request.getParameter("startIndex"));
    params.put("updatedSince", request.getParameter("updatedSince"));
    params.put("fields", request.getParameter("fields"));
    params.put("ext", request.getParameter("ext"));

    // Preconditions
    HandlerPreconditions.requireNotEmpty(contextIds, "No contextId is specified");
    
    CollectionOptions options = new CollectionOptions(request);

    if(contextIds.size() == 1){

      String output = "";
      HttpClient client = new DefaultHttpClient();

      String url = GRAASP_URL+"/rest/activitystreams/"+contextIds.iterator().next()+"/@self/"; 
      if(contextType.equals("@user")){
        url+="@app/@user";
      } else if(contextType.equals("@space")) {
        url+="@app/@space";
      } else if(contextType != null) throw new  IllegalArgumentException("Invalid context type");

      url += "/?token="+GRAASP_TOKEN+"&user="+viewerId;

      //Append parameters to the URL
      url = appendParams(url, params);
      HttpGet get = new HttpGet(url);
      get.getParams().setParameter("http.protocol.expect-continue", false);

      try {
        HttpResponse response = client.execute(get);
        BufferedReader rd = new BufferedReader(new InputStreamReader(
            response.getEntity().getContent()));
        String line = "";
        while ((line = rd.readLine()) != null) {
          output = line;
        } 
        JSONObject jsonOutput = new JSONObject(output);
        return  Futures.immediateFuture(jsonOutput);
      } catch (Exception e) {
        return  Futures.immediateFuture(e);
      }
    }else{
      throw new IllegalArgumentException("Cannot fetch activities for multiple contexts");
    }
  }

  /**
   * Not implemented.
   */
  @Operation(httpMethods = "GET", path="/@supportedFields")
  public List<Object> supportedFields(RequestItem request) {
    return null;
  }

  private String appendParams(String url, Map<String, String> params){
    String newUrl = url;
    String paramValue = "";
    String paramName = "";
    for (Map.Entry<String,String> param : params.entrySet()) {
      paramValue = param.getValue();
      paramName = param.getKey();
      if (paramValue != null && !paramValue.equals("")) {
        newUrl += "&"+paramName+"="+paramValue;
      }
    }
    return newUrl;
  }
}
