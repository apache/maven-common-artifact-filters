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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author <a href="mailto:brianf@apache.org">Brian Fox</a>
 */
class TestProjectTransitivityFilter {
    Set<Artifact> artifacts;

    Set<Artifact> directArtifacts;

    Set<Artifact> classifiedArtifacts;

    @BeforeEach
    void setUp() throws Exception {
        ArtifactStubFactory fact = new ArtifactStubFactory(null, false);
        artifacts = fact.getScopedArtifacts();
        directArtifacts = fact.getReleaseAndSnapshotArtifacts();
        classifiedArtifacts = fact.getClassifiedArtifacts();
        artifacts.addAll(directArtifacts);
        artifacts.addAll(classifiedArtifacts);
    }

    @Test
    void checkAll() {
        ProjectTransitivityFilter filter = new ProjectTransitivityFilter(directArtifacts, false);
        Set<Artifact> result = filter.filter(artifacts);
        assertEquals(11, result.size());
    }

    @Test
    void checkExclude() {
        ProjectTransitivityFilter filter = new ProjectTransitivityFilter(directArtifacts, false);
        assertFalse(filter.isExcludeTransitive());
        filter.setExcludeTransitive(true);
        assertTrue(filter.isExcludeTransitive());
        Set<Artifact> result = filter.filter(artifacts);

        assertEquals(2, result.size());

        for (Artifact artifact : result) {
            assertTrue(artifact.getArtifactId().equals("release")
                    || artifact.getArtifactId().equals("snapshot"));
        }
    }

    @Test
    void checkClassified() {
        ProjectTransitivityFilter filter = new ProjectTransitivityFilter(classifiedArtifacts, true);
        Set<Artifact> result = filter.filter(artifacts);
        assertEquals(4, result.size());
    }
}
