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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.testing.ArtifactStubFactory;
import org.apache.maven.shared.artifact.filter.resolve.Node;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


class ArtifactIncludeNodeTest {
    private final ArtifactStubFactory artifactFactory = new ArtifactStubFactory();

    @Test
    void checkGav() throws Exception {
        Node node = new ArtifactIncludeNode(newArtifact("g:a:v", null));

        Dependency dependency = node.getDependency();

        assertEquals("g", dependency.getGroupId());
        assertEquals("a", dependency.getArtifactId());
        assertEquals("v", dependency.getVersion());
        assertEquals("", dependency.getClassifier());
        // This is different compared to AetherNodes. Here it's based on artifact, which in the end always has a type.
        assertEquals("jar", dependency.getType());
    }

    @Test
    void checkClassifier() throws Exception {
        Node node = new ArtifactIncludeNode(newArtifact("g:a::c:v", null));

        Dependency dependency = node.getDependency();

        assertEquals("g", dependency.getGroupId());
        assertEquals("a", dependency.getArtifactId());
        assertEquals("v", dependency.getVersion());
        assertEquals("c", dependency.getClassifier());
        // empty type stays empty type when using ArtifactStubFactory
        assertEquals("", dependency.getType());
    }

    @Test
    void checkType() throws Exception {
        Node node = new ArtifactIncludeNode(newArtifact("g:a:pom:v", null));

        Dependency dependency = node.getDependency();

        assertEquals("g", dependency.getGroupId());
        assertEquals("a", dependency.getArtifactId());
        assertEquals("v", dependency.getVersion());
        assertNull(dependency.getClassifier());
        assertEquals("pom", dependency.getType());
    }

    @Test
    void checkScope() throws Exception {
        Node node = new ArtifactIncludeNode(newArtifact("g:a:v", "s"));

        Dependency dependency = node.getDependency();

        assertEquals("g", dependency.getGroupId());
        assertEquals("a", dependency.getArtifactId());
        assertEquals("v", dependency.getVersion());
        assertEquals("", dependency.getClassifier());
        assertEquals("jar", dependency.getType());
        assertEquals("s", dependency.getScope());
    }

    @Test
    void checkOptional() throws Exception {
        Node node = new ArtifactIncludeNode(newArtifact("g:a:pom:v", null, null));

        assertEquals("false", node.getDependency().getOptional());
        assertFalse(node.getDependency().isOptional());

        node = new ArtifactIncludeNode(newArtifact("g:a:pom:v", null, true));
        assertEquals("true", node.getDependency().getOptional());
        assertTrue(node.getDependency().isOptional());

        node = new ArtifactIncludeNode(newArtifact("g:a:pom:v", null, false));
        assertEquals("false", node.getDependency().getOptional());
        assertFalse(node.getDependency().isOptional());
    }

    private Artifact newArtifact(String coor, String scope) throws Exception {
        return newArtifact(coor, scope, null);
    }

    private Artifact newArtifact(String coor, String scope, Boolean optional) throws Exception {
        String[] gav = coor.split(":");
        String groupId = gav[0];
        String artifactId = gav[1];
        String version = null;
        String classifier = null;
        String type = null;

        if (gav.length == 3) {
            version = gav[2];
        } else if (gav.length == 4) {
            type = gav[2];
            version = gav[3];
        } else if (gav.length == 5) {
            type = gav[2];
            classifier = gav[3];
            version = gav[4];
        }

        if (optional != null) {
            VersionRange versionRange = VersionRange.createFromVersion(version);
            return artifactFactory.createArtifact(groupId, artifactId, versionRange, scope, type, classifier, optional);
        } else if (gav.length == 3) {
            return artifactFactory.createArtifact(groupId, artifactId, version, scope);
        } else if (gav.length == 4) {
            return artifactFactory.createArtifact(groupId, artifactId, version, scope, type, null);
        } else if (gav.length == 5) {
            return artifactFactory.createArtifact(groupId, artifactId, version, scope, type, classifier);
        } else {
            throw new IllegalArgumentException("Can't translate coor to an Artifact");
        }
    }
}
