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

import java.util.Collection;

import org.apache.maven.artifact.Artifact;

/**
 * TODO: include in maven-artifact in future
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @see StrictPatternExcludesArtifactFilter
 */
public class PatternExcludesArtifactFilter extends PatternIncludesArtifactFilter {
    /**
     * <p>Constructor for PatternExcludesArtifactFilter.</p>
     *
     * @param patterns The pattern to be used.
     */
    public PatternExcludesArtifactFilter(Collection<String> patterns) {
        super(patterns);
    }

    /**
     * <p>Constructor for PatternExcludesArtifactFilter.</p>
     *
     * @param patterns The pattern which will be used.
     * @param actTransitively yes/no.
     */
    public PatternExcludesArtifactFilter(Collection<String> patterns, boolean actTransitively) {
        super(patterns, actTransitively);
    }

    @Override
    public boolean include(Artifact artifact) {
        boolean shouldInclude = !patternMatches(artifact);

        if (!shouldInclude) {
            addFilteredArtifact(artifact);
        }

        return shouldInclude;
    }

    @Override
    protected String getFilterDescription() {
        return "artifact exclusion filter";
    }

    @Override
    public String toString() {
        return "Excludes filter:" + getPatternsAsString();
    }
}
