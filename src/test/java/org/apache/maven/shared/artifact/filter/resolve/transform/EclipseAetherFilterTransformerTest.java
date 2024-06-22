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

import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.shared.artifact.filter.resolve.AbstractFilter;
import org.apache.maven.shared.artifact.filter.resolve.AndFilter;
import org.apache.maven.shared.artifact.filter.resolve.ExclusionsFilter;
import org.apache.maven.shared.artifact.filter.resolve.Node;
import org.apache.maven.shared.artifact.filter.resolve.OrFilter;
import org.apache.maven.shared.artifact.filter.resolve.PatternExclusionsFilter;
import org.apache.maven.shared.artifact.filter.resolve.PatternInclusionsFilter;
import org.apache.maven.shared.artifact.filter.resolve.ScopeFilter;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.DefaultDependencyNode;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.util.filter.AndDependencyFilter;
import org.eclipse.aether.util.filter.ExclusionsDependencyFilter;
import org.eclipse.aether.util.filter.OrDependencyFilter;
import org.eclipse.aether.util.filter.PatternExclusionsDependencyFilter;
import org.eclipse.aether.util.filter.PatternInclusionsDependencyFilter;
import org.eclipse.aether.util.filter.ScopeDependencyFilter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EclipseAetherFilterTransformerTest {
    private final EclipseAetherFilterTransformer transformer = new EclipseAetherFilterTransformer();

    @Test
    void checkTransformAndFilter() {
        AndFilter filter = new AndFilter(Arrays.asList(
                ScopeFilter.including("compile"), new ExclusionsFilter(Collections.singletonList("x:a"))));

        AndDependencyFilter dependencyFilter = (AndDependencyFilter) filter.transform(transformer);

        assertTrue(dependencyFilter.accept(newDependencyNode("g:a:v", "compile"), Collections.emptyList()));

        assertFalse(dependencyFilter.accept(newDependencyNode("x:a:v", "compile"), Collections.emptyList()));

        assertFalse(dependencyFilter.accept(newDependencyNode("g:a:v", "test"), Collections.emptyList()));

        assertFalse(dependencyFilter.accept(newDependencyNode("x:a:v", "test"), Collections.emptyList()));
    }

    @Test
    void checkTransformExclusionsFilter() {
        ExclusionsFilter filter = new ExclusionsFilter(Collections.singletonList("x:a"));

        ExclusionsDependencyFilter dependencyFilter = (ExclusionsDependencyFilter) filter.transform(transformer);

        assertTrue(dependencyFilter.accept(newDependencyNode("g:a:v", "compile"), Collections.emptyList()));

        assertFalse(dependencyFilter.accept(newDependencyNode("x:a:v", "compile"), Collections.emptyList()));
    }

    @Test
    void checkTransformOrFilter() {
        OrFilter filter = new OrFilter(Arrays.asList(ScopeFilter.including("compile"), ScopeFilter.including("test")));

        OrDependencyFilter dependencyFilter = (OrDependencyFilter) filter.transform(transformer);

        assertTrue(dependencyFilter.accept(newDependencyNode("g:a:v", "compile"), Collections.emptyList()));

        assertTrue(dependencyFilter.accept(newDependencyNode("g:a:v", "test"), Collections.emptyList()));

        assertFalse(dependencyFilter.accept(newDependencyNode("g:a:v", "runtime"), Collections.emptyList()));
    }

    @Test
    void checkTransformScopeFilter() {
        ScopeFilter filter = ScopeFilter.including(Collections.singletonList("runtime"));

        ScopeDependencyFilter dependencyFilter = (ScopeDependencyFilter) filter.transform(transformer);

        assertTrue(dependencyFilter.accept(newDependencyNode("g:a:v", "runtime"), Collections.emptyList()));

        assertFalse(dependencyFilter.accept(newDependencyNode("g:a:v", "compile"), Collections.emptyList()));

        assertFalse(dependencyFilter.accept(newDependencyNode("g:a:v", "test"), Collections.emptyList()));
    }

    @Test
    void checkTransformPatternExclusionsFilter() {
        PatternExclusionsFilter filter = new PatternExclusionsFilter(Collections.singletonList("x:*"));

        PatternExclusionsDependencyFilter dependencyFilter =
                (PatternExclusionsDependencyFilter) filter.transform(transformer);

        assertTrue(dependencyFilter.accept(newDependencyNode("g:a:v", "runtime"), Collections.emptyList()));

        assertFalse(dependencyFilter.accept(newDependencyNode("x:a:v", "runtime"), Collections.emptyList()));
    }

    @Test
    void checkTransformPatternInclusionsFilter() {
        PatternInclusionsFilter filter = new PatternInclusionsFilter(Collections.singletonList("g:*"));

        PatternInclusionsDependencyFilter dependencyFilter =
                (PatternInclusionsDependencyFilter) filter.transform(transformer);

        assertTrue(dependencyFilter.accept(newDependencyNode("g:a:v", "runtime"), Collections.emptyList()));

        assertFalse(dependencyFilter.accept(newDependencyNode("x:a:v", "runtime"), Collections.emptyList()));
    }

    @Test
    void checkTransformClassifierPatternInclusionsFilter() {
        PatternInclusionsFilter filter = new PatternInclusionsFilter(Collections.singletonList("g:*:*:c:*"));

        DependencyFilter dependencyFilter = filter.transform(transformer);

        assertTrue(dependencyFilter.accept(newDependencyNode("g:a:v:c:*", "runtime"), Collections.emptyList()));

        assertFalse(dependencyFilter.accept(newDependencyNode("g:a:v", "runtime"), Collections.emptyList()));

        assertFalse(dependencyFilter.accept(newDependencyNode("x:a:v:c:*", "runtime"), Collections.emptyList()));
    }

    @Test
    void checkTransformAbstractFilter() {
        AbstractFilter snapshotFilter = new AbstractFilter() {
            @Override
            public boolean accept(Node node, List<Node> parents) {
                return ArtifactUtils.isSnapshot(node.getDependency().getVersion());
            }
        };

        DependencyFilter dependencyFilter = snapshotFilter.transform(transformer);

        assertTrue(dependencyFilter.accept(newDependencyNode("g:a:1.0-SNAPSHOT", "compile"), Collections.emptyList()));

        assertFalse(dependencyFilter.accept(newDependencyNode("g:a:1.0", "compile"), Collections.emptyList()));
    }

    private DependencyNode newDependencyNode(String string, String scope) {
        return new DefaultDependencyNode(new Dependency(new DefaultArtifact(string), scope));
    }
}
