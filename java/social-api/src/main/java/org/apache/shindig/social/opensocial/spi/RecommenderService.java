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
import org.apache.shindig.social.opensocial.model.Recommender;

import java.util.Set;
import java.util.concurrent.Future;

/**
 * Interface that defines how shindig gathers spaces information.
 */
public interface RecommenderService {
  /**
   * Returns a list of recommended items for the context.
   *
   * @param context A context for which spaces to be returned
   * @param collectionOptions How to filter, sort and paginate the collection being fetched
   * @param token The gadget token @return a list of people.
   * @return Future that returns a RestfulCollection of Recommended items
   */
  Future<Recommender> getRecommendationsForContext(Context context,
      CollectionOptions collectionOptions, SecurityToken token)
      throws ProtocolException;
  
}
