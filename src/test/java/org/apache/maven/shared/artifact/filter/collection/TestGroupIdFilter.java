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

import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.testing.ArtifactStubFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * TestCases for GroupIdFilter
 */
class TestGroupIdFilter extends AbstractArtifactFeatureFilterTest {

    @BeforeEach
    void setUp() throws Exception {
        filterClass = GroupIdFilter.class;
        ArtifactStubFactory factory = new ArtifactStubFactory(null, false);
        artifacts = factory.getGroupIdArtifacts();
    }

    @Test
    public void checkParsing() throws Exception {
        parsing();
    }

    @Test
    public void checkFiltering() throws Exception {
        Set<Artifact> result = filtering();
        for (Artifact artifact : result) {
            assertTrue(
                    artifact.getGroupId().equals("one") || artifact.getGroupId().equals("two"));
        }
    }

    @Test
    public void checkFiltering2() throws Exception {
        Set<Artifact> result = filtering2();
        for (Artifact artifact : result) {
            assertTrue(
                    artifact.getGroupId().equals("two") || artifact.getGroupId().equals("four"));
        }
    }

    @Test
    public void checkFiltering3() throws Exception {
        filtering3();
    }

    @Test
    void chckFiltering4() throws Exception {
        // include o* from groupIds one,two should leave one
        Set<Artifact> result = filtering();
        assertEquals(1, result.size());
        GroupIdFilter filter = new GroupIdFilter("o", null);
        result = filter.filter(result);
        for (Artifact artifact : result) {
            assertEquals("one", artifact.getGroupId());
        }

        // exclude on* from groupIds one,two should leave two
        result = filtering();
        assertEquals(1, result.size());
        filter = new GroupIdFilter(null, "on");
        result = filter.filter(result);
        for (Artifact artifact : result) {
            assertEquals("two", artifact.getGroupId());
        }
    }

    @Test
    void checkMultipleInclude() throws SecurityException, IllegalArgumentException, ArtifactFilterException {
        ArtifactsFilter filter = new GroupIdFilter("one,two", null);

        assertEquals(4, artifacts.size());

        Set<Artifact> result = filter.filter(artifacts);

        assertEquals(2, result.size());
    }

    @Test
    void checkMultipleExclude() throws SecurityException, IllegalArgumentException, ArtifactFilterException {
        ArtifactsFilter filter = new GroupIdFilter(null, "one,two");

        assertEquals(4, artifacts.size());

        Set<Artifact> result = filter.filter(artifacts);

        assertEquals(2, result.size());
    }
}
