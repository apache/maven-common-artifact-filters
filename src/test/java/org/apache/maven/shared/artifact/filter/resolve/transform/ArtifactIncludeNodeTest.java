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

import org.apache.maven.api.Dependency;
import org.apache.maven.shared.artifact.filter.DependencyStubs;
import org.apache.maven.shared.artifact.filter.resolve.Node;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArtifactIncludeNodeTest {
    @Test
    void checkGav() throws Exception {
        Node node = new ArtifactIncludeNode(newArtifact("g:a:v", null));

        org.apache.maven.api.model.Dependency dependency = node.getDependency();

        assertEquals("g", dependency.getGroupId());
        assertEquals("a", dependency.getArtifactId());
        assertEquals("v", dependency.getVersion());
        assertNull(dependency.getClassifier());
        // This is different compared to AetherNodes. Here it's based on artifact, which in the end always has a type.
        assertEquals("jar", dependency.getType());
    }

    @Test
    void checkClassifier() throws Exception {
        Node node = new ArtifactIncludeNode(newArtifact("g:a::c:v", null));

        org.apache.maven.api.model.Dependency dependency = node.getDependency();

        assertEquals("g", dependency.getGroupId());
        assertEquals("a", dependency.getArtifactId());
        assertEquals("v", dependency.getVersion());
        assertEquals("c", dependency.getClassifier());
        assertEquals("", dependency.getType());
    }

    @Test
    void checkType() throws Exception {
        Node node = new ArtifactIncludeNode(newArtifact("g:a:pom:v", null));

        org.apache.maven.api.model.Dependency dependency = node.getDependency();

        assertEquals("g", dependency.getGroupId());
        assertEquals("a", dependency.getArtifactId());
        assertEquals("v", dependency.getVersion());
        assertNull(dependency.getClassifier());
        assertEquals("pom", dependency.getType());
    }

    @Test
    void checkScope() throws Exception {
        Node node = new ArtifactIncludeNode(newArtifact("g:a:v", "runtime"));

        org.apache.maven.api.model.Dependency dependency = node.getDependency();

        assertEquals("g", dependency.getGroupId());
        assertEquals("a", dependency.getArtifactId());
        assertEquals("v", dependency.getVersion());
        assertNull(dependency.getClassifier());
        assertEquals("jar", dependency.getType());
        assertEquals("runtime", dependency.getScope());
    }

    @Test
    void checkOptional() throws Exception {
        Node node = new ArtifactIncludeNode(newArtifact("g:a:pom:v", null, null));

        assertEquals("false", node.getDependency().getOptional());
        assertFalse(Boolean.parseBoolean(node.getDependency().getOptional()));

        node = new ArtifactIncludeNode(newArtifact("g:a:pom:v", null, true));
        assertEquals("true", node.getDependency().getOptional());
        assertTrue(Boolean.parseBoolean(node.getDependency().getOptional()));

        node = new ArtifactIncludeNode(newArtifact("g:a:pom:v", null, false));
        assertEquals("false", node.getDependency().getOptional());
        assertFalse(Boolean.parseBoolean(node.getDependency().getOptional()));
    }

    private Dependency newArtifact(String coor, String scope) throws Exception {
        return newArtifact(coor, scope, null);
    }

    private Dependency newArtifact(String coor, String scope, Boolean optional) throws Exception {
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

        return DependencyStubs.dependency(
                groupId,
                artifactId,
                version,
                scope,
                type == null ? "jar" : type,
                classifier == null ? "" : classifier,
                Boolean.TRUE.equals(optional));
    }
}
