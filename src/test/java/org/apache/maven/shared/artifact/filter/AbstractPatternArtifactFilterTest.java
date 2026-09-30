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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import org.apache.maven.api.Dependency;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class AbstractPatternArtifactFilterTest {

    protected abstract Predicate<Dependency> createFilter(List<String> patterns);

    protected abstract boolean isInclusionNotExpected();

    @Test
    public void shouldTriggerBothPatternsWithWildcards() {
        final String groupId1 = "group";
        final String artifactId1 = "artifact";

        final String groupId2 = "group2";
        final String artifactId2 = "artifact2";

        Dependency artifact1 = mock(Dependency.class);
        when(artifact1.getGroupId()).thenReturn(groupId1);
        when(artifact1.getArtifactId()).thenReturn(artifactId1);
        when(artifact1.getType()).thenReturn(DependencyStubs.type("jar"));
        when(artifact1.getBaseVersion()).thenReturn(DependencyStubs.version("version"));

        Dependency artifact2 = mock(Dependency.class);
        when(artifact2.getGroupId()).thenReturn(groupId2);
        when(artifact2.getArtifactId()).thenReturn(artifactId2);
        when(artifact2.getType()).thenReturn(DependencyStubs.type("jar"));
        when(artifact2.getBaseVersion()).thenReturn(DependencyStubs.version("version"));

        final List<String> patterns = new ArrayList<>();
        patterns.add(groupId1 + ":" + artifactId1 + ":*");
        patterns.add(groupId2 + ":" + artifactId2 + ":*");

        final Predicate<Dependency> filter = createFilter(patterns);

        if (isInclusionNotExpected()) {
            assertFalse(filter.test(artifact1));
            assertFalse(filter.test(artifact2));
        } else {
            assertTrue(filter.test(artifact1));
            assertTrue(filter.test(artifact2));
        }
    }

    @Test
    public void shouldTriggerBothPatternsWithNonColonWildcards() {
        final String groupId1 = "group";
        final String artifactId1 = "artifact";

        final String groupId2 = "group2";
        final String artifactId2 = "artifact2";

        Dependency artifact1 = mock(Dependency.class);
        when(artifact1.getGroupId()).thenReturn(groupId1);
        when(artifact1.getArtifactId()).thenReturn(artifactId1);
        when(artifact1.getType()).thenReturn(DependencyStubs.type("jar"));
        when(artifact1.getBaseVersion()).thenReturn(DependencyStubs.version("version"));

        Dependency artifact2 = mock(Dependency.class);
        when(artifact2.getGroupId()).thenReturn(groupId2);
        when(artifact2.getArtifactId()).thenReturn(artifactId2);
        when(artifact2.getType()).thenReturn(DependencyStubs.type("jar"));
        when(artifact2.getBaseVersion()).thenReturn(DependencyStubs.version("version"));

        final List<String> patterns = new ArrayList<>();
        patterns.add(groupId1 + "*");
        patterns.add(groupId2 + "*");

        final Predicate<Dependency> filter = createFilter(patterns);

        if (isInclusionNotExpected()) {
            assertFalse(filter.test(artifact1));
            assertFalse(filter.test(artifact2));
        } else {
            assertTrue(filter.test(artifact1));
            assertTrue(filter.test(artifact2));
        }
    }

    @Test
    public void shouldIncludeDirectlyMatchedArtifactByGroupIdArtifactId() {
        final String groupId = "group";
        final String artifactId = "artifact";

        Dependency artifact = mock(Dependency.class);
        when(artifact.getGroupId()).thenReturn(groupId);
        when(artifact.getArtifactId()).thenReturn(artifactId);
        when(artifact.getType()).thenReturn(DependencyStubs.type("jar"));
        when(artifact.getBaseVersion()).thenReturn(DependencyStubs.version("version"));

        final Predicate<Dependency> filter = createFilter(Collections.singletonList(groupId + ":" + artifactId));

        if (isInclusionNotExpected()) {
            assertFalse(filter.test(artifact));
        } else {
            assertTrue(filter.test(artifact));
        }
    }

    @Test
    public void shouldIncludeDirectlyMatchedArtifactByDependencyConflictId() {
        final String groupId = "group";
        final String artifactId = "artifact";

        Dependency artifact = mock(Dependency.class);
        when(artifact.getGroupId()).thenReturn(groupId);
        when(artifact.getArtifactId()).thenReturn(artifactId);
        when(artifact.getType()).thenReturn(DependencyStubs.type("jar"));
        when(artifact.getBaseVersion()).thenReturn(DependencyStubs.version("version"));

        final Predicate<Dependency> filter =
                createFilter(Collections.singletonList(groupId + ":" + artifactId + ":jar"));

        if (isInclusionNotExpected()) {
            assertFalse(filter.test(artifact));
        } else {
            assertTrue(filter.test(artifact));
        }
    }

    @Test
    public void shouldNotIncludeWhenGroupIdDiffers() {
        final String groupId = "group";
        final String artifactId = "artifact";

        Dependency artifact = mock(Dependency.class);
        when(artifact.getGroupId()).thenReturn(groupId);
        when(artifact.getArtifactId()).thenReturn(artifactId);
        when(artifact.getType()).thenReturn(DependencyStubs.type("jar"));
        when(artifact.getBaseVersion()).thenReturn(DependencyStubs.version("version"));

        final List<String> patterns = new ArrayList<>();
        patterns.add("otherGroup:" + artifactId + ":jar");
        patterns.add("otherGroup:" + artifactId);

        final Predicate<Dependency> filter = createFilter(patterns);

        if (isInclusionNotExpected()) {
            assertTrue(filter.test(artifact));
        } else {
            assertFalse(filter.test(artifact));
        }
    }

    @Test
    public void shouldNotIncludeWhenArtifactIdDiffers() {
        final String groupId = "group";
        final String artifactId = "artifact";

        Dependency artifact = mock(Dependency.class);
        when(artifact.getGroupId()).thenReturn(groupId);
        when(artifact.getArtifactId()).thenReturn(artifactId);
        when(artifact.getType()).thenReturn(DependencyStubs.type("jar"));
        when(artifact.getBaseVersion()).thenReturn(DependencyStubs.version("version"));

        final List<String> patterns = new ArrayList<>();
        patterns.add(groupId + "otherArtifact:jar");
        patterns.add(groupId + "otherArtifact");

        final Predicate<Dependency> filter = createFilter(patterns);

        if (isInclusionNotExpected()) {
            assertTrue(filter.test(artifact));
        } else {
            assertFalse(filter.test(artifact));
        }
    }

    @Test
    public void shouldNotIncludeWhenBothIdElementsDiffer() {
        final String groupId = "group";
        final String artifactId = "artifact";

        Dependency artifact = mock(Dependency.class);
        when(artifact.getGroupId()).thenReturn(groupId);
        when(artifact.getArtifactId()).thenReturn(artifactId);
        when(artifact.getType()).thenReturn(DependencyStubs.type("jar"));
        when(artifact.getBaseVersion()).thenReturn(DependencyStubs.version("version"));

        final List<String> patterns = new ArrayList<>();
        patterns.add("otherGroup:otherArtifact:jar");
        patterns.add("otherGroup:otherArtifact");

        final Predicate<Dependency> filter = createFilter(patterns);

        if (isInclusionNotExpected()) {
            assertTrue(filter.test(artifact));
        } else {
            assertFalse(filter.test(artifact));
        }
    }

    @Test
    public void shouldNotIncludeWhenNegativeMatch() {
        final String groupId = "group";
        final String artifactId = "artifact";

        Dependency artifact = mock(Dependency.class);
        when(artifact.getGroupId()).thenReturn(groupId);
        when(artifact.getArtifactId()).thenReturn(artifactId);
        when(artifact.getType()).thenReturn(DependencyStubs.type("jar"));
        when(artifact.getBaseVersion()).thenReturn(DependencyStubs.version("version"));

        final List<String> patterns = new ArrayList<>();
        patterns.add("!group:artifact:jar");

        final Predicate<Dependency> filter = createFilter(patterns);

        if (isInclusionNotExpected()) {
            assertTrue(filter.test(artifact));
        } else {
            assertFalse(filter.test(artifact));
        }
    }

    @Test
    public void shouldIncludeWhenWildcardMatchesInsideSequence() {
        final String groupId = "group";
        final String artifactId = "artifact";

        Dependency artifact = mock(Dependency.class);
        when(artifact.getGroupId()).thenReturn(groupId);
        when(artifact.getArtifactId()).thenReturn(artifactId);
        when(artifact.getType()).thenReturn(DependencyStubs.type("jar"));
        when(artifact.getBaseVersion()).thenReturn(DependencyStubs.version("version"));

        final List<String> patterns = new ArrayList<>();
        patterns.add("group:*:jar");

        final Predicate<Dependency> filter = createFilter(patterns);

        if (isInclusionNotExpected()) {
            assertFalse(filter.test(artifact));
        } else {
            assertTrue(filter.test(artifact));
        }
    }

    @Test
    public void shouldIncludeWhenWildcardMatchesOutsideSequence() {
        final String groupId = "group";
        final String artifactId = "artifact";

        Dependency artifact = mock(Dependency.class);

        when(artifact.getGroupId()).thenReturn(groupId);
        when(artifact.getArtifactId()).thenReturn(artifactId);
        when(artifact.getType()).thenReturn(DependencyStubs.type("jar"));
        when(artifact.getBaseVersion()).thenReturn(DependencyStubs.version("version"));

        final List<String> patterns = new ArrayList<>();
        patterns.add("*:artifact:*");

        final Predicate<Dependency> filter = createFilter(patterns);

        if (isInclusionNotExpected()) {
            assertFalse(filter.test(artifact));
        } else {
            assertTrue(filter.test(artifact));
        }
    }

    @Test
    public void shouldIncludeWhenWildcardMatchesMiddleOfArtifactId() {
        final String groupId = "group";
        final String artifactId = "some-artifact-id";

        Dependency artifact = mock(Dependency.class);

        when(artifact.getGroupId()).thenReturn(groupId);
        when(artifact.getArtifactId()).thenReturn(artifactId);
        when(artifact.getType()).thenReturn(DependencyStubs.type("jar"));
        when(artifact.getBaseVersion()).thenReturn(DependencyStubs.version("version"));

        final List<String> patterns = new ArrayList<>();
        patterns.add("group:some-*-id");

        final Predicate<Dependency> filter = createFilter(patterns);

        if (isInclusionNotExpected()) {
            assertFalse(filter.test(artifact));
        } else {
            assertTrue(filter.test(artifact));
        }
    }

    @Test
    public void shouldIncludeWhenWildcardCoversPartOfGroupIdAndEverythingElse() {
        final String groupId = "some.group.id";
        final String artifactId = "some-artifact-id";

        Dependency artifact = mock(Dependency.class);

        when(artifact.getGroupId()).thenReturn(groupId);
        when(artifact.getArtifactId()).thenReturn(artifactId);
        when(artifact.getType()).thenReturn(DependencyStubs.type("jar"));
        when(artifact.getBaseVersion()).thenReturn(DependencyStubs.version("version"));

        final List<String> patterns = new ArrayList<>();
        patterns.add("some.group*");

        final Predicate<Dependency> filter = createFilter(patterns);

        if (isInclusionNotExpected()) {
            assertFalse(filter.test(artifact));
        } else {
            assertTrue(filter.test(artifact));
        }
    }

    @Test
    public void shouldIncludeTransitiveDependencyWhenWildcardMatchesButDoesntMatchParent() {
        final String groupId = "group";
        final String artifactId = "artifact";

        final String otherGroup = "otherGroup";
        final String otherArtifact = "otherArtifact";
        final String otherType = "ejb";

        final List<String> patterns = Collections.singletonList("*:jar:*");

        Dependency artifact1 = mock(Dependency.class);
        when(artifact1.getGroupId()).thenReturn(groupId);
        when(artifact1.getArtifactId()).thenReturn(artifactId);
        when(artifact1.getType()).thenReturn(DependencyStubs.type("jar"));
        when(artifact1.getBaseVersion()).thenReturn(DependencyStubs.version("version"));

        Dependency artifact2 = mock(Dependency.class);
        when(artifact2.getGroupId()).thenReturn(otherGroup);
        when(artifact2.getArtifactId()).thenReturn(otherArtifact);
        when(artifact2.getType()).thenReturn(DependencyStubs.type(otherType));
        when(artifact2.getBaseVersion()).thenReturn(DependencyStubs.version("version"));

        final Predicate<Dependency> filter = createFilter(patterns);

        if (isInclusionNotExpected()) {
            assertTrue(filter.test(artifact2));
            assertFalse(filter.test(artifact1));
        } else {
            assertFalse(filter.test(artifact2));
            assertTrue(filter.test(artifact1));
        }
    }

    @Test
    public void shouldIncludeJarsWithAndWithoutClassifier() {
        final String groupId = "com.mycompany.myproject";
        final String artifactId = "some-artifact-id";

        Dependency artifact = mock(Dependency.class);
        when(artifact.getGroupId()).thenReturn(groupId);
        when(artifact.getArtifactId()).thenReturn(artifactId);
        when(artifact.getType()).thenReturn(DependencyStubs.type("jar"));
        when(artifact.getBaseVersion()).thenReturn(DependencyStubs.version("version"));

        final List<String> patterns = new ArrayList<>();
        patterns.add("com.mycompany.*:*:jar:*:*");

        final Predicate<Dependency> filter = createFilter(patterns);

        if (isInclusionNotExpected()) {
            assertFalse(filter.test(artifact));
        } else {
            assertTrue(filter.test(artifact));
        }
    }

    @Test
    public void checkWithVersionRange() {
        final String groupId = "com.mycompany.myproject";
        final String artifactId = "some-artifact-id";

        Dependency artifact = mock(Dependency.class);
        when(artifact.getGroupId()).thenReturn(groupId);
        when(artifact.getArtifactId()).thenReturn(artifactId);
        when(artifact.getType()).thenReturn(DependencyStubs.type("jar"));
        when(artifact.getBaseVersion()).thenReturn(DependencyStubs.version("1.1"));

        final List<String> patterns = new ArrayList<>();
        patterns.add("com.mycompany.myproject:some-artifact-id:jar:*:[1.0,2.0)");

        final Predicate<Dependency> filter = createFilter(patterns);

        if (isInclusionNotExpected()) {
            assertFalse(filter.test(artifact));
        } else {
            assertTrue(filter.test(artifact));
        }
    }

    @Test
    public void checkMassembly955() {
        Dependency artifact1 = mock(Dependency.class);
        when(artifact1.getGroupId()).thenReturn("org.python");
        when(artifact1.getArtifactId()).thenReturn("jython-standalone");
        when(artifact1.getType()).thenReturn(DependencyStubs.type("jar"));
        when(artifact1.getBaseVersion()).thenReturn(DependencyStubs.version("1.0"));

        Dependency artifact2 = mock(Dependency.class);
        when(artifact2.getGroupId()).thenReturn("org.teiid");
        when(artifact2.getArtifactId()).thenReturn("teiid");
        when(artifact2.getType()).thenReturn(DependencyStubs.type("jar"));
        when(artifact2.getClassifier()).thenReturn("jdbc");
        when(artifact2.getBaseVersion()).thenReturn(DependencyStubs.version("1.0"));

        final List<String> patterns = new ArrayList<>();
        patterns.add("org.teiid:teiid:*:jdbc:*");
        patterns.add("org.python:jython-standalone");

        final Predicate<Dependency> filter = createFilter(patterns);

        if (isInclusionNotExpected()) {
            assertFalse(filter.test(artifact1));
            assertFalse(filter.test(artifact2));
        } else {
            assertTrue(filter.test(artifact1));
            assertTrue(filter.test(artifact2));
        }
    }

    @Test
    public void partialWildcardShouldNotMatchEmptyComponent() {
        Dependency artifact =
                DependencyStubs.dependency("test-group", "test-artifact", "test-version", "compile", "jar", "");

        Predicate<Dependency> filter = createFilter(Collections.singletonList("test-group:test-artifact:*:ERROR*"));

        if (isInclusionNotExpected()) {
            assertTrue(filter.test(artifact));
        } else {
            assertFalse(filter.test(artifact));
        }
    }
}
