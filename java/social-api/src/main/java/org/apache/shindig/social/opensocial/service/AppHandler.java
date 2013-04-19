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
import org.apache.shindig.social.opensocial.model.App;
import org.apache.shindig.social.opensocial.spi.CollectionOptions;
import org.apache.shindig.social.opensocial.spi.GroupId;
import org.apache.shindig.social.opensocial.spi.AppId;
import org.apache.shindig.social.opensocial.spi.AppService;
import org.apache.shindig.social.opensocial.spi.UserId;
import org.apache.shindig.social.opensocial.spi.Context;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;

/**
 * RPC/REST handler for all /apps requests
 */
@Service(name = "apps", path = "/{contextId}+/{contextType}")
public class AppHandler {
  private final AppService appService;
  private final ContainerConfig config;

  @Inject
  public AppHandler(AppService appService, ContainerConfig config) {
    this.appService = appService;
    this.config = config;
  }

  /**
   * Allowed end-points /apps/{contextId}/{contextType} /apps/{AppId}+ 
   *
   * examples: /apps/john.doe/@person /apps/tex.group/@space /apps/mywidget
   */
  @Operation(httpMethods = "GET")
  public Future<?> get(SocialRequestItem request) throws ProtocolException {
    // get key file used for token encryption
    String keyFile = config.getString("default", "gadgets.securityTokenKeyFile");

    Set<String> fields = request.getFields(App.Field.DEFAULT_FIELDS);
    Set<String> contextIds = request.getContextIds();
    String contextType = request.getContextType();

    // Preconditions
    HandlerPreconditions.requireNotEmpty(contextIds, "No contextId is specified");
    
    CollectionOptions options = new CollectionOptions(request);
    if(contextType == null){
    	// when contextType is not specified, get list of apps specified by ids
    	if(contextIds.size() == 1){
        String contextId = contextIds.iterator().next();
        if (contextId.equals("@self")) {
          // get app id from the token
          contextId = request.getToken().getAppId();
        }
    		return appService.getApp(new AppId(contextId), fields, request.getToken(), keyFile);
    	}else{
    	    ImmutableSet.Builder<AppId> ids = ImmutableSet.builder();
    	    for (String id : contextIds) {
    	    	ids.add(new AppId(id));
    	    }
    	    Set<AppId> AppIds = ids.build();
    		
    		return appService.getApps(AppIds, options, fields, request.getToken(), keyFile);
    	}
    }else{
    	// contextType is specified, get a list of apps for this context
    	if(contextIds.size() == 1){
    		Context context = new Context(contextIds.iterator().next(),contextType);
    		return appService.getAppsForContext(context, options, fields, request.getToken(), keyFile);
    	}else{
    		throw new IllegalArgumentException("Cannot fetch apps for multiple contexts");
    	}
    }
    
  }

  @Operation(httpMethods = "GET", path="/@supportedFields")
  public List<Object> supportedFields(RequestItem request) {
    // TODO: Would be nice if name in config matched name of service.
    String container = Objects.firstNonNull(request.getToken().getContainer(), "default");
    return config.getList(container,
        "${Cur['gadgets.features'].opensocial.supportedFields.app}");
  }
}
