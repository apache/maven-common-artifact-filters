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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.api.Dependency;
import org.apache.maven.shared.artifact.filter.DependencyStubs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author <a href="mailto:brianf@apache.org">Brian Fox</a>
 */
class TestTypeFilter {
    Set<Dependency> artifacts;

    @BeforeEach
    void setUp() throws Exception {
        artifacts = DependencyStubs.typedDependencies();
    }

    @Test
    void checkTypeParsing() {
        TypeFilter filter = new TypeFilter("war,jar", "sources,zip,");
        List<String> includes = filter.getIncludes();
        List<String> excludes = filter.getExcludes();

        assertEquals(2, includes.size());
        assertEquals(2, excludes.size());
        assertEquals("war", includes.get(0));
        assertEquals("jar", includes.get(1));
        assertEquals("sources", excludes.get(0));
        assertEquals("zip", excludes.get(1));
    }

    @Test
    void checkFiltering() {
        TypeFilter filter = new TypeFilter("war,jar", "war,zip,");
        Set<Dependency> result = filter.filter(artifacts);
        assertEquals(1, result.size());

        for (Dependency artifact : result) {
            assertEquals("jar", artifact.getType().id());
        }
    }

    @Test
    void checkFiltering2() {
        TypeFilter filter = new TypeFilter(null, "war,jar,");
        Set<Dependency> result = filter.filter(artifacts);
        assertEquals(3, result.size());

        for (Dependency artifact : result) {
            assertTrue(!artifact.getType().id().equals("war")
                    && !artifact.getType().id().equals("jar"));
        }
    }

    @Test
    void checkFiltering3() {
        TypeFilter filter = new TypeFilter(null, null);
        Set<Dependency> result = filter.filter(artifacts);
        assertEquals(5, result.size());
    }

    @Test
    void checkFilteringOrder() throws Exception {
        TypeFilter filter = new TypeFilter("war,jar", "zip");
        Set<Dependency> artifacts = new LinkedHashSet<>();
        artifacts.add(DependencyStubs.dependency("g", "a", "1.0", "compile", "jar", ""));
        artifacts.add(DependencyStubs.dependency("g", "b", "1.0", "compile", "zip", ""));
        artifacts.add(DependencyStubs.dependency("g", "c", "1.0", "compile", "war", ""));

        Set<Dependency> result = filter.filter(artifacts);

        assertEquals(2, result.size());

        List<Dependency> resultList = new ArrayList<>(result);

        assertEquals("a", resultList.get(0).getArtifactId());
        assertEquals("c", resultList.get(1).getArtifactId());
    }
}
