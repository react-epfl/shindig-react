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
import com.google.common.util.concurrent.Futures;
import com.google.inject.Inject;

import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.protocol.ProtocolException;
import org.apache.shindig.protocol.RestfulCollection;
import org.apache.shindig.graaasp.jpa.spi.JPQLUtils;
import org.apache.shindig.graaasp.jpa.spi.SPIUtils;
import org.apache.shindig.social.core.model.FilterCapability;
import org.apache.shindig.social.core.model.FilterSpecification;
import org.apache.shindig.social.core.model.SpaceDb;
import org.apache.shindig.social.core.model.UserDb;
import org.apache.shindig.social.opensocial.model.Space;
import org.apache.shindig.social.opensocial.spi.CollectionOptions;
import org.apache.shindig.social.opensocial.spi.GroupId;
import org.apache.shindig.social.opensocial.spi.SpaceService;
import org.apache.shindig.social.opensocial.spi.Context;
import org.apache.shindig.social.opensocial.spi.SpaceId;
import org.apache.shindig.social.opensocial.spi.UserId;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.regex.*;


import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.http.HttpServletResponse;

/**
 * Implements the SpaceService from the SPI binding to the JPA model and providing queries to
 * support the OpenSocial implementation.
 */
public class SpaceServiceDb implements SpaceService {

  /**
   * This is the JPA entity manager, shared by all threads accessing this service (need to check
   * that its really thread safe).
   */
  private EntityManager entityManager;

  /**
   * Create the PersonServiceDb, injecting an entity manager that is configured with the social
   * model.
   *
   * @param entityManager the entity manager containing the social model.
   */
  @Inject
  public SpaceServiceDb(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  /**
   * {@inheritDoc}
   */
  public Future<RestfulCollection<Space>> getSpaces(Set<SpaceId> spaceIds, 
		  CollectionOptions collectionOptions, Set<String> fields,
       SecurityToken token) throws ProtocolException {
         
     // Hack to get all people in the system
     if (spaceIds.iterator().next().getSpaceId().equals("@all")) {
       return getAllPublicSpaces();
     }
         
    // for each user id get the filtered userid using the token and then, get the users identified
    // by the group id, the final set is filtered
    // using the collectionOptions and return the fields requested.

    // not dealing with the collection options at the moment, and not the fields because they are
    // either lazy or at no extra costs, the consumer will either access the properties or not
    List<SpaceDb> dblist = null;
    int lastPos = 1;
    Long totalResults = null;

    StringBuilder sb = new StringBuilder();
    // sanitize the list to get the uid's and remove duplicates
    List<String> paramList = SPIUtils.getSpaceList(spaceIds);
    
    sb.append(SpaceDb.JPQL_FINDSPACE);
    lastPos = JPQLUtils.addInClause(sb, "s", "id", lastPos, paramList.size());

    
    // Get total results, that is count the total number of rows for this query
    // totalResults = JPQLUtils.getTotalResults(entityManager, sb.toString(), paramList);

    
    // Execute ordered and paginated query
    //if (totalResults > 0) {
    	//addOrderClause(sb, collectionOptions);
    	dblist = JPQLUtils.getListQuery(entityManager, sb.toString(), paramList, collectionOptions);
    //}

    String viewerId = token.getViewerId();
    List<Space> plist = filterSpaces(dblist,viewerId);
    
    if (plist == null) {
    	plist = Lists.newArrayList();
    }
    // FIXME: use JPQLUtils.getTotalResults for it
    totalResults = new Long(plist.size());
    // all of the above could equally have been placed into a thread to overlay the
    // db wait times.
    RestfulCollection<Space> restCollection = new RestfulCollection<Space>(
        plist, collectionOptions.getFirst(), totalResults.intValue(), collectionOptions.getMax());
    return Futures.immediateFuture(restCollection);

  }
  
  /**
   * Get all public spaces in the system
   */
  public Future<RestfulCollection<Space>> getAllPublicSpaces() throws ProtocolException {
    StringBuilder sb = new StringBuilder();
    sb.append(SpaceDb.JPQL_ALL_PUBLIC_SPACES);
    List<String> paramList = Lists.newArrayList();
    List<Space> plist = null;
    plist = JPQLUtils.getListQuery(entityManager, sb.toString(), paramList, null);

    if (plist == null) {
      plist = Lists.newArrayList();
    }
    // FIXME: use JPQLUtils.getTotalResults for it
    // all of the above could equally have been placed into a thread to overlay the
    // db wait times.
    RestfulCollection<Space> restCollection = new RestfulCollection<Space>(plist);
    return Futures.immediateFuture(restCollection);
  }
  

  public Future<RestfulCollection<Space>> getSpacesForContext(Context context, 
      CollectionOptions collectionOptions, Set<String> fields,
       SecurityToken token) throws ProtocolException {
    // list of spaces is retrieved for a context

    // not dealing with the collection options at the moment, and not the fields because they are
    // either lazy or at no extra costs, the consumer will either access the properties or not
    List<SpaceDb> dblist = null;
    int lastPos = 1;
    Long totalResults = null;

    StringBuilder sb = new StringBuilder();
    // sanitize the list to get the uid's and remove duplicates
    List<String> paramList = Lists.newArrayList();
    
    sb.append(SpaceDb.JPQL_FINDSPACES);
    if(context.getContextType().equals("@person")){
    	sb.append("s.parentId = "+context.getContextId()+" and s.parentType = 'User'");
    }else if (context.getContextType().equals("@space")){
    	sb.append("s.parentId = "+context.getContextId()+" and s.parentType = 'Space'");
    }
    
    // Get total results, that is count the total number of rows for this query
    // totalResults = JPQLUtils.getTotalResults(entityManager, sb.toString(), paramList);
    
    // Execute ordered and paginated query
    //if (totalResults > 0) {
    	//addOrderClause(sb, collectionOptions);
    	dblist = JPQLUtils.getListQuery(entityManager, sb.toString(), paramList, collectionOptions);
    //}
    
    String viewerId = token.getViewerId();
    List<Space> plist = filterSpaces(dblist,viewerId);

    if (plist == null) {
    	plist = Lists.newArrayList();
    }
    // FIXME: use JPQLUtils.getTotalResults for it
    totalResults = new Long(plist.size());
    // all of the above could equally have been placed into a thread to overlay the
    // db wait times.
    RestfulCollection<Space> restCollection = new RestfulCollection<Space>(
        plist, collectionOptions.getFirst(), totalResults.intValue(), collectionOptions.getMax());
    return Futures.immediateFuture(restCollection);

  }

  // hack: for direct rest request filter space and don't do it if viewer id is specified
  // later a check if viewerId has enough rights should be done
  // 
  // removes the spaces that are not allowed to be seen
  private List<Space> filterSpaces(List<SpaceDb> list, String viewerId) {
    List<Space> plist = Lists.newArrayList();
    if (viewerId.equals("-1")) {
      for (SpaceDb spaceDb : list) {

        String vis = spaceDb.getVisibilityLevel();

        if (vis.equals("Space members")) {
          SpaceDb s = new SpaceDb();
          s.setId(spaceDb.getId());
          s.setDisplayName(spaceDb.getDisplayName());
          s.setParentId(spaceDb.getParentId());
          s.setParentType(spaceDb.getParentType());
          s.setProfileUrl(spaceDb.getProfileUrl());
          s.setVisibilityLevel(spaceDb.getVisibilityLevel());

          plist.add((Space) s);
        }
        if (vis.equals("Everyone")) {
          plist.add((Space) spaceDb);
        }
      }
    } else {
      for (SpaceDb spaceDb : list) {
        plist.add((Space) spaceDb);
      }
    }
    
    return plist;
  }
  /**
   * {@inheritDoc}
   */
  public Future<Space> getSpace(SpaceId spaceId, Set<String> fields, SecurityToken token)
      throws ProtocolException {
 
    Query q = null;
    // gets space for spaceId from the database
    q = entityManager.createNamedQuery(SpaceDb.FINDBY_SPACEID);
    q.setParameter(SpaceDb.PARAM_SPACEID, spaceId.getSpaceId());
    q.setFirstResult(0);
    q.setMaxResults(1);
 

    List<?> plist = q.getResultList();
    Space space = null;
    SpaceDb spaceDb = null;
    if (plist != null && !plist.isEmpty()) {
      spaceDb = (SpaceDb) plist.get(0);
      space = (Space) plist.get(0);
      String vis = spaceDb.getVisibilityLevel();
      
      // filter out hidden spaces and filter out params of closed spaces
      if (vis.equals("Myself")) {
        return Futures.immediateFuture(null);
      }
      if (vis.equals("Space members")) {
        SpaceDb s = new SpaceDb();
        s.setId(spaceDb.getId());
        s.setDisplayName(spaceDb.getDisplayName());
        s.setParentId(spaceDb.getParentId());
        s.setParentType(spaceDb.getParentType());
        s.setProfileUrl(spaceDb.getProfileUrl());
        s.setVisibilityLevel(spaceDb.getVisibilityLevel());
        return Futures.immediateFuture((Space) s);
      }
      
    }
    return Futures.immediateFuture(space);
  }



  /**
   * Add a filter clause specified by the collection options.
   *
   * @param sb the query builder buffer
   * @param collectionOptions the options
   * @param lastPos the last positional parameter that was used so far in the query
   * @return
   */
  private int addFilterClause(StringBuilder sb, FilterCapability filterable,
      CollectionOptions collectionOptions, int lastPos) {
    // this makes the filter value saf
    String filter = filterable.findFilterableProperty(collectionOptions.getFilter(),
        collectionOptions.getFilterOperation());
    String filterValue = collectionOptions.getFilterValue();
    int filterPos = 0;
    if (FilterSpecification.isValid(filter)) {
      if (FilterSpecification.isSpecial(filter)) {
        if (SpaceService.HAS_APP_FILTER.equals(filter)) {
          // Retrieves all friends with any data for this app.
          // TODO: how do we determine which app is being talked about,
          // the assumption below is wrong
          filterPos = lastPos + 1;
          sb.append(" f.app_id  = ?").append(filterPos);
        } else if (SpaceService.TOP_FRIENDS_FILTER.equals(filter)) {
          // Retrieves only the user's top friends, this is defined here by the implementation
          // and there is an assumption that the sort order has already been applied.
          // to do this we need to modify the collections options
          // there will only ever b x friends in the list and it will only ever start at 1

          collectionOptions.setFirst(1);
          collectionOptions.setMax(20);

        } else if (SpaceService.ALL_FILTER.equals(filter)) {
           // select all, ie no filtering
        } else if (SpaceService.IS_WITH_FRIENDS_FILTER.equals(filter)) {
          filterPos = lastPos + 1;
          sb.append(" f.friend  = ?").append(filterPos);
        }
      } else {
        sb.append("p.").append(filter);
        switch (collectionOptions.getFilterOperation()) {
        case contains:
          filterPos = lastPos + 1;
          sb.append(" like ").append(" ?").append(filterPos);
          filterValue = '%' + filterValue + '%';
          collectionOptions.setFilter(filterValue);
          break;
        case equals:
          filterPos = lastPos + 1;
          sb.append(" = ").append(" ?").append(filterPos);
          break;
        case present:
          sb.append(" is not null ");
          break;
        case startsWith:
          filterPos = lastPos + 1;
          sb.append(" like ").append(" ?").append(filterPos);
          filterValue = '%' + filterValue + '%';
          collectionOptions.setFilter(filterValue);
          break;
        }
      }
    }
    return filterPos;
  }

  /**
   * Add an order clause to the query string.
   *
   * @param sb the buffer for the query string
   * @param collectionOptions the options to use for the order.
   */
  private void addOrderClause(StringBuilder sb, CollectionOptions collectionOptions) {
    String sortBy = collectionOptions.getSortBy();
    if (sortBy != null && sortBy.length() > 0) {
      if (SpaceService.TOP_FRIENDS_SORT.equals(sortBy)) {
        // TODO sorting by friend.score doesn't work right now because of group by issue (see above TODO)
        // this assumes that the query is a join with the friends store.
        sb.append(" order by f.score ");
      } else {
        if ("name".equals(sortBy)) {
          // TODO Is this correct?
          // If sortBy is name then order by p.name.familyName, p.name.givenName.
          sb.append(" order by p.name.familyName, p.name.givenName ");
        } else {
          sb.append(" order by p.").append(sortBy);
        }
        switch (collectionOptions.getSortOrder()) {
        case ascending:
          sb.append(" asc ");
          break;
        case descending:
          sb.append(" desc ");
          break;
        }
      }
    }
  }
}
