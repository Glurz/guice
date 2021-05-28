/*
 * Copyright (C) 2006 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.inject.servlet;

import com.google.common.collect.Lists;
import com.google.inject.Module;
import com.google.inject.*;
import junit.framework.TestCase;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import static com.google.inject.servlet.ServletTestUtils.newFakeHttpServletRequest;

/**
 * @author Remo Marti
 */
public class ServletPrivateModuleTest extends TestCase {

  @Override
  public void setUp() {
    GuiceFilter.reset();
  }

  public void testUnexposedRequestScopedObjectsAreNotAccessibleOutsideOfPrivateModule() throws ServletException, IOException {
    final Injector injector = createInjector(new Module1(), new Module2());
    final HttpServletRequest request = newFakeHttpServletRequest();

    GuiceFilter filter = new GuiceFilter();
    FilterChain filterChain =
        new FilterChain() {
          @Override
          public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse) {
            Module1.Module1Service module1Service = injector.getInstance(Module1.Module1Service.class);
            Module2.Module2Service module2Service = injector.getInstance(Module2.Module2Service.class);

            assertEquals("Provided by Module1", module1Service.getObject().getValue());
            assertEquals("Provided by Module2", module2Service.getObject().getValue());
          }
        };

    filter.doFilter(request, null, filterChain);
  }

  private Injector createInjector(Module... modules) throws CreationException {
    return Guice.createInjector(
        Lists.<Module>asList(
            new AbstractModule() {
              @Override
              protected void configure() {
                install(new ServletModule());
              }
            },
            modules));
  }

  static class SomeObject {

    private final String value;

    public SomeObject(String value) {
      this.value = value;
    }

    String getValue() {
      return value;
    }
  }

  static class Module1 extends PrivateModule {

    @Provides
    @RequestScoped
    // not exposed!
    public SomeObject provideSomeObject() {
      return new SomeObject("Provided by Module1");
    }

    @Provides
    @Exposed
    @RequestScoped
    public Module1Service provideService(SomeObject someObject) {
      return new Module1Service(someObject);
    }

    @Override
    protected void configure() {
      // empty
    }

    static final class Module1Service {

      private final SomeObject object;

      public Module1Service(SomeObject object) {
        this.object = object;
      }

      public SomeObject getObject() {
        return object;
      }
    }
  }

  static class Module2 extends PrivateModule {

    @Provides
    @RequestScoped
    // not exposed!
    public SomeObject provideSomeObject() {
      return new SomeObject("Provided by Module2");
    }

    @Provides
    @Exposed
    @RequestScoped
    public Module2Service provideService(SomeObject someObject) {
      return new Module2Service(someObject);
    }

    @Override
    protected void configure() {
      // empty
    }

    static final class Module2Service {

      private final SomeObject object;

      public Module2Service(SomeObject object) {
        this.object = object;
      }

      public SomeObject getObject() {
        return object;
      }
    }
  }
}
