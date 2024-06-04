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

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.maven.artifact.Artifact;

import static org.apache.maven.shared.artifact.filter.internal.Utils.isNotEmpty;

/**
 * This is the common base class of ClassifierFilter and TypeFilter
 *
 * @author <a href="richardv@mxtelecom.com">Richard van der Hoff</a>
 */
public abstract class AbstractArtifactFeatureFilter extends AbstractArtifactsFilter {
    /** The list of types or classifiers to include */
    private List<String> includes;

    /**
     * The list of types or classifiers to exclude (ignored if includes != null)
     */
    private List<String> excludes;

    /**
     * <p>Constructor for AbstractArtifactFeatureFilter.</p>
     *
     * @param include comma separated list with includes.
     * @param exclude comma separated list with excludes.
     */
    public AbstractArtifactFeatureFilter(String include, String exclude) {
        setExcludes(exclude);
        setIncludes(include);
    }

    /**
     * {@inheritDoc}
     *
     * This function determines if filtering needs to be performed. Includes are processed before Excludes.
     */
    public Set<Artifact> filter(Set<Artifact> artifacts) {
        Set<Artifact> results = artifacts;

        if (this.includes != null && !this.includes.isEmpty()) {
            results = filterIncludes(results, this.includes);
        }

        if (this.excludes != null && !this.excludes.isEmpty()) {
            results = filterExcludes(results, this.excludes);
        }

        return results;
    }

    /**
     * Processes the dependencies list and includes the dependencies that match a filter in the list.
     *
     * @param artifacts List of dependencies.
     * @param theIncludes List of types or classifiers to include.
     * @return a set of filtered artifacts.
     */
    private Set<Artifact> filterIncludes(Set<Artifact> artifacts, List<String> theIncludes) {
        Set<Artifact> result = new LinkedHashSet<>();

        for (Artifact artifact : artifacts) {
            for (String include : theIncludes) {
                // if the classifier or type of the artifact
                // matches the feature
                // to include, add to the
                // results
                if (compareFeatures(getArtifactFeature(artifact), include)) {
                    result.add(artifact);
                }
            }
        }
        return result;
    }

    /**
     * Processes the dependencies list and excludes the dependencies that match a filter in the list.
     *
     * @param artifacts List of dependencies.
     * @param theExcludes List of types or classifiers to exclude.
     * @return a set of filtered artifacts.
     */
    private Set<Artifact> filterExcludes(Set<Artifact> artifacts, List<String> theExcludes) {
        Set<Artifact> result = new LinkedHashSet<>();

        for (Artifact artifact : artifacts) {
            boolean exclude = false;
            String artifactFeature = getArtifactFeature(artifact);

            // look through all types or classifiers. If no
            // matches are found
            // then it can be added to the results.
            for (String excludeFeature : theExcludes) {
                if (compareFeatures(artifactFeature, excludeFeature)) {
                    exclude = true;
                    break;
                }
            }

            if (!exclude) {
                result.add(artifact);
            }
        }

        return result;
    }

    /**
     * Should return the type or classifier of the given artifact, so that we can filter it
     *
     * @param artifact artifact to return type or classifier of
     * @return type or classifier
     */
    protected abstract String getArtifactFeature(Artifact artifact);

    /**
     * <p>Setter for the field <code>excludes</code>.</p>
     *
     * @param excludeString comma separated list with excludes.
     */
    public void setExcludes(String excludeString) {
        if (isNotEmpty(excludeString)) {
            this.excludes = Arrays.asList(excludeString.split(","));
        }
    }

    /**
     * <p>Setter for the field <code>includes</code>.</p>
     *
     * @param includeString comma separated list with includes.
     */
    public void setIncludes(String includeString) {
        if (isNotEmpty(includeString)) {
            this.includes = Arrays.asList(includeString.split(","));
        }
    }

    /**
     * <p>Getter for the field <code>excludes</code>.</p>
     *
     * @return Returns the excludes.
     */
    public List<String> getExcludes() {
        return this.excludes;
    }

    /**
     * <p>Getter for the field <code>includes</code>.</p>
     *
     * @return Returns the includes.
     */
    public List<String> getIncludes() {
        return this.includes;
    }

    /**
     * Allows Feature comparison to be customized
     *
     * @param lhs String artifact's feature
     * @param rhs String feature from exclude or include list
     * @return boolean true if features match
     */
    protected boolean compareFeatures(String lhs, String rhs) {
        return (Objects.equals(lhs, rhs));
    }
}
