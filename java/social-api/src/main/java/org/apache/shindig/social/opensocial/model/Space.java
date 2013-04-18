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
import org.apache.shindig.social.core.model.SpaceDb;
//import org.apache.shindig.social.core.model.SpaceImpl;

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
 * see <a href="http://www.opensocial.org/Technical-Resources/opensocial-spec-v081/opensocial-reference#opensocial.Space.Field">
 * http://www.opensocial.org/Technical-Resources/opensocial-spec-v081/opensocial-reference#opensocial.Space.Field</a>
 * for all field meanings. All fields are represented in the js api at this time except for lastUpdated.
 * This field is currently only in the RESTful spec.
 *
 */
@ImplementedBy(SpaceDb.class)
@Exportablebean
public interface Space {
  /**
   * The type of a profile url when represented as a list field.
   */
  String PROFILE_URL_TYPE = "profile";

  /**
   * The type of thumbnail photo types when represented as list fields.
   */
  String THUMBNAIL_PHOTO_TYPE = "thumbnail";

  /**
   * The display name for the space.
   * @return the display name
   */
  String getDisplayName();

  /**
   * Set the display name.
   * @param displayName the new display name.
   */
  void setDisplayName(String displayName);

  /**
   * The fields that represent the space object in json form.
   */
  public static enum Field {
    /** the json field for addresses. */
    ADDRESSES("addresses"),
    /** the json field for appData. */
    APP_DATA("appData"),
    /** the json field for parentId. */
    PARENT_ID("parentId"),
    /** the json field for parentType. */
    PARENT_TYPE("parentType"),
    /** the json field for description. */
    DESCRIPTION("description"),
    /** the json field for display name. */
    DISPLAY_NAME("displayName"), /** Needed to support the RESTful api. */
    /** the json field for emails. */
    EMAILS("emails"),
    /** the json field for hasApp. */
    HAS_APP("hasApp"),
    /** the json field for id. */
    ID("id"),
    /** the json field for images. */
    IMAGES("images"),
    /** the json field for IM accounts. */
    IMS("ims"),
    /** the json field for interests. */
    INTERESTS("interests"),
    /** the json field for location. */
    LOCATION("location"),
    /** the json field for name. */
    NAME("name"),
    /** the json field for phoneNumbers. */
    PHONE_NUMBERS("phoneNumbers"),
    /** the json field for status. */
    STATUS("status"),
    /** the json field for thumbnailUrl. */
    THUMBNAIL_URL("thumbnailUrl"),
    /** the json field for urls. */
    URLS("urls"),
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
        DESCRIPTION.toString(), 
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
     * @return The corresponding space field.
     */
    public static Space.Field fromUrlString(String urlString) {
      return LOOKUP.get(urlString);
    }
  }

  /**
   * Get addresses associated with the space, specified as an List of Address objects. Container
   * support for this field is OPTIONAL.
   *
   * @return a List of address objects
   */
  List<Address> getAddresses();

  /**
   * Set addresses associated with the space, specified as an List of Address objects. Container
   * support for this field is OPTIONAL.
   *
   * @param addresses a list of address objects
   */
  void setAddresses(List<Address> addresses);


  /**
   * Get app data for the space.
   * 
   * @return the app data, possibly a subset.
   */
  Map<String, ?> getAppData();
  
  /**
   * Sets app data for the space.
   * 
   * @param appData the app data, possibly a subset 
   */
  void setAppData(Map<String, ?> appData);

  /**
   * Set A parent ID for a space/space to which this space belongs. Container support for this
   * field is REQUIRED.
   * @return the parentId
   */
  String getParentId();

  /**
   * Set A parent ID for a space/space to which this space belongs. Container support for this
   * field is REQUIRED.
   *
   * @param parentId of element to which space belongs
   */
  void setParentId(String parentId);

	/**
	 * Set A parent TYPE to @space or @space where this space belongs. Container support for this
	 * field is REQUIRED.
	 * @return the parentType
	 */
	String getParentType();

	/**
	 * Set A parent TYPE to @space or @space where this space belongs. Container support for this
	 * field is REQUIRED.
	 *
	 * @param parentType of element to which space belongs
	 */
	void setParentType(String parentType);  

	/**
	 * Get string description of a space, specified as a string. Container support for this field is
	 * OPTIONAL.
	 *
	 * @return the space's description
	 */
	String getDescription();

	/**
	 * Set string description of a space, specified as a string. Container support for this field is
	 * OPTIONAL.
	 *
	 * @param description the space's description 
	 */
	void setDescription(String description); 
  
  /**
   * Get the space's Emails associated with the space.
   * Container support for this field is OPTIONAL.
   *
   * @return a list of the space's emails
   */
  List<ListField> getEmails();

  /**
   * Set the space's Emails associated with the space.
   * Container support for this field is OPTIONAL.
   *
   * @param emails a list of the space's emails
   */
  void setEmails(List<ListField> emails);

  /**
   * Get if the space has used the current app. Container support for this field is OPTIONAL.
   * Has app needs to take account of the parent of the application that is performing the
   * query on this space object.
   * @return true the current app has been used
   */
  Boolean getHasApp();

  /**
   * Set if the space has used the current app. Container support for this field is OPTIONAL.
   *
   * @param hasApp set true the current app has been used
   */
  void setHasApp(Boolean hasApp);

  /**
   * Get A string ID that can be permanently associated with this space. Container support for this
   * field is REQUIRED.
   *
   * @return the permanent ID of the space
   */
  String getId();

  /**
   * Set A string ID that can be permanently associated with this space. Container support for this
   * field is REQUIRED.
   *
   * @param id the permanent ID of the space
   */
  void setId(String id);
    
	/**
	 * URL of an image for this space. The value SHOULD be a canonicalized URL, and MUST point to an
	 * actual image file (e.g. a GIF, JPEG, or PNG image file) rather than to a web page containing an
	 * image. Service Providers MAY return the same image at different sizes, though it is recognized
	 * that no standard for describing images of various sizes currently exists. Note that this field
	 * SHOULD NOT be used to send down arbitrary photos taken by this space, but specifically profile
	 * photos of the contact suitable for display when describing the contact.
	 *
	 * @return a list of Images
	 */
	List<ListField> getImages();

	/**
	 * Set a list of Images for the space.
	 * @see Space#getImages()
	 *
	 * @param images a list of images.
	 */
	void setImages(List<ListField> images); 

  /**
   * Get a list of Instant messaging address for this Space. No official canonicalization rules
   * exist for all instant messaging addresses, but Service Providers SHOULD remove all whitespace
   * and convert the address to lowercase, if this is appropriate for the service this IM address is
   * used for. Instead of the standard Canonical Values for type, this field defines the following
   * Canonical Values to represent currently popular IM services: aim, gtalk, icq, xmpp, msn, skype,
   * qq, and yahoo.
   *
   * @return A list of IM addresses
   */
  List<ListField> getIms();

  /**
   * Set a list of Instant messaging address for this Space. No official canonicalization rules
   * exist for all instant messaging addresses, but Service Providers SHOULD remove all whitespace
   * and convert the address to lowercase, if this is appropriate for the service this IM address is
   * used for. Instead of the standard Canonical Values for type, this field defines the following
   * Canonical Values to represent currently popular IM services: aim, gtalk, icq, xmpp, msn, skype,
   * qq, and yahoo.
   *
   * @param ims a list ListFields representing IM addresses.
   */
  void setIms(List<ListField> ims);  

  /**
   * Get the space's interests, hobbies or passions, specified as an List of strings. Container
   * support for this field is OPTIONAL.
   *
   * @return the space's interests, hobbies or passions
   */
  List<String> getInterests();

  /**
   * Set the space's interests, hobbies or passions, specified as an List of strings. Container
   * support for this field is OPTIONAL.
   *
   * @param interests the space's interests, hobbies or passions
   */
  void setInterests(List<String> interests);

  /**
   * Get the Space's favorite jobs, or job interests and skills, specified as a string. Container
   * support for this field is OPTIONAL
   *
   * @return the Space's favorite jobs, or job interests and skills
   */
  String getLocation();

  /**
   * Set the Space's favorite jobs, or job interests and skills, specified as a string. Container
   * support for this field is OPTIONAL
   *
   * @param location the Space's favorite jobs, or job interests and skills
   */
  void setLocation(String location);  

	/**
	 * Get the space's name Container support for this field is REQUIRED.
	 *
	 * @return the space's name
	 */
	String getName();

	/**
	 * Set the space's name Container support for this field is REQUIRED.
	 *
	 * @param name the space's name
	 */
	void setName(String name);

	/**
	 * Get the Phone numbers associated with the space.
	 *
	 * @return the Phone numbers associated with the space
	 */
	List<ListField> getPhoneNumbers();

	/**
	 * Set the Phone numbers associated with the space.
	 *
	 * @param phoneNumbers the Phone numbers associated with the space
	 */
	void setPhoneNumbers(List<ListField> phoneNumbers);

	/**
	 * Get the space's status, headline or shoutout. Container support for this field is OPTIONAL.
	 *
	 * @return the space's status, headline or shoutout
	 */
	String getStatus();

	/**
	 * Set the space's status, headline or shoutout. Container support for this field is OPTIONAL.
	 *
	 * @param status the space's status, headline or shoutout
	 */
	void setStatus(String status);

  /**
   * The time this space was last updated.
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
   * Get the Space's time zone, specified as the difference in minutes between Greenwich Mean Time
   * (GMT) and the space's local time. Container support for this field is OPTIONAL.
   *
   * @return the Space's time zone
   */
  Long getUtcOffset();

  /**
   * Set the Space's time zone, specified as the difference in minutes between Greenwich Mean Time
   * (GMT) and the space's local time. Container support for this field is OPTIONAL.
   *
   * @param utcOffset the Space's time zone
   */
  void setUtcOffset(Long utcOffset);

  /**
   * Get the URLs related to the space, their webpages, or feeds Container support for this field
   * is OPTIONAL.
   *
   * @return the URLs related to the space, their webpages, or feeds
   */
  List<Url> getUrls();

  /**
   * Set the URLs related to the space, their webpages, or feeds Container support for this field
   * is OPTIONAL.
   *
   * @param urls the URLs related to the space, their webpages, or feeds
   */
  void setUrls(List<Url> urls);

  /**
   * @return true if this space object represents the owner of the current page.
   */
  boolean getIsOwner();

  /**
   * Set the owner flag.
   * @param isOwner the isOwnerflag
   */
  void setIsOwner(boolean isOwner);

  // Proxied fields

  /**
   * Get the space's profile URL. This URL must be fully qualified. Relative URLs will not work in
   * gadgets. This field MUST be stored in the urls list with a type of "profile".
   *
   * Container support for this field is OPTIONAL.
   *
   * @return the space's profile URL
   */
  String getProfileUrl();

  /**
   * Set the space's profile URL. This URL must be fully qualified. Relative URLs will not work in
   * gadgets. This field MUST be stored in the urls list with a type of "profile".
   *
   * Container support for this field is OPTIONAL.
   *
   * @param profileUrl the space's profile URL
   */
  void setProfileUrl(String profileUrl);

  /**
   * Get the space's photo thumbnail URL, specified as a string. This URL must be fully qualified.
   * Relative URLs will not work in gadgets.
   * This field MUST be stored in the photos list with a type of "thumbnail".
   *
   * Container support for this field is OPTIONAL.
   *
   * @return the space's photo thumbnail URL
   */
  String getThumbnailUrl();

  /**
   * Set the space's photo thumbnail URL, specified as a string. This URL must be fully qualified.
   * Relative URLs will not work in gadgets.
   * This field MUST be stored in the photos list with a type of "thumbnail".
   *
   * Container support for this field is OPTIONAL.
   *
   * @param thumbnailUrl the space's photo thumbnail URL
   */
  void setThumbnailUrl(String thumbnailUrl);
}
