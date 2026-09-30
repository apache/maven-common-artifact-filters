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

import java.util.function.Predicate;

import org.apache.maven.api.Dependency;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScopeArtifactFilterTest {

    @Test
    void checkExcludedArtifactWithRangeShouldNotCauseNPE() {
        // the Maven 4 API resolves ranges before a dependency reaches a filter, so only the exact version remains
        Dependency excluded = DependencyStubs.dependency("group", "artifact", "1.2.3", "provided");

        Predicate<Dependency> filter = new ScopeArtifactFilter("runtime");

        assertFalse(filter.test(excluded));
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

        verifyExcluded(filter, "compile");
        verifyExcluded(filter, "provided");
        verifyExcluded(filter, "runtime");
        verifyExcluded(filter, "system");
        verifyIncluded(filter, "test");
        verifyIncluded(filter, null);
    }

    @Test
    void checkFineGrainedIncludeOnlyScopesThatWereEnabledCompileScope() {
        ScopeArtifactFilter filter = new ScopeArtifactFilter();
        filter.setIncludeCompileScope(true);

        verifyIncluded(filter, "compile");
        verifyExcluded(filter, "provided");
        verifyExcluded(filter, "runtime");
        verifyExcluded(filter, "system");
        verifyExcluded(filter, "test");
        verifyIncluded(filter, null);
    }

    @Test
    void checkFineGrainedIncludeOnlyScopesThatWereEnabledRuntimeScope() {
        ScopeArtifactFilter filter = new ScopeArtifactFilter();
        filter.setIncludeRuntimeScope(true);

        verifyExcluded(filter, "compile");
        verifyExcluded(filter, "provided");
        verifyIncluded(filter, "runtime");
        verifyExcluded(filter, "system");
        verifyExcluded(filter, "test");
        verifyIncluded(filter, null);
    }

    @Test
    void checkFineGrainedIncludeOnlyScopesThatWereEnabledProvidedScope() {
        ScopeArtifactFilter filter = new ScopeArtifactFilter();
        filter.setIncludeProvidedScope(true);

        verifyExcluded(filter, "compile");
        verifyIncluded(filter, "provided");
        verifyExcluded(filter, "runtime");
        verifyExcluded(filter, "system");
        verifyExcluded(filter, "test");
        verifyIncluded(filter, null);
    }

    @Test
    void checkFineGrainedIncludeOnlyScopesThatWereEnabledSystemScope() {
        ScopeArtifactFilter filter = new ScopeArtifactFilter();
        filter.setIncludeSystemScope(true);

        verifyExcluded(filter, "compile");
        verifyExcluded(filter, "provided");
        verifyExcluded(filter, "runtime");
        verifyIncluded(filter, "system");
        verifyExcluded(filter, "test");
        verifyIncluded(filter, null);
    }

    @Test
    void checkFineGrainedIncludeOnlyScopesThatWereEnabledProvidedAndRuntimeScopes() {
        ScopeArtifactFilter filter = new ScopeArtifactFilter();
        filter.setIncludeRuntimeScope(true);
        filter.setIncludeProvidedScope(true);

        verifyExcluded(filter, "compile");
        verifyIncluded(filter, "provided");
        verifyIncluded(filter, "runtime");
        verifyExcluded(filter, "system");
        verifyExcluded(filter, "test");
        verifyIncluded(filter, null);
    }

    @Test
    void checkFineGrainedIncludeOnlyScopesThatWereEnabledSystemAndRuntimeScopes() {
        ScopeArtifactFilter filter = new ScopeArtifactFilter();
        filter.setIncludeRuntimeScope(true);
        filter.setIncludeSystemScope(true);

        verifyExcluded(filter, "compile");
        verifyExcluded(filter, "provided");
        verifyIncluded(filter, "runtime");
        verifyIncluded(filter, "system");
        verifyExcluded(filter, "test");
        verifyIncluded(filter, null);
    }

    @Test
    void checkFineGrainedWithImplicationsCompileScopeShouldIncludeOnlyArtifactsWithNullSystemProvidedOrCompileScopes() {
        ScopeArtifactFilter filter = new ScopeArtifactFilter();
        filter.setIncludeCompileScopeWithImplications(true);

        verifyIncluded(filter, null);
        verifyIncluded(filter, "compile");
        verifyIncluded(filter, "provided");
        verifyIncluded(filter, "system");

        verifyExcluded(filter, "runtime");
        verifyExcluded(filter, "test");
    }

    @Test
    void checkFineGrainedWithImplicationsRuntimeScopeShouldIncludeOnlyArtifactsWithNullRuntimeOrCompileScopes() {
        ScopeArtifactFilter filter = new ScopeArtifactFilter();
        filter.setIncludeRuntimeScopeWithImplications(true);

        verifyIncluded(filter, null);
        verifyIncluded(filter, "compile");
        verifyIncluded(filter, "runtime");

        verifyExcluded(filter, "provided");
        verifyExcluded(filter, "system");
        verifyExcluded(filter, "test");
    }

    @Test
    void checkFineGrainedWithImplicationsTestScopeShouldIncludeAllScopes() {
        ScopeArtifactFilter filter = new ScopeArtifactFilter();
        filter.setIncludeTestScopeWithImplications(true);

        verifyIncluded(filter, null);
        verifyIncluded(filter, "compile");
        verifyIncluded(filter, "runtime");

        verifyIncluded(filter, "provided");
        verifyIncluded(filter, "system");
        verifyIncluded(filter, "test");
    }

    @Test
    void checkScopesShouldIncludeArtifactWithSameScope() {
        verifyIncluded("compile", "compile");
        verifyIncluded("provided", "provided");
        verifyIncluded("runtime", "runtime");
        verifyIncluded("system", "system");
        verifyIncluded("test", "test");
        verifyIncluded((String) null, null);
    }

    @Test
    void checkCompileScopeShouldIncludeOnlyArtifactsWithNullSystemProvidedOrCompileScopes() {
        String scope = "compile";

        verifyIncluded(scope, null);
        verifyIncluded(scope, "compile");
        verifyIncluded(scope, "provided");
        verifyIncluded(scope, "system");

        verifyExcluded(scope, "runtime");
        verifyExcluded(scope, "test");
    }

    @Test
    void checkRuntimeScopeShouldIncludeOnlyArtifactsWithNullRuntimeOrCompileScopes() {
        String scope = "runtime";

        verifyIncluded(scope, null);
        verifyIncluded(scope, "compile");
        verifyIncluded(scope, "runtime");

        verifyExcluded(scope, "provided");
        verifyExcluded(scope, "system");
        verifyExcluded(scope, "test");
    }

    @Test
    void checkScopeShouldIncludeAllScopes() {
        String scope = "test";

        verifyIncluded(scope, null);
        verifyIncluded(scope, "compile");
        verifyIncluded(scope, "runtime");

        verifyIncluded(scope, "provided");
        verifyIncluded(scope, "system");
        verifyIncluded(scope, "test");
    }

    @Test
    void checkProvidedScopeShouldIncludeOnlyArtifactsWithNullOrProvidedScopes() {
        String scope = "provided";

        verifyIncluded(scope, null);
        verifyExcluded(scope, "compile");
        verifyExcluded(scope, "runtime");

        verifyIncluded(scope, "provided");

        verifyExcluded(scope, "system");
        verifyExcluded(scope, "test");
    }

    @Test
    void checkSystemScopeShouldIncludeOnlyArtifactsWithNullOrSystemScopes() {
        String scope = "system";

        verifyIncluded(scope, null);
        verifyExcluded(scope, "compile");
        verifyExcluded(scope, "runtime");
        verifyExcluded(scope, "provided");

        verifyIncluded(scope, "system");

        verifyExcluded(scope, "test");
    }

    private void verifyIncluded(String filterScope, String artifactScope) {
        Dependency artifact = createMockArtifact(artifactScope);

        Predicate<Dependency> filter = new ScopeArtifactFilter(filterScope);

        assertTrue(
                filter.test(artifact),
                "Dependency scope: " + artifactScope + " NOT included using filter scope: " + filterScope);
    }

    private void verifyExcluded(String filterScope, String artifactScope) {
        Dependency artifact = createMockArtifact(artifactScope);

        Predicate<Dependency> filter = new ScopeArtifactFilter(filterScope);

        assertFalse(
                filter.test(artifact),
                "Dependency scope: " + artifactScope + " NOT excluded using filter scope: " + filterScope);
    }

    private void verifyIncluded(ScopeArtifactFilter filter, String artifactScope) {
        Dependency artifact = createMockArtifact(artifactScope);

        assertTrue(filter.test(artifact), "Dependency scope: " + artifactScope + " SHOULD BE included");
    }

    private void verifyExcluded(ScopeArtifactFilter filter, String artifactScope) {
        Dependency artifact = createMockArtifact(artifactScope);

        assertFalse(filter.test(artifact), "Dependency scope: " + artifactScope + " SHOULD BE excluded");
    }

    private Dependency createMockArtifact(String scope) {
        Dependency artifact = DependencyStubs.dependency("group", "artifact", "1.0", scope, "type", "");

        return artifact;
    }
}
