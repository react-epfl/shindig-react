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
package org.apache.shindig.social.opensocial.service;

import org.apache.shindig.common.EasyMockTestCase;
import org.apache.shindig.common.testing.FakeGadgetToken;
import org.apache.shindig.config.ContainerConfig;
import org.apache.shindig.config.JsonContainerConfig;
import org.apache.shindig.expressions.Expressions;
import org.apache.shindig.protocol.DefaultHandlerRegistry;
import org.apache.shindig.protocol.HandlerExecutionListener;
import org.apache.shindig.protocol.HandlerRegistry;
import org.apache.shindig.protocol.ProtocolException;
import org.apache.shindig.protocol.RestHandler;
import org.apache.shindig.protocol.RestfulCollection;
import org.apache.shindig.protocol.conversion.BeanJsonConverter;
import org.apache.shindig.social.opensocial.model.Activity;
import org.apache.shindig.social.opensocial.spi.ActivityService;
import org.apache.shindig.social.opensocial.spi.CollectionOptions;
import org.apache.shindig.social.opensocial.spi.GroupId;
import org.apache.shindig.social.opensocial.spi.UserId;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.Futures;

import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.isNull;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.io.StringReader;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

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

import java.io.File;

public class DocumentHandlerTest extends EasyMockTestCase {

  private BeanJsonConverter converter;

  private ActivityService activityService;

  private ActivityHandler handler;

  private FakeGadgetToken token;

  private static final Set<UserId> JOHN_DOE =
      ImmutableSet.of(new UserId(UserId.Type.userId, "john.doe"));

  protected HandlerRegistry registry;
  protected ContainerConfig containerConfig;

  @Before
  public void setUp() throws Exception {
    token = new FakeGadgetToken();
    token.setAppId("appId");

    converter = mock(BeanJsonConverter.class);
    activityService = mock(ActivityService.class);

    JSONObject config = new JSONObject('{' + ContainerConfig.DEFAULT_CONTAINER + ':' +
        "{'gadgets.container': ['default']," +
         "'gadgets.features':{opensocial:" +
           "{supportedFields: {activity: ['id', 'title']}}" +
         "}}}");

    containerConfig = new JsonContainerConfig(config, Expressions.forTesting());
    handler = new ActivityHandler(activityService, containerConfig);
    registry = new DefaultHandlerRegistry(null, converter,
        new HandlerExecutionListener.NoOpHandler());
    registry.addHandlers(ImmutableSet.<Object>of(handler));
  }

  private void assertHandleGetForGroup(GroupId.Type group) throws Exception {
    String path = "/activities/john.doe/@" + group.toString();
    RestHandler operation = registry.getRestHandler(path, "GET");

    List<Activity> activityList = ImmutableList.of();
    RestfulCollection<Activity> data = new RestfulCollection<Activity>(activityList);
    org.easymock.EasyMock.expect(activityService.getActivities(eq(JOHN_DOE),
       eq(new GroupId(group, null)), (String)isNull(), eq(ImmutableSet.<String>of()),
        org.easymock.EasyMock.isA(CollectionOptions.class), eq(token))).
        andReturn(Futures.immediateFuture(data));

    replay();
    assertEquals(data, operation.execute(Maps.<String, String[]>newHashMap(),
        null, token, converter).get());
    verify();
    reset();
  }

  @Test
  public void testHandleGetPlural() throws Exception {
    String path = "/activities/john.doe,jane.doe/@self/@app";
    RestHandler operation = registry.getRestHandler(path, "GET");

    // JSONArray people = db.getDb().getJSONArray("people");
    JSONObject jsonPerson = new JSONObject();
    jsonPerson.put("id", "updatePerson");
    // people.put(people.length(),jsonPerson);

    JSONObject test = new JSONObject("{aboutMe:\"test\",displayName:\"Evgeny\",thumbnailUrl:\"httpaboutme\"}");
    System.out.println(test.toString());


    System.out.println(jsonPerson);

    // URL oracle = new URL("http://localhost:8080/rest/spaces/6");
    //   BufferedReader in = new BufferedReader(
    //         new InputStreamReader(
    //         oracle.openStream()));
    //
    //   String inputLine;
    //   String s = "";
    //   while ((inputLine = in.readLine()) != null)
    //       s += inputLine;
    //
    //   in.close();
    //
    // System.out.println(s);
    //
    // Future f = Futures.immediateFuture(s);
    // System.out.println(f);

    String data = "";
    HttpClient client = new DefaultHttpClient();
    HttpPost post = new HttpPost("http://reacttest.epfl.ch/rest/documents/23");
    // HttpPost post = new HttpPost("http://localhost:3000/rest/documents/23?test=hey");
    try {
      post.getParams().setParameter("http.protocol.expect-continue", false);
      // post.setHeader("Accept", "application/json");
      // post.removeHeaders("Expect");
      // post.removeHeaders("expect");
      // post.setHeader("Expect", "100");
       // List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
       // nameValuePairs.add(new BasicNameValuePair("registrationid",
       //    "123456789"));
       // post.setEntity(new UrlEncodedFormEntity(nameValuePairs));

      // File file = new File("c:/TRASH/zaba_1.jpg");
      //
      // MultipartEntity mpEntity = new MultipartEntity();
      // ContentBody cbFile = new FileBody(file, "image/jpeg");
      // mpEntity.addPart("userfile", cbFile);
      //
      //
      // post.setEntity(mpEntity);
      MultipartEntity entity = new MultipartEntity();
      entity.addPart("title", new StringBody("mytitle","text/plain", Charset.forName("UTF-8")));
      entity.addPart("desc", new StringBody("mydesc","text/plain",  Charset.forName("UTF-8")));
      // File f = new File("test.txt");
      // if(!f.exists()){
      //   f.createNewFile();
      // }
      //
      // FileBody fileBody = new FileBody(f);
      // entity.addPart("file", fileBody);
      post.setEntity(entity);

      HttpResponse response = client.execute(post);
      BufferedReader rd = new BufferedReader(new InputStreamReader(
          response.getEntity().getContent()));
      String line = "";
      while ((line = rd.readLine()) != null) {
        System.out.println(line);
      }

    } catch (IOException e) {
      e.printStackTrace();
    }
    System.out.println(data);

    // List<Activity> activities = ImmutableList.of();
    // RestfulCollection<Activity> data = new RestfulCollection<Activity>(activities);
    // Set<UserId> userIdSet = Sets.newLinkedHashSet(JOHN_DOE);
    // userIdSet.add(new UserId(UserId.Type.userId, "jane.doe"));
    // org.easymock.EasyMock.expect(activityService.getActivities(eq(userIdSet),
    //     eq(new GroupId(GroupId.Type.self, null)), eq("appId"),eq(ImmutableSet.<String>of()),
    //     org.easymock.EasyMock.isA((CollectionOptions.class)), eq(token))).andReturn(
    //       Futures.immediateFuture(data));

    replay();
    // assertEquals(data, operation.execute(Maps.<String, String[]>newHashMap(),
    //     null, token, converter).get());
    verify();
    reset();
  }
}
