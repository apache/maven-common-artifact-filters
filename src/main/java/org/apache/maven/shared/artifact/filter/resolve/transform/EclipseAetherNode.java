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

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.shared.artifact.filter.resolve.Node;
import org.eclipse.aether.artifact.ArtifactProperties;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.graph.Exclusion;

/**
 * Adapter of an Eclipse Aether DependencyNode for common Node
 *
 * @author Robert Scholte
 * @since 3.0
 */
class EclipseAetherNode implements Node {

    private final DependencyNode node;

    EclipseAetherNode(DependencyNode node) {
        this.node = node;
    }

    /** {@inheritDoc} */
    @Override
    public org.apache.maven.api.model.Dependency getDependency() {
        Dependency nodeDependency = node.getDependency();

        if (nodeDependency == null) {
            return null;
        }

        org.apache.maven.api.model.Dependency.Builder builder = org.apache.maven.api.model.Dependency.newBuilder()
                .groupId(nodeDependency.getArtifact().getGroupId())
                .artifactId(nodeDependency.getArtifact().getArtifactId())
                .version(nodeDependency.getArtifact().getVersion())
                .classifier(nodeDependency.getArtifact().getClassifier())
                .type(nodeDependency.getArtifact().getProperty(ArtifactProperties.TYPE, null))
                .scope(nodeDependency.getScope());
        // Eclipse Aether supports three-valued logic
        if (nodeDependency.getOptional() != null) {
            builder.optional(String.valueOf(nodeDependency.isOptional()));
        }
        if (nodeDependency.getExclusions() != null) {
            builder.exclusions(getExclusions(nodeDependency));
        }

        return builder.build();
    }

    private static List<org.apache.maven.api.model.Exclusion> getExclusions(Dependency nodeDependency) {
        List<org.apache.maven.api.model.Exclusion> mavenExclusions =
                new ArrayList<>(nodeDependency.getExclusions().size());

        for (Exclusion aetherExclusion : nodeDependency.getExclusions()) {
            // that's all folks, although Aether has more metadata
            mavenExclusions.add(org.apache.maven.api.model.Exclusion.newBuilder()
                    .groupId(aetherExclusion.getGroupId())
                    .artifactId(aetherExclusion.getArtifactId())
                    .build());
        }
        return mavenExclusions;
    }
}
