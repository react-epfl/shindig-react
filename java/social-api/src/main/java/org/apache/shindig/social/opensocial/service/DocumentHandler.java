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

import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.config.ContainerConfig;
import org.apache.shindig.protocol.HandlerPreconditions;
import org.apache.shindig.protocol.Operation;
import org.apache.shindig.protocol.ProtocolException;
import org.apache.shindig.protocol.RequestItem;
import org.apache.shindig.protocol.RestfulCollection;
import org.apache.shindig.protocol.Service;
import org.apache.shindig.social.opensocial.model.Document;
import org.apache.shindig.social.opensocial.spi.CollectionOptions;
import org.apache.shindig.social.opensocial.spi.GroupId;
import org.apache.shindig.social.opensocial.spi.DocumentId;
import org.apache.shindig.social.opensocial.spi.DocumentService;
import org.apache.shindig.social.opensocial.spi.UserId;
import org.apache.shindig.social.opensocial.spi.Context;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.common.util.concurrent.Futures;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

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

import org.json.JSONObject;
import java.io.File;
import java.io.*;


/**
 * RPC/REST handler for all /documents requests
 */
@Service(name = "documents", path = "/{contextId}+/{contextType}")
public class DocumentHandler {
  private final DocumentService documentService;
  private final ContainerConfig config;
  public static final String GRAASP_URL = System.getProperty("shindig.container_url");
  private final String GRAASP_TOKEN = System.getProperty("graasp.token");

  @Inject
  public DocumentHandler(DocumentService documentService, ContainerConfig config) {
    this.documentService = documentService;
    this.config = config;
  }

  /**
   * Allowed end-points /documents/{contextId}
   *
   * examples: /documents/1
   */
  @Operation(httpMethods="DELETE")
  public Future<?> delete(SocialRequestItem request)
      throws ProtocolException {

    // Set<UserId> userIds = request.getUsers();
    // Set<String> documentIds = ImmutableSet.copyOf(request.getListParameter("documentId"));
    // 
    // HandlerPreconditions.requireNotEmpty(userIds, "No userId specified");
    // HandlerPreconditions.requireSingular(userIds, "Multiple userIds not supported");
    // // Throws exceptions if userIds contains more than one element or zero elements
    // return documentService.deleteActivities(Iterables.getOnlyElement(userIds), request.getGroup(),
    //     request.getAppId(), documentIds, request.getToken());
        
    return  Futures.immediateFuture(null);
  }

  /**
   * Allowed end-points /documents/{contextId}
   *
   * examples: /documents/1 - postBody is a document object
   */
  @Operation(httpMethods="PUT", bodyParam = "document")
  public Future<?> update(SocialRequestItem request) throws ProtocolException {
    // return create(request);
    
    return  Futures.immediateFuture(null);
  }

  /**
   * Allowed end-points /documents/{contextId}
   *
   * examples: /documents/1 - postBody is a document object
   */
  @Operation(httpMethods="POST", bodyParam = "document")
  public Future<?> create(SocialRequestItem request) throws ProtocolException {
    
		try {
      String data = request.getParameter("document");
      JSONObject test = new JSONObject(data);
      data = test.toString();
      String viewerId = request.getToken().getViewerId();
    
      // HandlerPreconditions.requireNotEmpty(viewerId, "No viewerId is specified");
    
      String output = "";
  		HttpClient client = new DefaultHttpClient();
  		HttpPost post = new HttpPost(GRAASP_URL+"/rest/documents?token="+GRAASP_TOKEN+"&user="+viewerId);
      post.getParams().setParameter("http.protocol.expect-continue", false);

      // POST /rest/documents/23 to Graasp
		  MultipartEntity entity = new MultipartEntity();
      entity.addPart("data", new StringBody(data,"application/json", Charset.forName("UTF-8")));
      // send file body
      if (request.getFormMimePart("file") != null) {
        InputStream inputStream = request.getFormMimePart("file").getInputStream();
        File file = new File(request.getFormMimePart("file").getName());
  		  byte buf[]=new byte[1024];
        int len;
        OutputStream out=new FileOutputStream(file);
        while((len=inputStream.read(buf))>0)
          out.write(buf,0,len);
        out.close();
        inputStream.close();
  		  ContentBody cbFile = new FileBody(file, request.getFormMimePart("file").getContentType());		  
        entity.addPart("file", cbFile);
      }
      post.setEntity(entity);
      
      // return back the response
  		HttpResponse response = client.execute(post);
			BufferedReader rd = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()));
			String line = "";
			while ((line = rd.readLine()) != null) {
				output = line;
			}          
      if (request.getFormMimePart("file") != null) {
        File file = new File(request.getFormMimePart("file").getName());
        file.delete();
      }
      JSONObject jsonOutput = new JSONObject(output);
      return  Futures.immediateFuture(jsonOutput);

    } catch (Exception e) {
      return Futures.immediateFuture(e);
    }
  }

  /**
   * Allowed end-points /documents/{contextId}/{contextType} /documents/{documentId}+ 
   *
   * examples: /documents/john.doe/@person /documents/tex.group/@space /documents/mywidget
   */
  @Operation(httpMethods = "GET")
  public Future<?> get(SocialRequestItem request) throws ProtocolException {
    Set<String> fields = request.getFields(Document.Field.DEFAULT_FIELDS);
    Set<String> contextIds = request.getContextIds();
    String contextType = request.getContextType();
    String viewerId = request.getToken().getViewerId();
    String size = request.getParameter("size");

    // Preconditions
    HandlerPreconditions.requireNotEmpty(contextIds, "No contextId is specified");
    
    CollectionOptions options = new CollectionOptions(request);
    if(contextType == null){
    	// when contextType is not specified, get list of documents specified by ids
    	if(contextIds.size() == 1){
        // GET /rest/documents/23 from Graasp
        String output = "";
    		HttpClient client = new DefaultHttpClient();
    		String url = GRAASP_URL+"/rest/documents/"+contextIds.iterator().next()+"?token="+GRAASP_TOKEN+"&user="+viewerId;
    		if (size != null && !size.equals("")) {
    		  url += "&size="+size;
    		}
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
        
        // return documentService.getDocument(new DocumentId(contextIds.iterator().next()), fields, request.getToken());
    	}else{
    	    ImmutableSet.Builder<DocumentId> ids = ImmutableSet.builder();
    	    for (String id : contextIds) {
    	    	ids.add(new DocumentId(id));
    	    }
    	    Set<DocumentId> documentIds = ids.build();
    		
    		return documentService.getDocuments(documentIds, options, fields, request.getToken());
    	}
    }else{
    	// contextType is specified, get a list of documents for this context
    	if(contextIds.size() == 1){
    	  
        Context context = new Context(contextIds.iterator().next(),contextType);
        return documentService.getDocumentsForContext(context, options, fields, request.getToken());
        
    	}else{
    		throw new IllegalArgumentException("Cannot fetch documents for multiple contexts");
    	}
    }
    
  }

  @Operation(httpMethods = "GET", path="/@supportedFields")
  public List<Object> supportedFields(RequestItem request) {
    // TODO: Would be nice if name in config matched name of service.
    String container = Objects.firstNonNull(request.getToken().getContainer(), "default");
    return config.getList(container,
        "${Cur['gadgets.features'].opensocial.supportedFields.document}");
  }
}
