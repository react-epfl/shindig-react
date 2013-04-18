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

import java.util.EnumSet;
import java.util.Map;

import org.apache.shindig.protocol.model.Exportablebean;
import org.apache.shindig.social.core.model.AttachmentDb;

import com.google.common.base.Functions;
import com.google.common.collect.Maps;
import com.google.inject.ImplementedBy;

/**
 * Base interface for all address objects
 * see <a href="http://www.opensocial.org/Technical-Resources/opensocial-spec-v081/opensocial-reference#opensocial.Attachment">
 * http://www.opensocial.org/Technical-Resources/opensocial-spec-v081/opensocial-reference#opensocial.Attachment</a>.
 */

@ImplementedBy(AttachmentDb.class)
@Exportablebean
public interface Attachment {

  /**
   * The fields that represent the address object in json form.
   */
  public static enum Field {
    /** the field name for country. */
    SIZE("size"),
    /** the field name for country. */
    FILENAME("filename"),
    /** the field name for latitude. */
    CONTENT_TYPE("content_type");

    private static final Map<String, Field> LOOKUP = Maps.uniqueIndex(EnumSet.allOf(Field.class), 
        Functions.toStringFunction());

    /**
     * The json field that the instance represents.
     */
    private final String jsonString;

    /**
     * create a field base on the a json element.
     *
     * @param jsonString the name of the element
     */
    private Field(String jsonString) {
      this.jsonString = jsonString;
    }

    /**
     * emit the field as a json element.
     *
     * @return the field name
     */
    @Override
    public String toString() {
      return this.jsonString;
    }

    public static Field getField(String jsonString) {
      return LOOKUP.get(jsonString);
    }
  }

  /**
   * Get the country.
   *
   * @return the country
   */
  String getUrl();

  /**
   * Get the country.
   *
   * @return the country
   */
  String getFilename();

  /**
   * Set the country.
   *
   * @param country the country
   */
  void setFilename(String filename);

  /**
   * Get the latitude.
   *
   * @return latitude
   */
  String getSize();

  /**
   * Set the latitude.
   *
   * @param latitude latitude
   */
  void setSize(String size);

  /**
   * Get the locality.
   *
   * @return the locality
   */
  String getContentType();

  /**
   * Set the locality.
   *
   * @param locality the locality
   */
  void setContentType(String contentType);

}
