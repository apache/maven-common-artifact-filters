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
package org.apache.maven.shared.artifact.filter.collection;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.maven.api.Artifact;
import org.apache.maven.api.Dependency;
import org.apache.maven.api.Node;
import org.apache.maven.api.PathScope;
import org.apache.maven.api.Session;
import org.apache.maven.shared.artifact.filter.internal.Utils;

/**
 * This filter will exclude everything that is not a dependency of the selected artifact.
 *
 * @author <a href="mailto:brianf@apache.org">Brian Fox</a>
 */
public class ArtifactTransitivityFilter extends AbstractArtifactsFilter {
    /**
     * List of conflict ids of transitiveArtifacts
     */
    private final Set<String> transitiveArtifacts = new HashSet<>();

    /**
     * Collects the dependencies of the given artifact through the session, using the
     * {@link PathScope#TEST_RUNTIME test runtime} scope so that no scope is left out.
     *
     * @param session  the current session, in a Mojo obtained by injecting {@link Session}
     * @param artifact the artifact to resolve the dependencies from
     */
    public ArtifactTransitivityFilter(Session session, Artifact artifact) {
        Node root = session.collectDependencies(artifact, PathScope.TEST_RUNTIME);

        for (Node node : session.flattenDependencies(root, PathScope.TEST_RUNTIME)) {
            Dependency dependency = node.getDependency();
            if (node != root && dependency != null) {
                transitiveArtifacts.add(Utils.getConflictId(dependency));
            }
        }
    }

    /** {@inheritDoc} */
    public Set<Dependency> filter(Set<Dependency> artifacts) {
        Set<Dependency> result = new LinkedHashSet<>();
        for (Dependency artifact : artifacts) {
            if (artifactIsATransitiveDependency(artifact)) {
                result.add(artifact);
            }
        }
        return result;
    }

    /**
     * Compares the artifact to the list of dependencies to see if it is directly included by this project
     *
     * @param artifact representing the item to compare.
     * @return true if artifact is a transitive dependency
     */
    public boolean artifactIsATransitiveDependency(Dependency artifact) {
        return transitiveArtifacts.contains(Utils.getConflictId(artifact));
    }
}
