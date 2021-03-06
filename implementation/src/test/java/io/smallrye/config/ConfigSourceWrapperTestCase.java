/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2018 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.smallrye.config;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.Map;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.junit.Test;

/**
 */
public class ConfigSourceWrapperTestCase {

    @Test
    public void testUndecorated() {
        final Config config = new SmallRyeConfigBuilder().addDefaultSources().build();
        final Iterator<ConfigSource> iterator = config.getConfigSources().iterator();
        ConfigSource source;

        assertTrue(iterator.hasNext());
        source = iterator.next();
        assertIsInstance(SysPropConfigSource.class, source);
        assertTrue(iterator.hasNext());
        source = iterator.next();
        assertIsInstance(EnvConfigSource.class, source);
        assertFalse(iterator.hasNext());
    }

    @Test
    public void testDecoratedOnce() {
        final Config config = new SmallRyeConfigBuilder().withWrapper(WrappedSource::new).addDefaultSources().build();
        final Iterator<ConfigSource> iterator = config.getConfigSources().iterator();
        ConfigSource source;

        assertTrue(iterator.hasNext());
        source = iterator.next();
        assertIsInstance(SysPropConfigSource.class, assertIsInstance(WrappedSource.class, source).getDelegate());
        assertTrue(iterator.hasNext());
        source = iterator.next();
        assertIsInstance(EnvConfigSource.class, assertIsInstance(WrappedSource.class, source).getDelegate());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void testDecoratedTwice() {
        final Config config = new SmallRyeConfigBuilder().withWrapper(WrappedSource::new).withWrapper(WrappedSource::new)
                .addDefaultSources().build();
        final Iterator<ConfigSource> iterator = config.getConfigSources().iterator();
        ConfigSource source;

        assertTrue(iterator.hasNext());
        source = iterator.next();
        assertIsInstance(SysPropConfigSource.class,
                assertIsInstance(WrappedSource.class, assertIsInstance(WrappedSource.class, source).getDelegate())
                        .getDelegate());
        assertTrue(iterator.hasNext());
        source = iterator.next();
        assertIsInstance(EnvConfigSource.class,
                assertIsInstance(WrappedSource.class, assertIsInstance(WrappedSource.class, source).getDelegate())
                        .getDelegate());
        assertFalse(iterator.hasNext());
    }

    static class WrappedSource implements ConfigSource {
        private final ConfigSource delegate;

        WrappedSource(final ConfigSource delegate) {
            this.delegate = delegate;
        }

        ConfigSource getDelegate() {
            return delegate;
        }

        public Map<String, String> getProperties() {
            return delegate.getProperties();
        }

        public String getValue(final String propertyName) {
            return delegate.getValue(propertyName);
        }

        public String getName() {
            return delegate.getName();
        }
    }

    static <T> T assertIsInstance(Class<T> expected, Object thing) {
        assertNotNull(thing);
        assertTrue("Expected instance of " + expected + ", got " + thing.getClass(), expected.isInstance(thing));
        return expected.cast(thing);
    }
}
