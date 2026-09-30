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

import java.util.HashSet;
import java.util.Set;

import org.apache.maven.api.Dependency;
import org.apache.maven.api.DependencyCoordinates;
import org.apache.maven.api.DependencyScope;
import org.apache.maven.api.Language;
import org.apache.maven.api.PathType;
import org.apache.maven.api.Type;
import org.apache.maven.api.Version;
import org.eclipse.aether.util.version.GenericVersionScheme;
import org.eclipse.aether.version.InvalidVersionSpecificationException;

/**
 * Test doubles for the Maven 4 API, which has no public factory for {@link Dependency}, {@link Type} or
 * {@link Version} outside of a {@code Session}. Replaces {@code ArtifactStubFactory} of the Maven 3 plugin testing
 * harness.
 */
public final class DependencyStubs {
    private static final GenericVersionScheme SCHEME = new GenericVersionScheme();

    private DependencyStubs() {}

    public static Type type(String id) {
        return new StubType(id);
    }

    public static Version version(String version) {
        return new StubVersion(version);
    }

    public static Dependency dependency(
            String groupId, String artifactId, String version, String scope, String type, String classifier) {
        return dependency(groupId, artifactId, version, scope, type, classifier, false);
    }

    public static Dependency dependency(
            String groupId,
            String artifactId,
            String version,
            String scope,
            String type,
            String classifier,
            boolean optional) {
        return new StubDependency(groupId, artifactId, version, scope, type, classifier, optional);
    }

    public static Dependency dependency(String groupId, String artifactId, String version, String scope) {
        return dependency(groupId, artifactId, version, scope, "jar", "");
    }

    public static Set<Dependency> scopedDependencies() {
        Set<Dependency> set = new HashSet<>();
        for (String scope : new String[] {"compile", "provided", "test", "runtime", "system"}) {
            set.add(dependency("g", scope, "1.0", scope));
        }
        return set;
    }

    public static Set<Dependency> typedDependencies() {
        Set<Dependency> set = new HashSet<>();
        set.add(dependency("g", "a", "1.0", "compile", "war", ""));
        set.add(dependency("g", "b", "1.0", "compile", "jar", ""));
        set.add(dependency("g", "c", "1.0", "compile", "sources", ""));
        set.add(dependency("g", "d", "1.0", "compile", "zip", ""));
        set.add(dependency("g", "e", "1.0", "compile", "rar", ""));
        return set;
    }

    public static Set<Dependency> classifiedDependencies() {
        Set<Dependency> set = new HashSet<>();
        set.add(dependency("g", "a", "1.0", "compile", "jar", "one"));
        set.add(dependency("g", "b", "1.0", "compile", "jar", "two"));
        set.add(dependency("g", "c", "1.0", "compile", "jar", "three"));
        set.add(dependency("g", "d", "1.0", "compile", "jar", "four"));
        return set;
    }

    public static Set<Dependency> artifactIdDependencies() {
        Set<Dependency> set = new HashSet<>();
        for (String id : new String[] {"one", "two", "three", "four"}) {
            set.add(dependency("g", id, "1.0", "compile", "jar", "a"));
        }
        return set;
    }

    public static Set<Dependency> groupIdDependencies() {
        Set<Dependency> set = new HashSet<>();
        for (String id : new String[] {"one", "two", "three", "four"}) {
            set.add(dependency(id, "group-" + id, "1.0", "compile", "jar", "a"));
        }
        return set;
    }

    public static Set<Dependency> releaseAndSnapshotDependencies() {
        Set<Dependency> set = new HashSet<>();
        set.add(dependency("testGroupId", "release", "1.0", "compile"));
        set.add(dependency("testGroupId", "snapshot", "2.0-SNAPSHOT", "compile"));
        return set;
    }

    private static final class StubType implements Type {
        private final String id;

        StubType(String id) {
            this.id = id;
        }

        @Override
        public String id() {
            return id;
        }

        @Override
        public Language getLanguage() {
            return null;
        }

        @Override
        public String getExtension() {
            return id;
        }

        @Override
        public String getClassifier() {
            return null;
        }

        @Override
        public boolean isIncludesDependencies() {
            return false;
        }

        @Override
        public Set<PathType> getPathTypes() {
            return Set.of();
        }

        @Override
        public String toString() {
            return id;
        }
    }

    private static final class StubVersion implements Version {
        private final String version;

        StubVersion(String version) {
            this.version = version;
        }

        @Override
        public int compareTo(Version other) {
            try {
                return SCHEME.parseVersion(version).compareTo(SCHEME.parseVersion(other.toString()));
            } catch (InvalidVersionSpecificationException e) {
                throw new IllegalArgumentException(e);
            }
        }

        @Override
        public String toString() {
            return version;
        }
    }

    private static final class StubDependency implements Dependency {
        private final String groupId;
        private final String artifactId;
        private final String version;
        private final String scope;
        private final String type;
        private final String classifier;
        private final boolean optional;

        StubDependency(
                String groupId,
                String artifactId,
                String version,
                String scope,
                String type,
                String classifier,
                boolean optional) {
            this.optional = optional;
            this.groupId = groupId;
            this.artifactId = artifactId;
            this.version = version;
            this.scope = scope;
            this.type = type;
            this.classifier = classifier;
        }

        @Override
        public String getGroupId() {
            return groupId;
        }

        @Override
        public String getArtifactId() {
            return artifactId;
        }

        @Override
        public Version getVersion() {
            return new StubVersion(version);
        }

        @Override
        public Version getBaseVersion() {
            return new StubVersion(version.replaceFirst("-\\d{8}\\.\\d{6}-\\d+$", "-SNAPSHOT"));
        }

        @Override
        public String getClassifier() {
            return classifier;
        }

        @Override
        public String getExtension() {
            return type;
        }

        @Override
        public boolean isSnapshot() {
            return version.endsWith("-SNAPSHOT");
        }

        @Override
        public Type getType() {
            return new StubType(type);
        }

        @Override
        public DependencyScope getScope() {
            return scope == null ? null : DependencyScope.forId(scope);
        }

        @Override
        public boolean isOptional() {
            return optional;
        }

        @Override
        public DependencyCoordinates toCoordinates() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String key() {
            return groupId + ":" + artifactId + ":" + type + ":" + classifier + ":" + version;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof StubDependency && key().equals(((StubDependency) o).key());
        }

        @Override
        public int hashCode() {
            return key().hashCode();
        }

        @Override
        public String toString() {
            return key() + ":" + scope;
        }
    }
}
