/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.maven.shared.artifact.filter.collection;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Abstract test case for subclasses of AbstractArtifactFeatureFilter
 */
public abstract class AbstractArtifactFeatureFilterTest {
    protected Set<Artifact> artifacts = new HashSet<>();

    protected Class<?> filterClass;

    private Object createObjectViaReflection(Class<?> clazz, Object[] conArgs)
            throws SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException,
                    IllegalAccessException, InvocationTargetException {
        Class<?>[] argslist = new Class<?>[2];
        argslist[0] = String.class;
        argslist[1] = String.class;
        Constructor<?> ct = clazz.getConstructor(argslist);
        return ct.newInstance(conArgs);
    }

    @Test
    public abstract void checkParsing() throws Exception;

    public void parsing()
            throws SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException,
                    IllegalAccessException, InvocationTargetException {
        Object[] conArgs = new Object[] {"one,two", "three,four,"};

        AbstractArtifactFeatureFilter filter =
                (AbstractArtifactFeatureFilter) createObjectViaReflection(filterClass, conArgs);
        List<String> includes = filter.getIncludes();
        List<String> excludes = filter.getExcludes();

        assertEquals(2, includes.size());
        assertEquals(2, excludes.size());
        assertEquals("one", includes.get(0));
        assertEquals("two", includes.get(1));
        assertEquals("three", excludes.get(0));
        assertEquals("four", excludes.get(1));
    }

    @Test
    public abstract void checkFiltering() throws Exception;

    public Set<Artifact> filtering()
            throws SecurityException, IllegalArgumentException, NoSuchMethodException, InstantiationException,
                    IllegalAccessException, InvocationTargetException {
        Object[] conArgs = new Object[] {"one,two", "one,three,"};
        AbstractArtifactFeatureFilter filter =
                (AbstractArtifactFeatureFilter) createObjectViaReflection(filterClass, conArgs);
        Set<Artifact> result = filter.filter(artifacts);
        assertEquals(1, result.size());
        return result;
    }

    @Test
    public abstract void checkFiltering2() throws Exception;

    public Set<Artifact> filtering2()
            throws SecurityException, IllegalArgumentException, NoSuchMethodException, InstantiationException,
                    IllegalAccessException, InvocationTargetException {
        Object[] conArgs = new Object[] {null, "one,three,"};
        AbstractArtifactFeatureFilter filter =
                (AbstractArtifactFeatureFilter) createObjectViaReflection(filterClass, conArgs);
        Set<Artifact> result = filter.filter(artifacts);
        assertEquals(2, result.size());
        return result;
    }

    @Test
    public abstract void checkFiltering3() throws Exception;

    public void filtering3()
            throws SecurityException, IllegalArgumentException, NoSuchMethodException, InstantiationException,
                    IllegalAccessException, InvocationTargetException {
        Object[] conArgs = new Object[] {null, null};
        AbstractArtifactFeatureFilter filter =
                (AbstractArtifactFeatureFilter) createObjectViaReflection(filterClass, conArgs);
        Set<Artifact> result = filter.filter(artifacts);
        assertEquals(4, result.size());
    }
}
