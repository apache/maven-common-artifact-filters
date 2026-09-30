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
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.maven.api.Dependency;
import org.apache.maven.api.DependencyScope;
import org.apache.maven.shared.artifact.filter.PatternExcludesArtifactFilter;
import org.apache.maven.shared.artifact.filter.PatternIncludesArtifactFilter;
import org.apache.maven.shared.artifact.filter.resolve.AbstractFilter;
import org.apache.maven.shared.artifact.filter.resolve.AndFilter;
import org.apache.maven.shared.artifact.filter.resolve.ExclusionsFilter;
import org.apache.maven.shared.artifact.filter.resolve.FilterTransformer;
import org.apache.maven.shared.artifact.filter.resolve.OrFilter;
import org.apache.maven.shared.artifact.filter.resolve.PatternExclusionsFilter;
import org.apache.maven.shared.artifact.filter.resolve.PatternInclusionsFilter;
import org.apache.maven.shared.artifact.filter.resolve.ScopeFilter;
import org.apache.maven.shared.artifact.filter.resolve.TransformableFilter;

/**
 * Makes it possible to use the TransformableFilters for Aether and as classic Maven ArtifactFilter.
 *
 * <strong>Note:</strong> the {@link AndFilter} and {@link ExclusionsFilter} are transformed to {@link ArtifactFilter}
 * implementations of Maven Core
 *
 * @author Robert Scholte
 * @since 3.0
 */
public class ArtifactIncludeFilterTransformer implements FilterTransformer<Predicate<Dependency>> {

    private boolean includeNullScope = true;

    /**
     * Used by {@link #transform(ScopeFilter)}
     *
     * When filtering on artifacts it is possible that the scope is unknown.
     * Decide if artifact should be included if its scope is {@code null}, default is {@code true}
     *
     * @param includeNullScope set to {@code false} if {@code null}-scoped Artifacts should not be included
     */
    public void setIncludeNullScope(boolean includeNullScope) {
        this.includeNullScope = includeNullScope;
    }

    /** {@inheritDoc} */
    @Override
    public Predicate<Dependency> transform(final ScopeFilter scopeFilter) {
        return artifact -> {
            DependencyScope depScope = artifact.getScope();
            if (depScope == null || depScope == DependencyScope.NONE || depScope == DependencyScope.UNDEFINED) {
                return includeNullScope;
            }

            boolean isIncluded;

            if (scopeFilter.getIncluded() != null) {
                isIncluded = scopeFilter.getIncluded().contains(depScope.id());
            } else {
                isIncluded = true;
            }

            boolean isExcluded;

            if (scopeFilter.getExcluded() != null) {
                isExcluded = scopeFilter.getExcluded().contains(depScope.id());
            } else {
                isExcluded = false;
            }

            return isIncluded && !isExcluded;
        };
    }

    /** {@inheritDoc} */
    @Override
    public Predicate<Dependency> transform(AndFilter andFilter) {
        Predicate<Dependency> filter = artifact -> true;

        for (TransformableFilter subFilter : andFilter.getFilters()) {
            filter = filter.and(subFilter.transform(this));
        }

        return filter;
    }

    /** {@inheritDoc} */
    @Override
    public Predicate<Dependency> transform(final ExclusionsFilter exclusionsFilter) {
        final Set<String> excludes = new HashSet<>(exclusionsFilter.getExcludes());
        return artifact -> !excludes.contains(artifact.getGroupId() + ':' + artifact.getArtifactId());
    }

    /** {@inheritDoc} */
    @Override
    public Predicate<Dependency> transform(OrFilter orFilter) {
        final Collection<Predicate<Dependency>> filters =
                new ArrayList<>(orFilter.getFilters().size());

        for (TransformableFilter subFilter : orFilter.getFilters()) {
            filters.add(subFilter.transform(this));
        }

        return artifact -> {
            for (Predicate<Dependency> filter : filters) {
                if (filter.test(artifact)) {
                    return true;
                }
            }
            return false;
        };
    }

    /** {@inheritDoc} */
    @Override
    public Predicate<Dependency> transform(PatternExclusionsFilter patternExclusionsFilter) {
        return new PatternExcludesArtifactFilter(patternExclusionsFilter.getExcludes());
    }

    /** {@inheritDoc} */
    @Override
    public Predicate<Dependency> transform(PatternInclusionsFilter patternInclusionsFilter) {
        return new PatternIncludesArtifactFilter(patternInclusionsFilter.getIncludes());
    }

    /** {@inheritDoc} */
    @Override
    public Predicate<Dependency> transform(final AbstractFilter filter) {
        return artifact -> filter.accept(new ArtifactIncludeNode(artifact), null);
    }
}
