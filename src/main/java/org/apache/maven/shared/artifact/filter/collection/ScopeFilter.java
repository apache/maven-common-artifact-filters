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

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.maven.api.Dependency;
import org.apache.maven.api.DependencyScope;
import org.apache.maven.shared.artifact.filter.ScopeArtifactFilter;

import static org.apache.maven.shared.artifact.filter.internal.Utils.isNotEmpty;

/**
 * <p>ScopeFilter class.</p>
 *
 * @author <a href="mailto:brianf@apache.org">Brian Fox</a>
 */
public class ScopeFilter extends AbstractArtifactsFilter {

    private String includeScope;

    private String excludeScope;

    /**
     * <p>Constructor for ScopeFilter.</p>
     *
     * @param includeScope the scope to be included.
     * @param excludeScope the scope to be excluded.
     */
    public ScopeFilter(String includeScope, String excludeScope) {
        this.includeScope = includeScope;
        this.excludeScope = excludeScope;
    }

    /**
     * {@inheritDoc}
     *
     * This function determines if filtering needs to be performed. Excludes are
     * ignored if Includes are used.
     */
    public Set<Dependency> filter(Set<Dependency> artifacts) throws ArtifactFilterException {
        Set<Dependency> results = artifacts;

        if (isNotEmpty(includeScope)) {
            if (!DependencyScope.COMPILE.id().equals(includeScope)
                    && !DependencyScope.TEST.id().equals(includeScope)
                    && !DependencyScope.PROVIDED.id().equals(includeScope)
                    && !DependencyScope.RUNTIME.id().equals(includeScope)
                    && !DependencyScope.SYSTEM.id().equals(includeScope)) {
                throw new ArtifactFilterException("Invalid Scope in includeScope: " + includeScope);
            }

            results = new LinkedHashSet<>();

            if (DependencyScope.PROVIDED.id().equals(includeScope)
                    || DependencyScope.SYSTEM.id().equals(includeScope)) {
                results = includeSingleScope(artifacts, includeScope);
            } else {
                ScopeArtifactFilter saf = new ScopeArtifactFilter(includeScope);

                for (Dependency artifact : artifacts) {
                    if (saf.test(artifact)) {
                        results.add(artifact);
                    }
                }
            }
        } else if (isNotEmpty(excludeScope)) {
            if (!DependencyScope.COMPILE.id().equals(excludeScope)
                    && !DependencyScope.TEST.id().equals(excludeScope)
                    && !DependencyScope.PROVIDED.id().equals(excludeScope)
                    && !DependencyScope.RUNTIME.id().equals(excludeScope)
                    && !DependencyScope.SYSTEM.id().equals(excludeScope)) {
                throw new ArtifactFilterException("Invalid Scope in excludeScope: " + excludeScope);
            }
            results = new LinkedHashSet<>();
            // plexus ScopeArtifactFilter doesn't handle the provided scope so
            // we
            // need special handling for it.
            if (DependencyScope.TEST.id().equals(excludeScope)) {
                throw new ArtifactFilterException(" Can't exclude Test scope, this will exclude everything.");
            } else if (!DependencyScope.PROVIDED.id().equals(excludeScope)
                    && !DependencyScope.SYSTEM.id().equals(excludeScope)) {
                ScopeArtifactFilter saf = new ScopeArtifactFilter(excludeScope);

                for (Dependency artifact : artifacts) {
                    if (!saf.test(artifact)) {
                        results.add(artifact);
                    }
                }
            } else {
                results = excludeSingleScope(artifacts, excludeScope);
            }
        }

        return results;
    }

    private Set<Dependency> includeSingleScope(Set<Dependency> artifacts, String scope) {
        Set<Dependency> results = new LinkedHashSet<>();
        for (Dependency artifact : artifacts) {
            if (scope.equals(artifact.getScope().id())) {
                results.add(artifact);
            }
        }
        return results;
    }

    private Set<Dependency> excludeSingleScope(Set<Dependency> artifacts, String scope) {
        Set<Dependency> results = new LinkedHashSet<>();
        for (Dependency artifact : artifacts) {
            if (!scope.equals(artifact.getScope().id())) {
                results.add(artifact);
            }
        }
        return results;
    }

    /**
     * <p>Getter for the field <code>includeScope</code>.</p>
     *
     * @return Returns the includeScope.
     */
    public String getIncludeScope() {
        return this.includeScope;
    }

    /**
     * <p>Setter for the field <code>includeScope</code>.</p>
     *
     * @param scope
     *            The includeScope to set.
     */
    public void setIncludeScope(String scope) {
        this.includeScope = scope;
    }

    /**
     * <p>Getter for the field <code>excludeScope</code>.</p>
     *
     * @return Returns the excludeScope.
     */
    public String getExcludeScope() {
        return this.excludeScope;
    }

    /**
     * <p>Setter for the field <code>excludeScope</code>.</p>
     *
     * @param scope
     *            The excludeScope to set.
     */
    public void setExcludeScope(String scope) {
        this.excludeScope = scope;
    }
}
