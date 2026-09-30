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
package org.apache.maven.shared.artifact.filter.resolve.transform;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import org.apache.maven.api.Dependency;
import org.apache.maven.shared.artifact.filter.DependencyStubs;
import org.apache.maven.shared.artifact.filter.PatternExcludesArtifactFilter;
import org.apache.maven.shared.artifact.filter.PatternIncludesArtifactFilter;
import org.apache.maven.shared.artifact.filter.resolve.AbstractFilter;
import org.apache.maven.shared.artifact.filter.resolve.AndFilter;
import org.apache.maven.shared.artifact.filter.resolve.ExclusionsFilter;
import org.apache.maven.shared.artifact.filter.resolve.Node;
import org.apache.maven.shared.artifact.filter.resolve.OrFilter;
import org.apache.maven.shared.artifact.filter.resolve.PatternExclusionsFilter;
import org.apache.maven.shared.artifact.filter.resolve.PatternInclusionsFilter;
import org.apache.maven.shared.artifact.filter.resolve.ScopeFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArtifactIncludeFilterTransformerTest {

    private ArtifactIncludeFilterTransformer transformer;

    @BeforeEach
    void setUp() {
        transformer = new ArtifactIncludeFilterTransformer();
    }

    @Test
    void checkTransformAndFilter() throws Exception {
        AndFilter filter = new AndFilter(Arrays.asList(
                ScopeFilter.including("compile"), new ExclusionsFilter(Collections.singletonList("x:a"))));

        Predicate<Dependency> dependencyFilter = filter.transform(transformer);

        assertTrue(dependencyFilter.test(newArtifact("g:a:v", "compile")));

        assertFalse(dependencyFilter.test(newArtifact("x:a:v", "compile")));

        assertFalse(dependencyFilter.test(newArtifact("g:a:v", "test")));

        assertFalse(dependencyFilter.test(newArtifact("x:a:v", "test")));
    }

    @Test
    void checkTransformExclusionsFilter() throws Exception {
        ExclusionsFilter filter = new ExclusionsFilter(Collections.singletonList("x:a"));

        Predicate<Dependency> dependencyFilter = filter.transform(transformer);

        assertTrue(dependencyFilter.test(newArtifact("g:a:v", "compile")));

        assertFalse(dependencyFilter.test(newArtifact("x:a:v", "compile")));
    }

    @Test
    void checkTransformOrFilter() throws Exception {
        OrFilter filter = new OrFilter(Arrays.asList(ScopeFilter.including("compile"), ScopeFilter.including("test")));

        Predicate<Dependency> dependencyFilter = filter.transform(transformer);

        assertTrue(dependencyFilter.test(newArtifact("g:a:v", "compile")));

        assertTrue(dependencyFilter.test(newArtifact("g:a:v", "test")));

        assertFalse(dependencyFilter.test(newArtifact("g:a:v", "runtime")));
    }

    @Test
    void checkTransformScopeFilter() throws Exception {
        ScopeFilter filter = ScopeFilter.including(Collections.singletonList("runtime"));

        Predicate<Dependency> dependencyFilter = filter.transform(transformer);

        assertTrue(dependencyFilter.test(newArtifact("g:a:v", "runtime")));

        assertFalse(dependencyFilter.test(newArtifact("g:a:v", "compile")));

        assertFalse(dependencyFilter.test(newArtifact("g:a:v", "test")));
    }

    @Test
    void checkTransformScopeFilterIncludeNullScope() throws Exception {
        ScopeFilter filter = ScopeFilter.including();

        Dependency artifact = newArtifact("g:a:v", null);

        // default
        assertTrue(filter.transform(transformer).test(artifact));

        transformer.setIncludeNullScope(false);

        assertFalse(filter.transform(transformer).test(artifact));
    }

    @Test
    void checkTransformPatternExclusionsFilter() throws Exception {
        PatternExclusionsFilter filter = new PatternExclusionsFilter(Collections.singletonList("x:*"));

        PatternExcludesArtifactFilter dependencyFilter = (PatternExcludesArtifactFilter) filter.transform(transformer);

        assertTrue(dependencyFilter.test(newArtifact("g:a:v", "runtime")));

        assertFalse(dependencyFilter.test(newArtifact("x:a:v", "runtime")));
    }

    @Test
    void checkTransformPatternInclusionsFilter() throws Exception {
        PatternInclusionsFilter filter = new PatternInclusionsFilter(Collections.singletonList("g:*"));

        PatternIncludesArtifactFilter dependencyFilter = (PatternIncludesArtifactFilter) filter.transform(transformer);

        assertTrue(dependencyFilter.test(newArtifact("g:a:v", "runtime")));

        assertFalse(dependencyFilter.test(newArtifact("x:a:v", "runtime")));
    }

    @Test
    void checkTransformAbstractFilter() throws Exception {
        AbstractFilter snapshotFilter = new AbstractFilter() {
            @Override
            public boolean accept(Node node, List<Node> parents) {
                return node.getDependency().getVersion().endsWith("-SNAPSHOT");
            }
        };

        Predicate<Dependency> dependencyFilter = snapshotFilter.transform(transformer);

        assertTrue(dependencyFilter.test(newArtifact("g:a:1.0-SNAPSHOT", "compile")));

        assertFalse(dependencyFilter.test(newArtifact("g:a:1.0", "compile")));
    }

    private Dependency newArtifact(String coor, String scope) throws Exception {
        String[] gav = coor.split(":");
        return DependencyStubs.dependency(gav[0], gav[1], gav[2], scope);
    }
}
