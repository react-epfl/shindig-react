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
import org.apache.shindig.social.opensocial.spi.UserId.Type;

import com.google.common.collect.ImmutableMap;
import com.google.common.base.Objects;

import java.util.Map;

/**
 * Data structure representing a context
 */
public class Context {

  private String contextId;
  private String contextType;
  private String containerUrl;

  public Context(String contextId, String contextType) {
    this.contextId = contextId;
    this.contextType = contextType;
  }

  public String getContextId() {
    return contextId;
  }

  public String getContextType() {
    return contextType;
  }

  public String getContainerUrl() {
    return containerUrl;
  }

  public void setContainerUrl(String containerUrl) {
    this.containerUrl = containerUrl;
  }

  public static UserId fromJson(String jsonId) {
	  Type idSpecEnum = Type.jsonValueOf(jsonId);
	  if (idSpecEnum != null) {
		  return new UserId(idSpecEnum, null);
	  }

	  return new UserId(Type.userId, jsonId);
  }


}
