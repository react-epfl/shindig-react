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

import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.servlet.http.HttpServletResponse;

import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.protocol.ProtocolException;
import org.apache.shindig.protocol.RestfulCollection;
import org.apache.shindig.protocol.DataCollection;
import org.apache.shindig.graaasp.jpa.spi.JPQLUtils;
import org.apache.shindig.graaasp.jpa.spi.SPIUtils;
import org.apache.shindig.social.core.model.FilterCapability;
import org.apache.shindig.social.core.model.FilterSpecification;
import org.apache.shindig.social.core.model.PermissionDb;
import org.apache.shindig.social.core.model.SpaceDb;
import org.apache.shindig.social.core.model.UserDb;
import org.apache.shindig.social.opensocial.model.Person;
import org.apache.shindig.social.opensocial.spi.CollectionOptions;
import org.apache.shindig.social.opensocial.spi.GroupId;
import org.apache.shindig.social.opensocial.spi.PersonService;
import org.apache.shindig.social.opensocial.spi.UserId;
import org.apache.shindig.social.opensocial.spi.Context;

import org.apache.shindig.social.opensocial.model.Competence;
import org.apache.shindig.social.opensocial.model.ListField;
import org.apache.shindig.social.opensocial.model.Address;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import com.google.inject.Inject;

/**
 * Implements the PersonService from the SPI binding to the JPA model and providing queries to
 * support the OpenSocial implementation.
 */
public class PersonServiceDb implements PersonService {

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
    public PersonServiceDb(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * {@inheritDoc}
     */
    public Future<RestfulCollection<Person>> getPeople(Set<UserId> userIds,
         GroupId groupId, CollectionOptions collectionOptions, Set<String> fields,
         SecurityToken token) throws ProtocolException {

        // Hack to get all people in the system
        if (userIds.iterator().next().getUserId().equals("@all")) {
            return getAllPublicPeople();
        }

    // for each user id get the filtered userid using the token and then, get the users identified
    // by the group id, the final set is filtered
    // using the collectionOptions and return the fields requested.

    // not dealing with the collection options at the moment, and not the fields because they are
    // either lazy or at no extra costs, the consumer will either access the properties or not
    List<Person> plist = null;
    int lastPos = 1;
    Long totalResults = null;

    StringBuilder sb = new StringBuilder();
    // sanitize the list to get the uid's and remove duplicates
    List<String> paramList = SPIUtils.getUserList(userIds, token);
    // select the group Id as this will drive the query
    switch (groupId.getType()) {
    case all:
      // select all contacts
      sb.append(UserDb.JPQL_FINDALLPERSON);
      lastPos = JPQLUtils.addInClause(sb, "p", "id", lastPos, paramList.size());
      break;
    case friends:
      // select all friends (subset of contacts)
      sb.append(UserDb.JPQL_FINDPERSON_BY_FRIENDS);
      lastPos = JPQLUtils.addInClause(sb, "p", "id", lastPos, paramList.size());
      sb.append(") ");
      // TODO Group by doesn't work in HSQLDB or Derby - causes a "Not in aggregate function or group by clause" jdbc exception
      // sb.append(" group by p ");
      break;
    case objectId:
// TODO: implementation is required to support the requests implemented for graaasp
//        switch ($group_id) {
//       case '@spaces':
//         $ret = $this->getPersonSpacesIds($user_id);
//         break;
//       case '@people':
//         $ret = $this->getSpacePeopleIds($user_id);
//         break;
//       case '@administrators':
//         $ret = $this->getAdministratorsIds($user_id);
//         break;
//       case '@members':
//         $ret = $this->getMembersIds($user_id);
//         break;

      // select those in the group
      sb.append(UserDb.JPQL_FINDPERSON_BY_GROUP);
      lastPos = JPQLUtils.addInClause(sb, "p", "id", lastPos, paramList.size());
      sb.append(" and g.id = ?").append(lastPos);
      lastPos++;
      break;
    case self:
      // select self
      sb.append(UserDb.JPQL_FINDPERSON);
      lastPos = JPQLUtils.addInClause(sb, "p", "id", lastPos, paramList.size());
      break;
    default:
      throw new ProtocolException(HttpServletResponse.SC_BAD_REQUEST, "Group ID not recognized");

        }

    if (GroupId.Type.self.equals(groupId.getType())) {
      plist = JPQLUtils.getListQuery(entityManager, sb.toString(), paramList, collectionOptions);
      totalResults = Long.valueOf(1);
      if (plist.isEmpty()) {
        throw new ProtocolException(HttpServletResponse.SC_BAD_REQUEST, "Person not found");
      }
    } else {
      int filterPos = addFilterClause(sb, UserDb.getFilterCapability(), collectionOptions,
          lastPos);
      if (filterPos > 0) {
        paramList.add(collectionOptions.getFilterValue());
      }

      // Get total results, that is count the total number of rows for this query
      totalResults = JPQLUtils.getTotalResults(entityManager, sb.toString(), paramList);

      // Execute ordered and paginated query
      if (totalResults > 0) {
        addOrderClause(sb, collectionOptions);
        plist = JPQLUtils.getListQuery(entityManager, sb.toString(), paramList, collectionOptions);
      }

      if (plist == null) {
        plist = Lists.newArrayList();
      }
    }

    // all of the above could equally have been placed into a thread to overlay the
    // db wait times.
    RestfulCollection<Person> restCollection = new RestfulCollection<Person>(
        plist, collectionOptions.getFirst(), totalResults.intValue(), collectionOptions.getMax());
    return Futures.immediateFuture(restCollection);

  }

    /**
     * Get all public people in the system
     * TODO: added for graasp, check it
     */
    public Future<RestfulCollection<Person>> getAllPublicPeople() throws ProtocolException {
        StringBuilder sb = new StringBuilder();
        sb.append(UserDb.JPQL_ALL_PUBLIC_PEOPLE);
        List<String> paramList = Lists.newArrayList();
        List<Person> plist = null;
        plist = JPQLUtils.getListQuery(entityManager, sb.toString(), paramList, null);

        if (plist == null) {
            plist = Lists.newArrayList();
        }
        // FIXME: use JPQLUtils.getTotalResults for it
        // all of the above could equally have been placed into a thread to overlay the
        // db wait times.
        RestfulCollection<Person> restCollection = new RestfulCollection<Person>(plist);
        return Futures.immediateFuture(restCollection);
    }

    /**
     * {@inheritDoc}
     * TODO: changed for graasp, check it
     */
    public Future<Person> getPerson(UserId id, Set<String> fields, SecurityToken token)
            throws ProtocolException {
        String uid = id.getUserId(token);
        Query q = null;

        if (uid.startsWith("s_")) {
            // Current implementation to support owner for spaces
            // Takes space as context and viewer from security token
            String spaceId = uid.replaceFirst("s_","");
            String viewerId = token.getViewerId();

            // Set<String> owners =
            // get space owners
            StringBuilder sb = new StringBuilder();
            sb.append(PermissionDb.JPQL_OWNERS_BY_SPACE);
            sb.append(" and p.itemId = ").append(spaceId);
            List<String> owners = JPQLUtils.getListQuery(entityManager, sb.toString(),
                    Lists.newArrayList(), null);

            // If viewer is in the list of owners for a space it returns him,
            // otherwise returns first user from owners list.
            if (owners.size() == 0) {
                throw new ProtocolException(HttpServletResponse.SC_NOT_FOUND ,"space does not have owners!");
            }

            uid = owners.get(0);
            for (String owner : owners) {
                if (owner.equals(viewerId)) {
                    uid = viewerId; // viewer is the owner, set it
                    break;
                }
            }
        }

    // uid is set up now: either user-owner or owner of a space
    q = entityManager.createNamedQuery(UserDb.FINDBY_USERID);
    q.setParameter(UserDb.PARAM_USERID, uid);
    q.setFirstResult(0);
    q.setMaxResults(1);

    List<?> plist = q.getResultList();
    Person person = null;
    if (plist != null && !plist.isEmpty()) {
      person = (Person) plist.get(0);
    }
    return Futures.immediateFuture(person);
  }

    /**
    * Retrieves a list of people for a context
    * TODO: added for graasp, check it 
    */
    public Future<RestfulCollection<Person>> getPeopleForContext(Context context,
        CollectionOptions collectionOptions, Set<String> fields, SecurityToken token)
        throws ProtocolException {

        // not dealing with the collection options at the moment, and not the fields because they are
        // either lazy or at no extra costs, the consumer will either access the properties or not
        List<Person> plist = null;
        int lastPos = 1;
        Long totalResults = null;

        StringBuilder sb = new StringBuilder();
        // sanitize the list to get the uid's and remove duplicates
        List<String> paramList = Lists.newArrayList();

        // finds people connected (in Graaasp sense) to other people
        // or spaces
        sb.append(UserDb.JPQL_FINDPERSON_BY_PERMISSIONS);
        // add permission to request
        if(context.getContextType().equals("@person")){
            sb.append("p.itemId = "+context.getContextId()+" and p.itemType = 'User')");
        }else if (context.getContextType().equals("@space")){
            sb.append("p.itemId = "+context.getContextId()+" and p.itemType = 'Space')");
        }
        // sb.append(UserDb.JPQL_PERMISSIONS);
        // sb.append("p.itemId = "+context.getContextId()+" and p.itemType = 'Space'");

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
        RestfulCollection<Person> restCollection = new RestfulCollection<Person>(
                plist, collectionOptions.getFirst(), totalResults.intValue(), collectionOptions.getMax());
        return Futures.immediateFuture(restCollection);
    }

    /**
     * {@inheritDoc}
     * TODO: changed for graasp, check it
     */
    public Future<Person> updatePerson(UserId id, Person person, SecurityToken token)
        throws ProtocolException {

        String viewerId = token.getViewerId();
        String uid = id.getUserId(token);

        // if (!viewerId.equals(uid)) {
        //   throw new IllegalArgumentException("Viewer can change only his own profile");
        // }

        Query q = null;
        // Get the person object from db
        q = entityManager.createNamedQuery(UserDb.FINDBY_USERID);
        q.setParameter(UserDb.PARAM_USERID, viewerId);
        q.setFirstResult(0);
        q.setMaxResults(1);

        List<?> plist = q.getResultList();
        UserDb user = null;
        if (plist != null && !plist.isEmpty()) {
            user = (UserDb) plist.get(0);
            // update user's fields: displayName, aboutMe, age
            // add fields that has to be updated
            // user.setThumbnailUrl(person.getThumbnailUrl());
        }

        // TODO How should transactions be managed? Should samples be using warp-persist instead?
        if (!entityManager.getTransaction().isActive()) {
            entityManager.getTransaction().begin();
        }
        // update person object
        if (person.getName() != null) {
            if (user.getName() != null) {
                entityManager.remove(user.getName());
            }
            user.setName(person.getName());
        }
        if (person.getEmails() != null) {
            for (Object e : user.getEmails()) {
                entityManager.remove(e);
            }
            List<ListField> emails = Lists.newArrayList();
            for (ListField c : person.getEmails()) {
                c.setPerson(user);
                emails.add(c);
            }
            user.setEmails(emails);
        }
        if (person.getCompetences() != null) {
            for (Object e : user.getCompetences()) {
                entityManager.remove(e);
            }
            List<Competence> competences = Lists.newArrayList();
            for (Competence c : person.getCompetences()) {
                c.setPerson(user);
                competences.add(c);
            }
            user.setCompetences(competences);
        }
        if (person.getAddresses() != null) {
            for (Object e : user.getAddresses()) {
                entityManager.remove(e);
            }
            List<Address> addresses = Lists.newArrayList();
            for (Address c : person.getAddresses()) {
                c.setPerson(user);
                addresses.add(c);
            }
            user.setAddresses(addresses);
        }

        entityManager.persist(user);
        entityManager.getTransaction().commit();

        // send user data back
        return Futures.immediateFuture((Person) user);
    }

  /** Check if a viewer is allowed to update the given person record. **/
  protected boolean viewerCanUpdatePerson(String viewer, String person) {
    // A person can only update his own personal data (by default)
    // if you wish to allow other people to update the personal data of the user
    // you should change the current function
    return viewer.equals(person) ? true : false;
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
        if (PersonService.HAS_APP_FILTER.equals(filter)) {
          // Retrieves all friends with any data for this application.
          // TODO: how do we determine which application is being talked about,
          // the assumption below is wrong
          filterPos = lastPos + 1;
          sb.append(" f.application_id  = ?").append(filterPos);
        } else if (PersonService.TOP_FRIENDS_FILTER.equals(filter)) {
          // Retrieves only the user's top friends, this is defined here by the implementation
          // and there is an assumption that the sort order has already been applied.
          // to do this we need to modify the collections options
          // there will only ever b x friends in the list and it will only ever start at 1

          collectionOptions.setFirst(1);
          collectionOptions.setMax(20);

        } else if (PersonService.ALL_FILTER.equals(filter)) {
           // select all, ie no filtering
        } else if (PersonService.IS_WITH_FRIENDS_FILTER.equals(filter)) {
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
      if (PersonService.TOP_FRIENDS_SORT.equals(sortBy)) {
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
