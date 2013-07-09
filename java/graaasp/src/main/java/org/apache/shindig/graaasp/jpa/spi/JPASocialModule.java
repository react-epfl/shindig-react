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

import com.google.inject.AbstractModule;
import com.google.inject.CreationException;
import com.google.inject.Scopes;
import com.google.inject.name.Names;
import com.google.inject.spi.Message;

import org.apache.commons.io.IOUtils;

import org.apache.shindig.social.opensocial.spi.ActivityService;
import org.apache.shindig.social.opensocial.spi.ActivityStreamService;
import org.apache.shindig.social.opensocial.spi.AlbumService;
import org.apache.shindig.social.opensocial.spi.GroupService;
import org.apache.shindig.social.opensocial.spi.AppDataService;
import org.apache.shindig.social.opensocial.spi.MediaItemService;
import org.apache.shindig.social.opensocial.spi.MessageService;
import org.apache.shindig.social.opensocial.spi.PersonService;
import org.apache.shindig.social.opensocial.spi.SpaceService;
import org.apache.shindig.social.opensocial.spi.AppService;
import org.apache.shindig.social.opensocial.spi.DocumentService;
import org.apache.shindig.graaasp.jpa.eclipselink.EclipseEntityManagerProvider;
import org.apache.shindig.graaasp.jpa.spi.ActivityServiceDb;
import org.apache.shindig.graaasp.jpa.spi.ActivityStreamServiceDb;
import org.apache.shindig.graaasp.jpa.spi.AppDataServiceDb;
import org.apache.shindig.graaasp.jpa.spi.PersonServiceDb;
import org.apache.shindig.graaasp.jpa.spi.SpaceServiceDb;
import org.apache.shindig.graaasp.jpa.spi.AppServiceDb;
import org.apache.shindig.graaasp.jpa.spi.DocumentServiceDb;
import org.apache.shindig.social.sample.spi.JsonDbOpensocialService;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;

import javax.persistence.EntityManager;

/**
 *
 */
public class JPASocialModule extends AbstractModule {

  private final static String DEFAULT_PROPERTIES = "socialjpa.properties";
  private Properties properties;
  private EntityManager entityManager;

  /**
   *
   */
  public JPASocialModule() {
      this(null);
  }

  /**
   *
   */
  public JPASocialModule(EntityManager entityManager) {
    this.entityManager = entityManager;
    InputStream is = null;
    try {
      is = this.getClass().getClassLoader().getResourceAsStream(DEFAULT_PROPERTIES);
      if (is == null) {
        System.err.println("Cant loacate properties");
        throw new IOException("Failed to open " + DEFAULT_PROPERTIES);
      }
      properties = new Properties();
      properties.load(is);
    } catch (IOException e) {
      throw new CreationException(Arrays.asList(new Message(
          "Unable to load properties: " + DEFAULT_PROPERTIES)));
    } finally {
      IOUtils.closeQuietly( is );
    }
  }

  /**
   * {@inheritDoc}
   *
   * @see com.google.inject.AbstractModule#configure()
   */
  @Override
  protected void configure() {
    Names.bindProperties(this.binder(), properties);
    if (entityManager == null) {
      bind(EntityManager.class).toProvider(EclipseEntityManagerProvider.class)
          .in(Scopes.SINGLETON);
    } else {
      bind(EntityManager.class).toInstance(this.entityManager);
    }

    bind(ActivityService.class).to(ActivityServiceDb.class).in(Scopes.SINGLETON);
    bind(ActivityStreamService.class).to(ActivityStreamServiceDb.class).in(Scopes.SINGLETON);
    bind(PersonService.class).to(PersonServiceDb.class).in(Scopes.SINGLETON);
    bind(AppDataService.class).to(AppDataServiceDb.class).in(Scopes.SINGLETON);
    bind(SpaceService.class).to(SpaceServiceDb.class).in(Scopes.SINGLETON);
    bind(AppService.class).to(AppServiceDb.class).in(Scopes.SINGLETON);
    bind(DocumentService.class).to(DocumentServiceDb.class).in(Scopes.SINGLETON);

    //Temporary
    bind(AlbumService.class).to(JsonDbOpensocialService.class);
    bind(MediaItemService.class).to(JsonDbOpensocialService.class);
    bind(MessageService.class).to(JsonDbOpensocialService.class);
    //bind(ActivityStreamService.class).to(JsonDbOpensocialService.class);
    bind(GroupService.class).to(JsonDbOpensocialService.class);

  }
}
