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

import junit.framework.AssertionFailedError;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.versioning.VersionRange;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests subclasses of <code>AbstractStrictPatternArtifactFilter</code>.
 *
 * @author <a href="mailto:markhobson@gmail.com">Mark Hobson</a>
 * @see AbstractStrictPatternArtifactFilter
 */
public abstract class AbstractStrictPatternArtifactFilterTest {
    protected Artifact artifact;

    @Before
    public void setUp() {
        artifact = createArtifact("groupId", "artifactId", "type", "version");
    }

    @Test
    public void testExactIncluded() {
        assertIncluded("groupId:artifactId");
    }

    @Test
    public void testExactExcluded() {
        assertExcluded("differentGroupId:differentArtifactId");
    }

    @Test
    public void testGroupIdIncluded() {
        assertIncluded("groupId");
    }

    @Test
    public void testGroupIdExcluded() {
        assertExcluded("differentGroupId");
    }

    @Test
    public void testGroupIdWildcardIncluded() {
        assertIncluded("*");
    }

    @Test
    public void testGroupIdImplicitWildcardIncluded() {
        assertIncluded("");
    }

    @Test
    public void testGroupIdStartsWithWildcardIncluded() {
        assertIncluded("groupId*");
    }

    @Test
    public void testGroupIdStartsWithPartialWildcardIncluded() {
        assertIncluded("group*");
    }

    @Test
    public void testGroupIdStartsWithWildcardExcluded() {
        assertExcluded("different*");
    }

    @Test
    public void testGroupIdEndsWithWildcardIncluded() {
        assertIncluded("*groupId");
    }

    @Test
    public void testGroupIdEndsWithPartialWildcardIncluded() {
        assertIncluded("*Id");
    }

    @Test
    public void testGroupIdEndsWithWildcardExcluded() {
        assertExcluded("*different");
    }

    @Test
    public void testGroupIdContainsWildcardIncluded() {
        assertIncluded("*oup*");
    }

    @Test
    public void testGroupIdContainsWildcardExcluded() {
        assertExcluded("*different*");
    }

    @Test
    public void testArtifactIdIncluded() {
        assertIncluded(":artifactId");
    }

    @Test
    public void testArtifactIdExcluded() {
        assertExcluded(":differentArtifactId");
    }

    @Test
    public void testArtifactIdWildcardIncluded() {
        assertIncluded(":*");
    }

    @Test
    public void testArtifactIdImplicitWildcardIncluded() {
        assertIncluded(":");
    }

    @Test
    public void testArtifactIdStartsWithWildcardIncluded() {
        assertIncluded(":artifactId*");
    }

    @Test
    public void testArtifactIdStartsWithPartialWildcardIncluded() {
        assertIncluded(":artifact*");
    }

    @Test
    public void testArtifactIdStartsWithWildcardExcluded() {
        assertExcluded(":different*");
    }

    @Test
    public void testArtifactIdEndsWithWildcardIncluded() {
        assertIncluded(":*artifactId");
    }

    @Test
    public void testArtifactIdEndsWithPartialWildcardIncluded() {
        assertIncluded(":*Id");
    }

    @Test
    public void testArtifactIdEndsWithWildcardExcluded() {
        assertExcluded(":*different");
    }

    @Test
    public void testArtifactIdContainsWildcardIncluded() {
        assertIncluded(":*fact*");
    }

    @Test
    public void testArtifactIdContainsWildcardExcluded() {
        assertExcluded(":*different*");
    }

    @Test
    public void testTypeIncluded() {
        assertIncluded("::type");
    }

    @Test
    public void testTypeExcluded() {
        assertExcluded("::differentType");
    }

    @Test
    public void testTypeWildcardIncluded() {
        assertIncluded("::*");
    }

    @Test
    public void testTypeImplicitWildcardIncluded() {
        assertIncluded("::");
    }

    @Test
    public void testTypeStartsWithWildcardIncluded() {
        assertIncluded("::type*");
    }

    @Test
    public void testTypeStartsWithPartialWildcardIncluded() {
        assertIncluded("::t*");
    }

    @Test
    public void testTypeStartsWithWildcardExcluded() {
        assertExcluded("::different*");
    }

    @Test
    public void testTypeEndsWithWildcardIncluded() {
        assertIncluded("::*type");
    }

    @Test
    public void testTypeEndsWithPartialWildcardIncluded() {
        assertIncluded("::*e");
    }

    @Test
    public void testTypeEndsWithWildcardExcluded() {
        assertExcluded("::*different");
    }

    @Test
    public void testTypeContainsWildcardIncluded() {
        assertIncluded("::*yp*");
    }

    @Test
    public void testTypeContainsWildcardExcluded() {
        assertExcluded("::*different*");
    }

    @Test
    public void testVersionIncluded() {
        assertIncluded(":::version");
    }

    @Test
    public void testVersionExcluded() {
        assertExcluded(":::differentVersion");
    }

    @Test
    public void testVersionWildcardIncluded() {
        assertIncluded(":::*");
    }

    @Test
    public void testVersionImplicitWildcardIncluded() {
        assertIncluded(":::");
    }

    @Test
    public void testVersionStartsWithWildcardIncluded() {
        assertIncluded(":::version*");
    }

    @Test
    public void testVersionStartsWithPartialWildcardIncluded() {
        assertIncluded(":::ver*");
    }

    @Test
    public void testVersionStartsWithWildcardExcluded() {
        assertExcluded(":::different*");
    }

    @Test
    public void testVersionEndsWithWildcardIncluded() {
        assertIncluded(":::*version");
    }

    @Test
    public void testVersionEndsWithPartialWildcardIncluded() {
        assertIncluded(":::*ion");
    }

    @Test
    public void testVersionEndsWithWildcardExcluded() {
        assertExcluded(":::*different");
    }

    @Test
    public void testVersionContainsWildcardIncluded() {
        assertIncluded(":::*si*");
    }

    @Test
    public void testVersionContainsWildcardExcluded() {
        assertExcluded(":::*different*");
    }

    @Test
    public void testComplex() {
        assertIncluded("group*:*Id:*:version");
    }

    @Test
    public void testSnapshotVersion() {
        artifact = createArtifact("groupId", "artifactId", "type", "version-12345678.123456-1");

        assertIncluded(":::*-SNAPSHOT");
    }

    @Test
    public void testRangeVersion() {
        artifact = createArtifact("groupId", "artifactId", "type", "1.0.1");
        assertIncluded("groupId:artifactId:type:[1.0.1]");
        assertIncluded("groupId:artifactId:type:[1.0,1.1)");

        assertExcluded("groupId:artifactId:type:[1.5,)");
        assertExcluded("groupId:artifactId:type:(,1.0],[1.2,)");
        assertExcluded("groupId:artifactId:type:(,1.0],[1.2,)");
    }

    @Test
    public void testWildcardsWithRangeVersion() {
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
