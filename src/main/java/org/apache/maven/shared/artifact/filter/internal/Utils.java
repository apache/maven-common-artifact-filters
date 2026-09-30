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
package org.apache.maven.shared.artifact.filter.internal;

import org.apache.maven.api.Dependency;

/**
 * Utilities.
 *
 * @since TBD
 */
public final class Utils {
    private static final String SEP = ":";

    private Utils() {}

    /**
     * <p>Checks if a String is non <code>null</code> and is
     * not empty (<code>length &gt; 0</code>).</p>
     *
     * @param str the String to check
     * @return true if the String is non-null, and not length zero
     */
    public static boolean isNotEmpty(String str) {
        return str != null && !str.isEmpty();
    }

    /**
     * Returns the classifier of the dependency, or {@code null} when it has none. The Maven 4 API reports a missing
     * classifier as an empty string, where the Maven 3 API used {@code null}.
     *
     * @param dependency the dependency
     * @return the classifier or {@code null}
     */
    public static String getClassifier(Dependency dependency) {
        String classifier = dependency.getClassifier();
        return classifier == null || classifier.isEmpty() ? null : classifier;
    }

    /**
     * Returns the identifier {@code groupId:artifactId:type[:classifier]:baseVersion} of the dependency.
     *
     * @param dependency the dependency
     * @return the identifier
     */
    public static String getId(Dependency dependency) {
        StringBuilder sb = new StringBuilder();
        sb.append(dependency.getGroupId()).append(SEP).append(dependency.getArtifactId());
        sb.append(SEP).append(dependency.getType().id());
        String classifier = getClassifier(dependency);
        if (classifier != null) {
            sb.append(SEP).append(classifier);
        }
        return sb.append(SEP).append(dependency.getBaseVersion()).toString();
    }

    /**
     * Returns the conflict identifier {@code groupId:artifactId:type[:classifier]} of the dependency.
     *
     * @param dependency the dependency
     * @return the conflict identifier
     */
    public static String getConflictId(Dependency dependency) {
        StringBuilder sb = new StringBuilder();
        sb.append(dependency.getGroupId()).append(SEP).append(dependency.getArtifactId());
        sb.append(SEP).append(dependency.getType().id());
        String classifier = getClassifier(dependency);
        if (classifier != null) {
            sb.append(SEP).append(classifier);
        }
        return sb.toString();
    }
}
