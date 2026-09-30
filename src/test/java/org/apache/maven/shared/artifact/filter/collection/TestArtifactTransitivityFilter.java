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

import java.util.Collections;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.DependencyResolutionResult;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingResult;
import org.eclipse.aether.graph.Dependency;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TestArtifactTransitivityFilter {

    @Test
    void resolvedDependenciesAreRecordedAndFiltered() throws Exception {
        Artifact root = new org.apache.maven.artifact.DefaultArtifact(
                "g", "root", "1.0", "compile", "jar", null, new DefaultArtifactHandler("jar"));
        Artifact dep = new org.apache.maven.artifact.DefaultArtifact(
                "g", "dep", "1.0", "compile", "jar", null, new DefaultArtifactHandler("jar"));
        Artifact other = new org.apache.maven.artifact.DefaultArtifact(
                "g", "other", "1.0", "compile", "jar", null, new DefaultArtifactHandler("jar"));

        ProjectBuilder projectBuilder = mock(ProjectBuilder.class);
        ProjectBuildingResult buildingResult = mock(ProjectBuildingResult.class);
        DependencyResolutionResult resolutionResult = mock(DependencyResolutionResult.class);
        when(projectBuilder.build(any(Artifact.class), any(ProjectBuildingRequest.class)))
                .thenReturn(buildingResult);
        when(buildingResult.getDependencyResolutionResult()).thenReturn(resolutionResult);
        when(resolutionResult.getDependencies())
                .thenReturn(Collections.singletonList(
                        new Dependency(new org.eclipse.aether.artifact.DefaultArtifact("g:dep:jar:1.0"), "compile")));

        ArtifactTransitivityFilter filter =
                new ArtifactTransitivityFilter(root, new DefaultProjectBuildingRequest(), projectBuilder);

        assertTrue(filter.artifactIsATransitiveDependency(dep));
        assertFalse(filter.artifactIsATransitiveDependency(other));

        Set<Artifact> result = filter.filter(new java.util.LinkedHashSet<>(java.util.Arrays.asList(dep, other)));
        assertEquals(Collections.singleton(dep), result);
    }
}
