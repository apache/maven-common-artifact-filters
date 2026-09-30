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
import org.apache.maven.api.model.Dependency.Builder;
import org.apache.maven.shared.artifact.filter.internal.Utils;
import org.apache.maven.shared.artifact.filter.resolve.Node;

/**
 *
 * @author Robert Scholte
 * @since 3.0
 */
class ArtifactIncludeNode implements Node {
    private final Dependency artifact;

    ArtifactIncludeNode(Dependency artifact) {
        this.artifact = artifact;
    }

    /**
     * {@inheritDoc}
     *
     * Note: an artifact doesn't contain exclusion information, so it won't be available here.
     * When required switch to filtering based on Aether.
     * @see EclipseAetherNode
     */
    @Override
    public org.apache.maven.api.model.Dependency getDependency() {
        // no exclusions possible
        Builder builder = org.apache.maven.api.model.Dependency.newBuilder()
                .groupId(artifact.getGroupId())
                .artifactId(artifact.getArtifactId())
                .version(artifact.getVersion().toString())
                .classifier(Utils.getClassifier(artifact))
                .type(artifact.getType().id())
                .scope(artifact.getScope() == null ? null : artifact.getScope().id())
                .optional(String.valueOf(artifact.isOptional()));

        return builder.build();
    }
}
