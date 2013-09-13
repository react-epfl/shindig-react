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
package org.apache.shindig.social.opensocial.model;

import org.apache.shindig.protocol.model.Enum;
import org.apache.shindig.protocol.model.Exportablebean;
//import org.apache.shindig.social.core.model.AppImpl;

import com.google.common.base.Functions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.inject.ImplementedBy;

import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * see <a href="http://www.opensocial.org/Technical-Resources/opensocial-spec-v081/opensocial-reference#opensocial.App.Field">
 * http://www.opensocial.org/Technical-Resources/opensocial-spec-v081/opensocial-reference#opensocial.App.Field</a>
 * for all field meanings. All fields are represented in the js api at this time except for lastUpdated.
 * This field is currently only in the RESTful spec.
 *
 */
//@ImplementedBy(AppImpl.class)
@Exportablebean
public interface App {
  /**
   * The type of a profile url when represented as a list field.
   */
  String PROFILE_URL_TYPE = "profile";

  /**
   * The type of thumbnail photo types when represented as list fields.
   */
  String THUMBNAIL_PHOTO_TYPE = "thumbnail";

  /**
   * The display name for the application.
   * @return the display name
   */
  String getDisplayName();

  /**
   * Set the display name.
   * @param displayName the new display name.
   */
  void setDisplayName(String displayName);

  /**
   * The fields that represent the application object in json form.
   */
  public static enum Field {
    /** the json field for appData. */
    APP_DATA("appData"),
    /** the json field for appType. */
    APP_TYPE("appType"),
    /** the json field for appUrl. */
    APP_URL("appUrl"),
    /** the json field for author. */
    AUTHOR("author"),
    /** the json field for authorEmail. */
    AUTHOR_EMAIL("authorEmail"),
    /** the json field for parentId. */
    PARENT_ID("parentId"),
    /** the json field for parentType. */
    PARENT_TYPE("parentType"),
    /** the json field for description. */
    DESCRIPTION("description"),
    /** the json field for display name. */
    DISPLAY_NAME("displayName"), /** Needed to support the RESTful api. */
    /** the json field for height. */
    HEIGHT("height"),
    /** the json field for id. */
    ID("id"),
    /** the json field for IM accounts. */
    IMS("ims"),
    /** the json field for name. */
    NAME("name"),
    /** the json field for screenshotUrl. */
    SCREENSHOT_URL("screenshotUrl"),
    /** the json field for tags. */
    TAGS("tags"),
    /** the json field for thumbnailUrl. */
    THUMBNAIL_URL("thumbnailUrl"),
    /** the json field for utcOffset. */
    UTC_OFFSET("utcOffset");

    /**
     * a Map to convert json string to Field representations.
     */

    private static final Map<String,Field> LOOKUP = Maps.uniqueIndex(EnumSet.allOf(Field.class), 
        Functions.toStringFunction());

    /**
     * The json field that the instance represents.
     */
    private final String urlString;

    /**
     * The set of all fields.
     */
    public static final Set<String> ALL_FIELDS = LOOKUP.keySet();

    /**
     * The set of default fields returned fields.
     */
    public static final Set<String> DEFAULT_FIELDS = ImmutableSet.of(
        ID.toString(),
        NAME.toString(),           
        APP_URL.toString(), 
		PARENT_ID.toString(),
		PARENT_TYPE.toString(),
        THUMBNAIL_URL.toString());

    /**
     * create a field base on the a json element.
     *
     * @param urlString the name of the element
     */
    private Field(String urlString) {
      this.urlString = urlString;
    }

    /**
     * emit the field as a json element.
     *
     * @return the field name
     */
    @Override
    public String toString() {
      return this.urlString;
    }

    public static Field getField(String jsonString) {
      return LOOKUP.get(jsonString);
    }

    /**
     * Converts from a url string (usually passed in the fields= parameter) into the
     * corresponding field enum.
     * @param urlString The string to translate.
     * @return The corresponding application field.
     */
    public static App.Field fromUrlString(String urlString) {
      return LOOKUP.get(urlString);
    }
  }

  /**
   * Get app data for the application.
   * 
   * @return the app data, possibly a subset.
   */
  Map<String, ?> getAppData();
  
  /**
   * Sets app data for the application.
   * 
   * @param appData the app data, possibly a subset 
   */
  void setAppData(Map<String, ?> appData);  


	/**
	 * Get addresses associated with the application, specified as an List of Address objects. Container
	 * support for this field is OPTIONAL.
	 *
	 * @return appUrl
	 */
	String getAppType();

	/**
	 * Set addresses associated with the application, specified as an List of Address objects. Container
	 * support for this field is OPTIONAL.
	 *
	 * @param appUrl appUrl objects
	 */
	void setAppType(String appType); 
	
	/**
	 * Get addresses associated with the application, specified as an List of Address objects. Container
	 * support for this field is OPTIONAL.
	 *
	 * @return appUrl
	 */
	String getAppUrl();

	/**
	 * Set addresses associated with the application, specified as an List of Address objects. Container
	 * support for this field is OPTIONAL.
	 *
	 * @param appUrl appUrl objects
	 */
	void setAppUrl(String appUrl); 
	
	/**
	 * Get security token for an app
	 *
	 * @return token
	 */
  String getToken();

	/**
	 * Set security token for an app
	 *
	 * @param token security token
	 */
  void setToken(String token);
  
	/**
	 * Get addresses associated with the application, specified as an List of Address objects. Container
	 * support for this field is OPTIONAL.
	 *
	 * @return author
	 */
	String getAuthor();

	/**
	 * Set addresses associated with the application, specified as an List of Address objects. Container
	 * support for this field is OPTIONAL.
	 *
	 * @param author author objects
	 */
	void setAuthor(String author);	

	/**
	 * Get addresses associated with the application, specified as an List of Address objects. Container
	 * support for this field is OPTIONAL.
	 *
	 * @return authorEmail
	 */
	String getAuthorEmail();

	/**
	 * Set addresses associated with the application, specified as an List of Address objects. Container
	 * support for this field is OPTIONAL.
	 *
	 * @param authorEmail authorEmail objects
	 */
	void setAuthorEmail(String authorEmail);

  /**
   * Set A parent ID for a application/application to which this application belongs. Container support for this
   * field is REQUIRED.
   * @return the parentId
   */
  String getParentId();

  /**
   * Set A parent ID for a application/application to which this application belongs. Container support for this
   * field is REQUIRED.
   *
   * @param parentId of element to which application belongs
   */
  void setParentId(String parentId);

	/**
	 * Set A parent TYPE to @application or @application where this application belongs. Container support for this
	 * field is REQUIRED.
	 * @return the parentType
	 */
	String getParentType();

	/**
	 * Set A parent TYPE to @application or @application where this application belongs. Container support for this
	 * field is REQUIRED.
	 *
	 * @param parentType of element to which application belongs
	 */
	void setParentType(String parentType);  

	/**
	 * Get string description of a application, specified as a string. Container support for this field is
	 * OPTIONAL.
	 *
	 * @return the application's description
	 */
	String getDescription();

	/**
	 * Set string description of a application, specified as a string. Container support for this field is
	 * OPTIONAL.
	 *
	 * @param description the application's description 
	 */
	void setDescription(String description); 
  
  /**
   * Get the application's Emails associated with the application.
   * Container support for this field is OPTIONAL.
   *
   * @return a list of the application's emails
   */
  Integer getHeight();

  /**
   * Set the application's Emails associated with the application.
   * Container support for this field is OPTIONAL.
   *
   * @param height a list of the application's emails
   */
  void setHeight(Integer height);

  /**
   * Get A string ID that can be permanently associated with this application. Container support for this
   * field is REQUIRED.
   *
   * @return the permanent ID of the application
   */
  String getId();

  /**
   * Set A string ID that can be permanently associated with this application. Container support for this
   * field is REQUIRED.
   *
   * @param id the permanent ID of the application
   */
  void setId(String id);
    
  /**
   * Get a list of Instant messaging address for this App. No official canonicalization rules
   * exist for all instant messaging addresses, but Service Providers SHOULD remove all whiteapplication
   * and convert the address to lowercase, if this is appropriate for the service this IM address is
   * used for. Instead of the standard Canonical Values for type, this field defines the following
   * Canonical Values to represent currently popular IM services: aim, gtalk, icq, xmpp, msn, skype,
   * qq, and yahoo.
   *
   * @return A list of IM addresses
   */
  List<ListField> getIms();

  /**
   * Set a list of Instant messaging address for this App. No official canonicalization rules
   * exist for all instant messaging addresses, but Service Providers SHOULD remove all whiteapplication
   * and convert the address to lowercase, if this is appropriate for the service this IM address is
   * used for. Instead of the standard Canonical Values for type, this field defines the following
   * Canonical Values to represent currently popular IM services: aim, gtalk, icq, xmpp, msn, skype,
   * qq, and yahoo.
   *
   * @param ims a list ListFields representing IM addresses.
   */
  void setIms(List<ListField> ims);  

	/**
	 * Get the application's name Container support for this field is REQUIRED.
	 *
	 * @return the application's name
	 */
	String getName();

	/**
	 * Set the application's name Container support for this field is REQUIRED.
	 *
	 * @param name the application's name
	 */
	void setName(String name);

	/**
	 * Get the Phone numbers associated with the application.
	 *
	 * @return the Phone numbers associated with the application
	 */
  String getScreenshotUrl();

	/**
	 * Set the Phone numbers associated with the application.
	 *
	 * @param phoneNumbers the Phone numbers associated with the application
	 */
	void setScreenshotUrl(String screenshotUrl);
  
	/**
   * Get arbitrary tags about the person. Container support for this field is OPTIONAL.
   *
   * @return arbitrary tags about the person.
   */
  List<String> getTags();

  /**
   * Set arbitrary tags about the person. Container support for this field is OPTIONAL.
   *
   * @param tags arbitrary tags about the person.
   */
  void setTags(List<String> tags);

  /**
   * Get the application's profile url. Container support for this field is OPTIONAL.
   *
   * @return the application's status, headline or shoutout
   */
  String getProfileUrl();

  /**
   * Set the application's profile url. Container support for this field is OPTIONAL.
   *
   * @param status the application's status, headline or shoutout
   */
  void setProfileUrl(String profileUrl);

  /**
   * Get the application's status, headline or shoutout. Container support for this field is OPTIONAL.
   *
   * @return the application's status, headline or shoutout
   */
  String getThumbnailUrl();

  /**
   * Set the application's status, headline or shoutout. Container support for this field is OPTIONAL.
   *
   * @param status the application's status, headline or shoutout
   */
  void setThumbnailUrl(String thumbnailUrl);

  /**
   * The time this application was last updated.
   *
   * @return the last update time
   */
  Date getUpdated();

  /**
   * Set the time this record was last updated.
   *
   * @param updated the last update time
   */
  void setUpdated(Date updated);

  /**
   * Get the App's time zone, specified as the difference in minutes between Greenwich Mean Time
   * (GMT) and the application's local time. Container support for this field is OPTIONAL.
   *
   * @return the App's time zone
   */
  Long getUtcOffset();

  /**
   * Set the App's time zone, specified as the difference in minutes between Greenwich Mean Time
   * (GMT) and the application's local time. Container support for this field is OPTIONAL.
   *
   * @param utcOffset the App's time zone
   */
  void setUtcOffset(Long utcOffset);

  /**
   * @return true if this application object represents the owner of the current page.
   */
  boolean getIsOwner();

  /**
   * Set the owner flag.
   * @param isOwner the isOwnerflag
   */
  void setIsOwner(boolean isOwner);

}
