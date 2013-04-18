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
import com.google.inject.Inject;

import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.common.util.ImmediateFuture;
import org.apache.shindig.protocol.ProtocolException;
import org.apache.shindig.protocol.RestfulCollection;
import org.apache.shindig.graaasp.jpa.spi.JPQLUtils;
import org.apache.shindig.graaasp.jpa.spi.SPIUtils;
import org.apache.shindig.social.core.model.AssetDb;
import org.apache.shindig.social.core.model.FilterCapability;
import org.apache.shindig.social.core.model.FilterSpecification;
import org.apache.shindig.social.core.model.UserDb;
import org.apache.shindig.social.core.model.SpaceDb;
import org.apache.shindig.social.opensocial.model.Document;
import org.apache.shindig.social.opensocial.spi.CollectionOptions;
import org.apache.shindig.social.opensocial.spi.GroupId;
import org.apache.shindig.social.opensocial.spi.DocumentService;
import org.apache.shindig.social.opensocial.spi.Context;
import org.apache.shindig.social.opensocial.spi.DocumentId;
import org.apache.shindig.social.opensocial.spi.UserId;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.regex.*;


import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.http.HttpServletResponse;

/**
 * Implements the PersonService from the SPI binding to the JPA model and providing queries to
 * support the OpenSocial implementation.
 */
public class DocumentServiceDb implements DocumentService {

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
  public DocumentServiceDb(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  /**
   * {@inheritDoc}
   */
  public Future<RestfulCollection<Document>> getDocuments(Set<DocumentId> documentIds, 
		  CollectionOptions collectionOptions, Set<String> fields,
       SecurityToken token) throws ProtocolException {
    // for each user id get the filtered userid using the token and then, get the users identified
    // by the group id, the final set is filtered
    // using the collectionOptions and return the fields requested.

    // not dealing with the collection options at the moment, and not the fields because they are
    // either lazy or at no extra costs, the consumer will either access the properties or not
    List<Document> plist = null;
    int lastPos = 1;
    Long totalResults = null;

    StringBuilder sb = new StringBuilder();
    // sanitize the list to get the uid's and remove duplicates
    List<String> paramList = SPIUtils.getDocumentList(documentIds);
    
    sb.append(AssetDb.JPQL_FINDDOCUMENT);
    lastPos = JPQLUtils.addInClause(sb, "a", "id", lastPos, paramList.size());

    
    // Get total results, that is count the total number of rows for this query
    // totalResults = JPQLUtils.getTotalResults(entityManager, sb.toString(), paramList);

    
    // Execute ordered and paginated query
    //if (totalResults > 0) {
    	//addOrderClause(sb, collectionOptions);
    	plist = JPQLUtils.getListQuery(entityManager, sb.toString(), paramList, collectionOptions);
    //}

    if (plist == null) {
    	plist = Lists.newArrayList();
    }
    // FIXME: use JPQLUtils.getTotalResults for it
    totalResults = new Long(plist.size());
    // all of the above could equally have been placed into a thread to overlay the
    // db wait times.
    RestfulCollection<Document> restCollection = new RestfulCollection<Document>(
        plist, collectionOptions.getFirst(), totalResults.intValue(), collectionOptions.getMax());
    return ImmediateFuture.newInstance(restCollection);

  }

  public Future<RestfulCollection<Document>> getDocumentsForContext(Context context, 
		  CollectionOptions collectionOptions, Set<String> fields,
       SecurityToken token) throws ProtocolException {
    // list of documents is retrieved for a context

    // not dealing with the collection options at the moment, and not the fields because they are
    // either lazy or at no extra costs, the consumer will either access the properties or not
    List<Document> plist = null;
    int lastPos = 1;
    Long totalResults = null;

    StringBuilder sb = new StringBuilder();
    // sanitize the list to get the uid's and remove duplicates
    List<String> paramList = Lists.newArrayList();
    
    sb.append(AssetDb.JPQL_FINDDOCUMENTS);
    if(context.getContextType().equals("@person")){
    	sb.append("a.parentId = "+context.getContextId()+" and a.parentType = 'User'");
    }else if (context.getContextType().equals("@space")){
    	sb.append("a.parentId = "+context.getContextId()+" and a.parentType = 'Space'");
    }
    
    String viewerId = token.getViewerId();
    if (!viewerCanSee(context.getContextId(),viewerId)) {
      return ImmediateFuture.newInstance(null);
    }
    
    // Get total results, that is count the total number of rows for this query
    // totalResults = JPQLUtils.getTotalResults(entityManager, sb.toString(), paramList);
    
    // Execute ordered and paginated query
    //if (totalResults > 0) {
    	//addOrderClause(sb, collectionOptions);
    	plist = JPQLUtils.getListQuery(entityManager, sb.toString(), paramList, collectionOptions);
    //}

    if (plist == null) {
    	plist = Lists.newArrayList();
    }
    // FIXME: use JPQLUtils.getTotalResults for it
    totalResults = new Long(plist.size());
    // all of the above could equally have been placed into a thread to overlay the
    // db wait times.
    RestfulCollection<Document> restCollection = new RestfulCollection<Document>(
        plist, collectionOptions.getFirst(), totalResults.intValue(), collectionOptions.getMax());
    return ImmediateFuture.newInstance(restCollection);

  }
  
  // check whether an app can be seen by a viewer
  private boolean viewerCanSee(String spaceId, String viewerId) {
    if (viewerId.equals("-1")) {
      Query q = null;
      // gets space for spaceId from the database
      q = entityManager.createNamedQuery(SpaceDb.FINDBY_SPACEID);
      q.setParameter(SpaceDb.PARAM_SPACEID, spaceId);
      q.setFirstResult(0);
      q.setMaxResults(1);
      List<?> plist = q.getResultList();
      SpaceDb s = (SpaceDb) plist.get(0);
      if (!s.getVisibilityLevel().equals("Everyone")) {
        return false;
      } else {
        return true;
      }
    } else {
      return true;
    }
  }
  

  /**
   * {@inheritDoc}
   */
  public Future<Document> getDocument(DocumentId documentId, Set<String> fields, SecurityToken token)
      throws ProtocolException {
 
	Query q = null;
	// gets document for documentId from the database
	q = entityManager.createNamedQuery(AssetDb.FINDBY_DOCUMENTID);
	q.setParameter(AssetDb.PARAM_DOCUMENTID, documentId.getDocumentId());
	q.setFirstResult(0);
	q.setMaxResults(1);
		

    List<?> plist = q.getResultList();
    Document document = null;
    AssetDb assetDb = null;
    if (plist != null && !plist.isEmpty()) {
      assetDb = (AssetDb) plist.get(0);
      document = (Document) plist.get(0);
    }
    
    String viewerId = token.getViewerId();
    if (!viewerCanSee(assetDb.getParentId(),viewerId)) {
      return ImmediateFuture.newInstance(null);
    }
    
    return ImmediateFuture.newInstance(document);
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
        if (DocumentService.HAS_APP_FILTER.equals(filter)) {
          // Retrieves all friends with any data for this document.
          // TODO: how do we determine which document is being talked about,
          // the assumption below is wrong
          filterPos = lastPos + 1;
          sb.append(" f.document_id  = ?").append(filterPos);
        } else if (DocumentService.TOP_FRIENDS_FILTER.equals(filter)) {
          // Retrieves only the user's top friends, this is defined here by the implementation
          // and there is an assumption that the sort order has already been applied.
          // to do this we need to modify the collections options
          // there will only ever b x friends in the list and it will only ever start at 1

          collectionOptions.setFirst(1);
          collectionOptions.setMax(20);

        } else if (DocumentService.ALL_FILTER.equals(filter)) {
           // select all, ie no filtering
        } else if (DocumentService.IS_WITH_FRIENDS_FILTER.equals(filter)) {
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
      if (DocumentService.TOP_FRIENDS_SORT.equals(sortBy)) {
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
