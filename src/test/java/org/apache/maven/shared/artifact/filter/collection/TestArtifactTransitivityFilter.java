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

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.maven.api.Artifact;
import org.apache.maven.api.Dependency;
import org.apache.maven.api.Node;
import org.apache.maven.api.PathScope;
import org.apache.maven.api.Session;
import org.apache.maven.shared.artifact.filter.DependencyStubs;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TestArtifactTransitivityFilter {
    @Test
    void resolvedDependenciesAreRecordedAndFiltered() throws Exception {
        Session session = mock(Session.class);
        Artifact root = mock(Artifact.class);
        Node rootNode = mock(Node.class);
        Node childNode = mock(Node.class);
        Dependency dep = DependencyStubs.dependency("g", "dep", "1.0", "compile");
        Dependency other = DependencyStubs.dependency("g", "other", "1.0", "compile");
        when(childNode.getDependency()).thenReturn(dep);
        when(session.collectDependencies(root, PathScope.TEST_RUNTIME)).thenReturn(rootNode);
        when(session.flattenDependencies(rootNode, PathScope.TEST_RUNTIME))
                .thenReturn(Arrays.asList(rootNode, childNode));

        ArtifactTransitivityFilter filter = new ArtifactTransitivityFilter(session, root);

        assertTrue(filter.artifactIsATransitiveDependency(dep));
        assertFalse(filter.artifactIsATransitiveDependency(other));

        Set<Dependency> result = filter.filter(new LinkedHashSet<>(Arrays.asList(dep, other)));
        assertEquals(1, result.size());
        assertTrue(result.contains(dep));
    }
}
