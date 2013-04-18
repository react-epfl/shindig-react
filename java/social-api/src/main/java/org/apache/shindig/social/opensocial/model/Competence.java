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
import org.apache.shindig.social.core.model.CompetenceDb;


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
@ImplementedBy(CompetenceDb.class)
@Exportablebean
public interface Competence {
  /**
   * The type of a profile url when represented as a list field.
   */
  String PROFILE_URL_TYPE = "profile";

  /**
   * The type of thumbnail photo types when represented as list fields.
   */
  String THUMBNAIL_PHOTO_TYPE = "thumbnail";

  /**
   * The fields that represent the space object in json form.
   */
  public static enum Field {
    /** the json field for topic. */
    TOPIC("topic"),
    /** the json field for value. */
    VALUE("value");

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
        TOPIC.toString(),           
        VALUE.toString());

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
    public static Competence.Field fromUrlString(String urlString) {
      return LOOKUP.get(urlString);
    }
  }

  /**
   * Get topic associated with the competence. Container
   * support for this field is OPTIONAL.
   *
   * @return a Topic string
   */
  String getTopic();

  /**
   * Set topic associated with the competence. Container
   * support for this field is OPTIONAL.
   *
   * @param topic for a competence
   */
  void setTopic(String topic);


  /**
   * Get value associated with the competence. Container
   * support for this field is OPTIONAL.
   *
   * @return a Value string
   */
  String getValue();

  /**
   * Set value associated with the competence. Container
   * support for this field is OPTIONAL.
   *
   * @param value for a competence
   */
  void setValue(String value);
  
  void setPerson(Person person);
}
