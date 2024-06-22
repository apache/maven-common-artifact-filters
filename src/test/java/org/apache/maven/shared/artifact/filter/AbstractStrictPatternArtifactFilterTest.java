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

import java.util.Collections;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.versioning.VersionRange;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests subclasses of <code>AbstractStrictPatternArtifactFilter</code>.
 *
 * @author <a href="mailto:markhobson@gmail.com">Mark Hobson</a>
 * @see AbstractStrictPatternArtifactFilter
 */
public abstract class AbstractStrictPatternArtifactFilterTest {
    protected Artifact artifact;

    @BeforeEach
    public void setUp() {
        artifact = createArtifact("groupId", "artifactId", "type", "version");
    }

    @Test
    public void checkExactIncluded() {
        assertIncluded("groupId:artifactId");
    }

    @Test
    public void checkExactExcluded() {
        assertExcluded("differentGroupId:differentArtifactId");
    }

    @Test
    public void checkGroupIdIncluded() {
        assertIncluded("groupId");
    }

    @Test
    public void checkgroupIdExcluded() {
        assertExcluded("differentGroupId");
    }

    @Test
    public void checkGroupIdWildcardIncluded() {
        assertIncluded("*");
    }

    @Test
    public void checkGroupIdImplicitWildcardIncluded() {
        assertIncluded("");
    }

    @Test
    public void checkGroupIdStartsWithWildcardIncluded() {
        assertIncluded("groupId*");
    }

    @Test
    public void checkGroupIdStartsWithPartialWildcardIncluded() {
        assertIncluded("group*");
    }

    @Test
    public void checkGroupIdStartsWithWildcardExcluded() {
        assertExcluded("different*");
    }

    @Test
    public void checkGroupIdEndsWithWildcardIncluded() {
        assertIncluded("*groupId");
    }

    @Test
    public void checkGroupIdEndsWithPartialWildcardIncluded() {
        assertIncluded("*Id");
    }

    @Test
    public void checkGroupIdEndsWithWildcardExcluded() {
        assertExcluded("*different");
    }

    @Test
    public void checkGroupIdContainsWildcardIncluded() {
        assertIncluded("*oup*");
    }

    @Test
    public void checkGroupIdContainsWildcardExcluded() {
        assertExcluded("*different*");
    }

    @Test
    public void checkArtifactIdIncluded() {
        assertIncluded(":artifactId");
    }

    @Test
    public void checkArtifactIdExcluded() {
        assertExcluded(":differentArtifactId");
    }

    @Test
    public void checkArtifactIdWildcardIncluded() {
        assertIncluded(":*");
    }

    @Test
    public void checkArtifactIdImplicitWildcardIncluded() {
        assertIncluded(":");
    }

    @Test
    public void checkArtifactIdStartsWithWildcardIncluded() {
        assertIncluded(":artifactId*");
    }

    @Test
    public void checkArtifactIdStartsWithPartialWildcardIncluded() {
        assertIncluded(":artifact*");
    }

    @Test
    public void checkArtifactIdStartsWithWildcardExcluded() {
        assertExcluded(":different*");
    }

    @Test
    public void checkArtifactIdEndsWithWildcardIncluded() {
        assertIncluded(":*artifactId");
    }

    @Test
    public void checkArtifactIdEndsWithPartialWildcardIncluded() {
        assertIncluded(":*Id");
    }

    @Test
    public void checkArtifactIdEndsWithWildcardExcluded() {
        assertExcluded(":*different");
    }

    @Test
    public void checkArtifactIdContainsWildcardIncluded() {
        assertIncluded(":*fact*");
    }

    @Test
    public void checkArtifactIdContainsWildcardExcluded() {
        assertExcluded(":*different*");
    }

    @Test
    public void checkTypeIncluded() {
        assertIncluded("::type");
    }

    @Test
    public void checkTypeExcluded() {
        assertExcluded("::differentType");
    }

    @Test
    public void checkTypeWildcardIncluded() {
        assertIncluded("::*");
    }

    @Test
    public void checkTypeImplicitWildcardIncluded() {
        assertIncluded("::");
    }

    @Test
    public void checkTypeStartsWithWildcardIncluded() {
        assertIncluded("::type*");
    }

    @Test
    public void checkTypeStartsWithPartialWildcardIncluded() {
        assertIncluded("::t*");
    }

    @Test
    public void checkTypeStartsWithWildcardExcluded() {
        assertExcluded("::different*");
    }

    @Test
    public void checkTypeEndsWithWildcardIncluded() {
        assertIncluded("::*type");
    }

    @Test
    public void checkTypeEndsWithPartialWildcardIncluded() {
        assertIncluded("::*e");
    }

    @Test
    public void checkTypeEndsWithWildcardExcluded() {
        assertExcluded("::*different");
    }

    @Test
    public void checkTypeContainsWildcardIncluded() {
        assertIncluded("::*yp*");
    }

    @Test
    public void checkTypeContainsWildcardExcluded() {
        assertExcluded("::*different*");
    }

    @Test
    public void checkVersionIncluded() {
        assertIncluded(":::version");
    }

    @Test
    public void checkVersionExcluded() {
        assertExcluded(":::differentVersion");
    }

    @Test
    public void checkVersionWildcardIncluded() {
        assertIncluded(":::*");
    }

    @Test
    public void checkVersionImplicitWildcardIncluded() {
        assertIncluded(":::");
    }

    @Test
    public void checkVersionStartsWithWildcardIncluded() {
        assertIncluded(":::version*");
    }

    @Test
    public void checkVersionStartsWithPartialWildcardIncluded() {
        assertIncluded(":::ver*");
    }

    @Test
    public void checkVersionStartsWithWildcardExcluded() {
        assertExcluded(":::different*");
    }

    @Test
    public void checkVersionEndsWithWildcardIncluded() {
        assertIncluded(":::*version");
    }

    @Test
    public void checkVersionEndsWithPartialWildcardIncluded() {
        assertIncluded(":::*ion");
    }

    @Test
    public void checkVersionEndsWithWildcardExcluded() {
        assertExcluded(":::*different");
    }

    @Test
    public void checkVersionContainsWildcardIncluded() {
        assertIncluded(":::*si*");
    }

    @Test
    public void checkVersionContainsWildcardExcluded() {
        assertExcluded(":::*different*");
    }

    @Test
    public void checkComplex() {
        assertIncluded("group*:*Id:*:version");
    }

    @Test
    public void checkSnapshotVersion() {
        artifact = createArtifact("groupId", "artifactId", "type", "version-12345678.123456-1");

        assertIncluded(":::*-SNAPSHOT");
    }

    @Test
    public void checkRangeVersion() {
        artifact = createArtifact("groupId", "artifactId", "type", "1.0.1");
        assertIncluded("groupId:artifactId:type:[1.0.1]");
        assertIncluded("groupId:artifactId:type:[1.0,1.1)");

        assertExcluded("groupId:artifactId:type:[1.5,)");
        assertExcluded("groupId:artifactId:type:(,1.0],[1.2,)");
        assertExcluded("groupId:artifactId:type:(,1.0],[1.2,)");
    }

    @Test
    public void checkWildcardsWithRangeVersion() {
        artifact = createArtifact("groupId", "artifactId", "type", "1.0.1");
        assertIncluded(":::[1.0.1]");
        assertIncluded(":artifact*:*:[1.0,1.1)");

        assertExcluded("*group*:*:t*e:[1.5,)");

        artifact = createArtifact("test", "uf", "jar", "0.2.0");
        assertIncluded("test:*:*:[0.0.2,)");
    }

    // protected methods ------------------------------------------------------

    /**
     * Creates an artifact with the specified attributes.
     *
     * @param groupId
     *            the group id for the new artifact
     * @param artifactId
     *            the artifact id for the new artifact
     * @param type
     *            the type for the new artifact
     * @param version
     *            the version for the new artifact
     * @return the artifact
     */
    protected Artifact createArtifact(String groupId, String artifactId, String type, String version) {
        VersionRange versionRange = VersionRange.createFromVersion(version);
        ArtifactHandler handler = new DefaultArtifactHandler();

        return new DefaultArtifact(groupId, artifactId, versionRange, null, type, null, handler);
    }

    /**
     * Asserts that the specified pattern is included by the filter being tested.
     *
     * @param pattern
     *            the pattern to test for inclusion
     * @throws AssertionFailedError
     *             if the assertion fails
     */
    protected void assertIncluded(String pattern) {
        assertFilter(true, pattern);
    }

    /**
     * Asserts that the specified pattern is excluded by the filter being tested.
     *
     * @param pattern
     *            the pattern to test for exclusion
     * @throws AssertionFailedError
     *             if the assertion fails
     */
    protected void assertExcluded(String pattern) {
        assertFilter(false, pattern);
    }

    /**
     * Asserts that the filter being tested returns the specified result for the specified pattern.
     *
     * @param expected
     *            the result expected from the filter
     * @param pattern
     *            the pattern to test
     * @throws AssertionFailedError
     *             if the assertion fails
     */
    protected void assertFilter(boolean expected, String pattern) {
        List<String> patterns = Collections.singletonList(pattern);
        AbstractStrictPatternArtifactFilter filter = createFilter(patterns);

        assertEquals(expected, filter.include(artifact));
    }

    /**
     * Creates the strict pattern artifact filter to test for the specified patterns.
     *
     * @param patterns
     *            the list of artifact patterns that the filter should match
     * @return the filter to test
     */
    protected abstract AbstractStrictPatternArtifactFilter createFilter(List<String> patterns);
}
