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
package org.apache.maven.shared.artifact.filter;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.plugin.testing.ArtifactStubFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ScopeArtifactFilterTest {

    @Test
    void checkExcludedArtifactWithRangeShouldNotCauseNPE() throws Exception {
        ArtifactStubFactory factory = new ArtifactStubFactory();

        Artifact excluded = factory.createArtifact(
                "group",
                "artifact",
                VersionRange.createFromVersionSpec("[1.2.3]"),
                Artifact.SCOPE_PROVIDED,
                "jar",
                null,
                false);

        ArtifactFilter filter = new ScopeArtifactFilter(Artifact.SCOPE_RUNTIME);

        assertFalse(filter.include(excluded));
    }

    @Test
    void checkNullScopeDisabled() {
        ScopeArtifactFilter filter = new ScopeArtifactFilter();
        filter.setIncludeNullScope(false);

        verifyExcluded(filter, null);
    }

    @Test
    void checkFineGrainedIncludeOnlyScopesThatWereEnabledTestScope() {
        ScopeArtifactFilter filter = new ScopeArtifactFilter();
        filter.setIncludeTestScope(true);

        verifyExcluded(filter, Artifact.SCOPE_COMPILE);
        verifyExcluded(filter, Artifact.SCOPE_PROVIDED);
        verifyExcluded(filter, Artifact.SCOPE_RUNTIME);
        verifyExcluded(filter, Artifact.SCOPE_SYSTEM);
        verifyIncluded(filter, Artifact.SCOPE_TEST);
        verifyIncluded(filter, null);
    }

    @Test
    void checkFineGrainedIncludeOnlyScopesThatWereEnabledCompileScope() {
        ScopeArtifactFilter filter = new ScopeArtifactFilter();
        filter.setIncludeCompileScope(true);

        verifyIncluded(filter, Artifact.SCOPE_COMPILE);
        verifyExcluded(filter, Artifact.SCOPE_PROVIDED);
        verifyExcluded(filter, Artifact.SCOPE_RUNTIME);
        verifyExcluded(filter, Artifact.SCOPE_SYSTEM);
        verifyExcluded(filter, Artifact.SCOPE_TEST);
        verifyIncluded(filter, null);
    }

    @Test
    void checkFineGrainedIncludeOnlyScopesThatWereEnabledRuntimeScope() {
        ScopeArtifactFilter filter = new ScopeArtifactFilter();
        filter.setIncludeRuntimeScope(true);

        verifyExcluded(filter, Artifact.SCOPE_COMPILE);
        verifyExcluded(filter, Artifact.SCOPE_PROVIDED);
        verifyIncluded(filter, Artifact.SCOPE_RUNTIME);
        verifyExcluded(filter, Artifact.SCOPE_SYSTEM);
        verifyExcluded(filter, Artifact.SCOPE_TEST);
        verifyIncluded(filter, null);
    }

    @Test
    void checkFineGrainedIncludeOnlyScopesThatWereEnabledProvidedScope() {
        ScopeArtifactFilter filter = new ScopeArtifactFilter();
        filter.setIncludeProvidedScope(true);

        verifyExcluded(filter, Artifact.SCOPE_COMPILE);
        verifyIncluded(filter, Artifact.SCOPE_PROVIDED);
        verifyExcluded(filter, Artifact.SCOPE_RUNTIME);
        verifyExcluded(filter, Artifact.SCOPE_SYSTEM);
        verifyExcluded(filter, Artifact.SCOPE_TEST);
        verifyIncluded(filter, null);
    }

    @Test
    void checkFineGrainedIncludeOnlyScopesThatWereEnabledSystemScope() {
        ScopeArtifactFilter filter = new ScopeArtifactFilter();
        filter.setIncludeSystemScope(true);

        verifyExcluded(filter, Artifact.SCOPE_COMPILE);
        verifyExcluded(filter, Artifact.SCOPE_PROVIDED);
        verifyExcluded(filter, Artifact.SCOPE_RUNTIME);
        verifyIncluded(filter, Artifact.SCOPE_SYSTEM);
        verifyExcluded(filter, Artifact.SCOPE_TEST);
        verifyIncluded(filter, null);
    }

    @Test
    void checkFineGrainedIncludeOnlyScopesThatWereEnabledProvidedAndRuntimeScopes() {
        ScopeArtifactFilter filter = new ScopeArtifactFilter();
        filter.setIncludeRuntimeScope(true);
        filter.setIncludeProvidedScope(true);

        verifyExcluded(filter, Artifact.SCOPE_COMPILE);
        verifyIncluded(filter, Artifact.SCOPE_PROVIDED);
        verifyIncluded(filter, Artifact.SCOPE_RUNTIME);
        verifyExcluded(filter, Artifact.SCOPE_SYSTEM);
        verifyExcluded(filter, Artifact.SCOPE_TEST);
        verifyIncluded(filter, null);
    }

    @Test
    void checkFineGrainedIncludeOnlyScopesThatWereEnabledSystemAndRuntimeScopes() {
        ScopeArtifactFilter filter = new ScopeArtifactFilter();
        filter.setIncludeRuntimeScope(true);
        filter.setIncludeSystemScope(true);

        verifyExcluded(filter, Artifact.SCOPE_COMPILE);
        verifyExcluded(filter, Artifact.SCOPE_PROVIDED);
        verifyIncluded(filter, Artifact.SCOPE_RUNTIME);
        verifyIncluded(filter, Artifact.SCOPE_SYSTEM);
        verifyExcluded(filter, Artifact.SCOPE_TEST);
        verifyIncluded(filter, null);
    }

    @Test
    void checkFineGrainedWithImplicationsCompileScopeShouldIncludeOnlyArtifactsWithNullSystemProvidedOrCompileScopes() {
        ScopeArtifactFilter filter = new ScopeArtifactFilter();
        filter.setIncludeCompileScopeWithImplications(true);

        verifyIncluded(filter, null);
        verifyIncluded(filter, Artifact.SCOPE_COMPILE);
        verifyIncluded(filter, Artifact.SCOPE_PROVIDED);
        verifyIncluded(filter, Artifact.SCOPE_SYSTEM);

        verifyExcluded(filter, Artifact.SCOPE_RUNTIME);
        verifyExcluded(filter, Artifact.SCOPE_TEST);
    }

    @Test
    void checkFineGrainedWithImplicationsRuntimeScopeShouldIncludeOnlyArtifactsWithNullRuntimeOrCompileScopes() {
        ScopeArtifactFilter filter = new ScopeArtifactFilter();
        filter.setIncludeRuntimeScopeWithImplications(true);

        verifyIncluded(filter, null);
        verifyIncluded(filter, Artifact.SCOPE_COMPILE);
        verifyIncluded(filter, Artifact.SCOPE_RUNTIME);

        verifyExcluded(filter, Artifact.SCOPE_PROVIDED);
        verifyExcluded(filter, Artifact.SCOPE_SYSTEM);
        verifyExcluded(filter, Artifact.SCOPE_TEST);
    }

    @Test
    void checkFineGrainedWithImplicationsTestScopeShouldIncludeAllScopes() {
        ScopeArtifactFilter filter = new ScopeArtifactFilter();
        filter.setIncludeTestScopeWithImplications(true);

        verifyIncluded(filter, null);
        verifyIncluded(filter, Artifact.SCOPE_COMPILE);
        verifyIncluded(filter, Artifact.SCOPE_RUNTIME);

        verifyIncluded(filter, Artifact.SCOPE_PROVIDED);
        verifyIncluded(filter, Artifact.SCOPE_SYSTEM);
        verifyIncluded(filter, Artifact.SCOPE_TEST);
    }

    @Test
    void checkScopesShouldIncludeArtifactWithSameScope() {
        verifyIncluded(Artifact.SCOPE_COMPILE, Artifact.SCOPE_COMPILE);
        verifyIncluded(Artifact.SCOPE_PROVIDED, Artifact.SCOPE_PROVIDED);
        verifyIncluded(Artifact.SCOPE_RUNTIME, Artifact.SCOPE_RUNTIME);
        verifyIncluded(Artifact.SCOPE_SYSTEM, Artifact.SCOPE_SYSTEM);
        verifyIncluded(Artifact.SCOPE_TEST, Artifact.SCOPE_TEST);
        verifyIncluded((String) null, null);
    }

    @Test
    void checkCompileScopeShouldIncludeOnlyArtifactsWithNullSystemProvidedOrCompileScopes() {
        String scope = Artifact.SCOPE_COMPILE;

        verifyIncluded(scope, null);
        verifyIncluded(scope, Artifact.SCOPE_COMPILE);
        verifyIncluded(scope, Artifact.SCOPE_PROVIDED);
        verifyIncluded(scope, Artifact.SCOPE_SYSTEM);

        verifyExcluded(scope, Artifact.SCOPE_RUNTIME);
        verifyExcluded(scope, Artifact.SCOPE_TEST);
    }

    @Test
    void checkRuntimeScopeShouldIncludeOnlyArtifactsWithNullRuntimeOrCompileScopes() {
        String scope = Artifact.SCOPE_RUNTIME;

        verifyIncluded(scope, null);
        verifyIncluded(scope, Artifact.SCOPE_COMPILE);
        verifyIncluded(scope, Artifact.SCOPE_RUNTIME);

        verifyExcluded(scope, Artifact.SCOPE_PROVIDED);
        verifyExcluded(scope, Artifact.SCOPE_SYSTEM);
        verifyExcluded(scope, Artifact.SCOPE_TEST);
    }

    @Test
    void checkScopeShouldIncludeAllScopes() {
        String scope = Artifact.SCOPE_TEST;

        verifyIncluded(scope, null);
        verifyIncluded(scope, Artifact.SCOPE_COMPILE);
        verifyIncluded(scope, Artifact.SCOPE_RUNTIME);

        verifyIncluded(scope, Artifact.SCOPE_PROVIDED);
        verifyIncluded(scope, Artifact.SCOPE_SYSTEM);
        verifyIncluded(scope, Artifact.SCOPE_TEST);
    }

    @Test
    void checkProvidedScopeShouldIncludeOnlyArtifactsWithNullOrProvidedScopes() {
        String scope = Artifact.SCOPE_PROVIDED;

        verifyIncluded(scope, null);
        verifyExcluded(scope, Artifact.SCOPE_COMPILE);
        verifyExcluded(scope, Artifact.SCOPE_RUNTIME);

        verifyIncluded(scope, Artifact.SCOPE_PROVIDED);

        verifyExcluded(scope, Artifact.SCOPE_SYSTEM);
        verifyExcluded(scope, Artifact.SCOPE_TEST);
    }

    @Test
    void checkSystemScopeShouldIncludeOnlyArtifactsWithNullOrSystemScopes() {
        String scope = Artifact.SCOPE_SYSTEM;

        verifyIncluded(scope, null);
        verifyExcluded(scope, Artifact.SCOPE_COMPILE);
        verifyExcluded(scope, Artifact.SCOPE_RUNTIME);
        verifyExcluded(scope, Artifact.SCOPE_PROVIDED);

        verifyIncluded(scope, Artifact.SCOPE_SYSTEM);

        verifyExcluded(scope, Artifact.SCOPE_TEST);
    }

    private void verifyIncluded(String filterScope, String artifactScope) {
        Artifact artifact = createMockArtifact(artifactScope);

        ArtifactFilter filter = new ScopeArtifactFilter(filterScope);

        assertTrue(
                filter.include(artifact),
                "Artifact scope: " + artifactScope + " NOT included using filter scope: " + filterScope);
    }

    private void verifyExcluded(String filterScope, String artifactScope) {
        Artifact artifact = createMockArtifact(artifactScope);

        ArtifactFilter filter = new ScopeArtifactFilter(filterScope);

        assertFalse(
                filter.include(artifact),
                "Artifact scope: " + artifactScope + " NOT excluded using filter scope: " + filterScope);
    }

    private void verifyIncluded(ScopeArtifactFilter filter, String artifactScope) {
        Artifact artifact = createMockArtifact(artifactScope);

        assertTrue(filter.include(artifact), "Artifact scope: " + artifactScope + " SHOULD BE included");
    }

    private void verifyExcluded(ScopeArtifactFilter filter, String artifactScope) {
        Artifact artifact = createMockArtifact(artifactScope);

        assertFalse(filter.include(artifact), "Artifact scope: " + artifactScope + " SHOULD BE excluded");
    }

    private Artifact createMockArtifact(String scope) {
        Artifact artifact = mock(Artifact.class);

        when(artifact.getScope()).thenReturn(scope);
        when(artifact.getId()).thenReturn("group:artifact:type:version");

        return artifact;
    }
}
