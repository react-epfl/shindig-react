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
import org.apache.shindig.common.util.FutureUtil;
import org.apache.shindig.common.util.ImmediateFuture;
import org.apache.shindig.config.ContainerConfig;
import org.apache.shindig.protocol.HandlerPreconditions;
import org.apache.shindig.protocol.Operation;
import org.apache.shindig.protocol.ProtocolException;
import org.apache.shindig.protocol.RequestItem;
import org.apache.shindig.protocol.RestfulCollection;
import org.apache.shindig.protocol.Service;
import org.apache.shindig.social.opensocial.spi.CollectionOptions;
import org.apache.shindig.social.opensocial.spi.GroupId;
import org.apache.shindig.social.opensocial.spi.Context;
import org.apache.shindig.social.opensocial.spi.ContextService;
import org.apache.shindig.social.opensocial.spi.UserId;
import org.apache.shindig.social.opensocial.spi.Context;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;

/**
 * RPC/REST handler for all /contexts requests
 */
@Service(name = "context", path = "")
public class ContextHandler {
  private final ContextService contextService;
  private final ContainerConfig config;

  @Inject
  public ContextHandler(ContextService contextService, ContainerConfig config) {
    this.contextService = contextService;
    this.config = config;
  }

  /**
   * Allowed end-points /context
   *
   * examples: /context
   */
  @Operation(httpMethods = "GET")
  public Future<?> get(SocialRequestItem request) throws ProtocolException {
    String containerUrl = System.getProperty("shindig.container_url");

    String owner = request.getToken().getOwnerId();
    String id = owner;
    String type = "@person";
    if (owner.startsWith("s_")) { // space
      type = "@space";
      id = owner.replaceFirst("s_","");
    }

    Context context = new Context(id,type);
    context.setContainerUrl(containerUrl);
    return ImmediateFuture.newInstance(context);
  }

}
