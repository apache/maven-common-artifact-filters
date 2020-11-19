package org.apache.maven.shared.artifact.filter;

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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.junit.Test;

public abstract class AbstractPatternArtifactFilterTest
{

    protected abstract ArtifactFilter createFilter( List<String> patterns );

    protected abstract ArtifactFilter createFilter( List<String> patterns, boolean actTransitively );

    protected abstract boolean isInclusionNotExpected();

    @Test
    public void testShouldTriggerBothPatternsWithWildcards()
    {
        final String groupId1 = "group";
        final String artifactId1 = "artifact";

        final String groupId2 = "group2";
        final String artifactId2 = "artifact2";

        Artifact artifact1 = mock( Artifact.class );
        when( artifact1.getGroupId() ).thenReturn( groupId1 );
        when( artifact1.getArtifactId() ).thenReturn( artifactId1 );
        when( artifact1.getType() ).thenReturn( "jar" );
        when( artifact1.getBaseVersion() ).thenReturn( "version" );
        
        Artifact artifact2 = mock( Artifact.class );
        when( artifact2.getGroupId() ).thenReturn( groupId2 );
        when( artifact2.getArtifactId() ).thenReturn( artifactId2 );
        when( artifact2.getType() ).thenReturn( "jar" );
        when( artifact2.getBaseVersion() ).thenReturn( "version" );

        final List<String> patterns = new ArrayList<>();
        patterns.add( groupId1 + ":" + artifactId1 + ":*" );
        patterns.add( groupId2 + ":" + artifactId2 + ":*" );

        final ArtifactFilter filter = createFilter( patterns );

        if ( isInclusionNotExpected() )
        {
            assertFalse( filter.include( artifact1 ) );
            assertFalse( filter.include( artifact2 ) );
        }
        else
        {
            assertTrue( filter.include( artifact1 ) );
            assertTrue( filter.include( artifact2 ) );
        }
    }

    @Test
    public void testShouldTriggerBothPatternsWithNonColonWildcards()
    {
        final String groupId1 = "group";
        final String artifactId1 = "artifact";

        final String groupId2 = "group2";
        final String artifactId2 = "artifact2";

        Artifact artifact1 = mock( Artifact.class );
        when( artifact1.getGroupId() ).thenReturn( groupId1 );
        when( artifact1.getArtifactId() ).thenReturn( artifactId1 );
        when( artifact1.getType() ).thenReturn( "jar" );
        when( artifact1.getBaseVersion() ).thenReturn( "version" );

        Artifact artifact2 = mock( Artifact.class );
        when( artifact2.getGroupId() ).thenReturn( groupId2 );
        when( artifact2.getArtifactId() ).thenReturn( artifactId2 );
        when( artifact2.getType() ).thenReturn( "jar" );
        when( artifact2.getBaseVersion() ).thenReturn( "version" );

        final List<String> patterns = new ArrayList<>();
        patterns.add( groupId1 + "*" );
        patterns.add( groupId2 + "*" );

        final ArtifactFilter filter = createFilter( patterns );

        if ( isInclusionNotExpected() )
        {
            assertFalse( filter.include( artifact1 ) );
            assertFalse( filter.include( artifact2 ) );
        }
        else
        {
            assertTrue( filter.include( artifact1 ) );
            assertTrue( filter.include( artifact2 ) );
        }
    }

    @Test
    public void testShouldIncludeDirectlyMatchedArtifactByGroupIdArtifactId()
    {
        final String groupId = "group";
        final String artifactId = "artifact";

        Artifact artifact = mock( Artifact.class );
        when( artifact.getGroupId() ).thenReturn( groupId );
        when( artifact.getArtifactId() ).thenReturn( artifactId );
        when( artifact.getType() ).thenReturn( "jar" );
        when( artifact.getBaseVersion() ).thenReturn( "version" );

        final ArtifactFilter filter = createFilter( Collections.singletonList( groupId + ":" + artifactId ) );

        if ( isInclusionNotExpected() )
        {
            assertFalse( filter.include( artifact ) );
        }
        else
        {
            assertTrue( filter.include( artifact ) );
        }
    }

    @Test
    public void testShouldIncludeDirectlyMatchedArtifactByDependencyConflictId()
    {
        final String groupId = "group";
        final String artifactId = "artifact";

        Artifact artifact = mock( Artifact.class );
        when( artifact.getGroupId() ).thenReturn( groupId );
        when( artifact.getArtifactId() ).thenReturn( artifactId );
        when( artifact.getType() ).thenReturn( "jar" );
        when( artifact.getBaseVersion() ).thenReturn( "version" );

        final ArtifactFilter filter = createFilter( Collections.singletonList( groupId + ":" + artifactId + ":jar" ) );

        if ( isInclusionNotExpected() )
        {
            assertFalse( filter.include( artifact ) );
        }
        else
        {
            assertTrue( filter.include( artifact ) );
        }
    }

    @Test
    public void testShouldNotIncludeWhenGroupIdDiffers()
    {
        final String groupId = "group";
        final String artifactId = "artifact";

        Artifact artifact = mock( Artifact.class );
        when( artifact.getGroupId() ).thenReturn( groupId );
        when( artifact.getArtifactId() ).thenReturn( artifactId );
        when( artifact.getType() ).thenReturn( "jar" );
        when( artifact.getBaseVersion() ).thenReturn( "version" );

        final List<String> patterns = new ArrayList<>();
        patterns.add( "otherGroup:" + artifactId + ":jar" );
        patterns.add( "otherGroup:" + artifactId );

        final ArtifactFilter filter = createFilter( patterns );

        if ( isInclusionNotExpected() )
        {
            assertTrue( filter.include( artifact ) );
        }
        else
        {
            assertFalse( filter.include( artifact ) );
        }
    }

    @Test
    public void testShouldNotIncludeWhenArtifactIdDiffers()
    {
        final String groupId = "group";
        final String artifactId = "artifact";

        Artifact artifact = mock( Artifact.class );
        when( artifact.getGroupId() ).thenReturn( groupId );
        when( artifact.getArtifactId() ).thenReturn( artifactId );
        when( artifact.getType() ).thenReturn( "jar" );
        when( artifact.getBaseVersion() ).thenReturn( "version" );

        final List<String> patterns = new ArrayList<>();
        patterns.add( groupId + "otherArtifact:jar" );
        patterns.add( groupId + "otherArtifact" );

        final ArtifactFilter filter = createFilter( patterns );

        if ( isInclusionNotExpected() )
        {
            assertTrue( filter.include( artifact ) );
        }
        else
        {
            assertFalse( filter.include( artifact ) );
        }
    }

    @Test
    public void testShouldNotIncludeWhenBothIdElementsDiffer()
    {
        final String groupId = "group";
        final String artifactId = "artifact";

        Artifact artifact = mock( Artifact.class );
        when( artifact.getGroupId() ).thenReturn( groupId );
        when( artifact.getArtifactId() ).thenReturn( artifactId );
        when( artifact.getType() ).thenReturn( "jar" );
        when( artifact.getBaseVersion() ).thenReturn( "version" );

        final List<String> patterns = new ArrayList<>();
        patterns.add( "otherGroup:otherArtifact:jar" );
        patterns.add( "otherGroup:otherArtifact" );

        final ArtifactFilter filter = createFilter( patterns );

        if ( isInclusionNotExpected() )
        {
            assertTrue( filter.include( artifact ) );
        }
        else
        {
            assertFalse( filter.include( artifact ) );
        }
    }

    @Test
    public void testShouldIncludeWhenPatternMatchesDependencyTrailAndTransitivityIsEnabled()
    {
        final String groupId = "group";
        final String artifactId = "artifact";

        final String rootDepTrailItem = "current:project:jar:1.0";
        final String depTrailItem = "otherGroup:otherArtifact";

        final List<String> depTrail = Arrays.asList( rootDepTrailItem, depTrailItem + ":jar:1.0" );
        final List<String> patterns = Collections.singletonList( depTrailItem );

        Artifact artifact = mock( Artifact.class );
        when( artifact.getGroupId() ).thenReturn( groupId );
        when( artifact.getArtifactId() ).thenReturn( artifactId );
        when( artifact.getType() ).thenReturn( "jar" );
        when( artifact.getBaseVersion() ).thenReturn( "version" );
        when( artifact.getDependencyTrail() ).thenReturn( depTrail );

        final ArtifactFilter filter = createFilter( patterns, true );

        if ( isInclusionNotExpected() )
        {
            assertFalse( filter.include( artifact ) );
        }
        else
        {
            assertTrue( filter.include( artifact ) );
        }
    }

    @Test
    public void testIncludeWhenPatternMatchesDepTrailWithTransitivityUsingNonColonWildcard()
    {
        final String groupId = "group";
        final String artifactId = "artifact";

        final String rootDepTrailItem = "current:project:jar:1.0";
        final String depTrailItem = "otherGroup:otherArtifact";

        final List<String> depTrail = Arrays.asList( rootDepTrailItem, depTrailItem + ":jar:1.0" );
        final List<String> patterns = Collections.singletonList( "otherGroup*" );

        Artifact artifact = mock( Artifact.class );
        when( artifact.getGroupId() ).thenReturn( groupId );
        when( artifact.getArtifactId() ).thenReturn( artifactId );
        when( artifact.getType() ).thenReturn( "jar" );
        when( artifact.getBaseVersion() ).thenReturn( "version" );
        when( artifact.getDependencyTrail() ).thenReturn( depTrail );

        final ArtifactFilter filter = createFilter( patterns, true );

        if ( isInclusionNotExpected() )
        {
            assertFalse( filter.include( artifact ) );
        }
        else
        {
            assertTrue( filter.include( artifact ) );
        }
    }

    @Test
    public void testShouldNotIncludeWhenNegativeMatch()
    {
        final String groupId = "group";
        final String artifactId = "artifact";

        Artifact artifact = mock( Artifact.class );
        when( artifact.getGroupId() ).thenReturn( groupId );
        when( artifact.getArtifactId() ).thenReturn( artifactId );
        when( artifact.getType() ).thenReturn( "jar" );
        when( artifact.getBaseVersion() ).thenReturn( "version" );

        final List<String> patterns = new ArrayList<>();
        patterns.add( "!group:artifact:jar" );

        final ArtifactFilter filter = createFilter( patterns );

        if ( isInclusionNotExpected() )
        {
            assertTrue( filter.include( artifact ) );
        }
        else
        {
            assertFalse( filter.include( artifact ) );
        }
    }

    @Test
    public void testShouldIncludeWhenWildcardMatchesInsideSequence()
    {
        final String groupId = "group";
        final String artifactId = "artifact";

        Artifact artifact = mock( Artifact.class );
        when( artifact.getGroupId() ).thenReturn( groupId );
        when( artifact.getArtifactId() ).thenReturn( artifactId );
        when( artifact.getType() ).thenReturn( "jar" );
        when( artifact.getBaseVersion() ).thenReturn( "version" );

        final List<String> patterns = new ArrayList<>();
        patterns.add( "group:*:jar" );

        final ArtifactFilter filter = createFilter( patterns );

        if ( isInclusionNotExpected() )
        {
            assertFalse( filter.include( artifact ) );
        }
        else
        {
            assertTrue( filter.include( artifact ) );
        }
    }

    @Test
    public void testShouldIncludeWhenWildcardMatchesOutsideSequence()
    {
        final String groupId = "group";
        final String artifactId = "artifact";

        Artifact artifact = mock( Artifact.class );

        when( artifact.getGroupId() ).thenReturn( groupId );
        when( artifact.getArtifactId() ).thenReturn( artifactId );
        when( artifact.getType() ).thenReturn( "jar" );
        when( artifact.getBaseVersion() ).thenReturn( "version" );

        final List<String> patterns = new ArrayList<>();
        patterns.add( "*:artifact:*" );

        final ArtifactFilter filter = createFilter( patterns );

        if ( isInclusionNotExpected() )
        {
            assertFalse( filter.include( artifact ) );
        }
        else
        {
            assertTrue( filter.include( artifact ) );
        }
    }

    @Test
    public void testShouldIncludeWhenWildcardMatchesMiddleOfArtifactId()
    {
        final String groupId = "group";
        final String artifactId = "some-artifact-id";

        Artifact artifact = mock( Artifact.class );

        when( artifact.getGroupId() ).thenReturn( groupId );
        when( artifact.getArtifactId() ).thenReturn( artifactId );
        when( artifact.getType() ).thenReturn( "jar" );
        when( artifact.getBaseVersion() ).thenReturn( "version" );

        final List<String> patterns = new ArrayList<>();
        patterns.add( "group:some-*-id" );

        final ArtifactFilter filter = createFilter( patterns );

        if ( isInclusionNotExpected() )
        {
            assertFalse( filter.include( artifact ) );
        }
        else
        {
            assertTrue( filter.include( artifact ) );
        }
    }

    @Test
    public void testShouldIncludeWhenWildcardCoversPartOfGroupIdAndEverythingElse()
    {
        final String groupId = "some.group.id";
        final String artifactId = "some-artifact-id";

        Artifact artifact = mock( Artifact.class );

        when( artifact.getGroupId() ).thenReturn( groupId );
        when( artifact.getArtifactId() ).thenReturn( artifactId );
        when( artifact.getType() ).thenReturn( "jar" );
        when( artifact.getBaseVersion() ).thenReturn( "version" );

        final List<String> patterns = new ArrayList<>();
        patterns.add( "some.group*" );

        final ArtifactFilter filter = createFilter( patterns );

        if ( isInclusionNotExpected() )
        {
            assertFalse( filter.include( artifact ) );
        }
        else
        {
            assertTrue( filter.include( artifact ) );
        }
    }

    @Test
    public void testShouldIncludeTransitiveDependencyWhenWildcardMatchesButDoesntMatchParent()
    {
        final String groupId = "group";
        final String artifactId = "artifact";

        final String otherGroup = "otherGroup";
        final String otherArtifact = "otherArtifact";
        final String otherType = "ejb";

        final List<String> patterns = Collections.singletonList( "*:jar:*" );

        Artifact artifact1 = mock( Artifact.class );
        when( artifact1.getGroupId() ).thenReturn( groupId );
        when( artifact1.getArtifactId() ).thenReturn( artifactId );
        when( artifact1.getType() ).thenReturn( "jar" );
        when( artifact1.getBaseVersion() ).thenReturn( "version" );

        Artifact artifact2 = mock( Artifact.class );
        when( artifact2.getGroupId() ).thenReturn( otherGroup );
        when( artifact2.getArtifactId() ).thenReturn( otherArtifact );
        when( artifact2.getType() ).thenReturn( otherType );
        when( artifact2.getBaseVersion() ).thenReturn( "version" );
        when( artifact2.getDependencyTrail() ).thenReturn( Collections.<String> emptyList() );

        final ArtifactFilter filter = createFilter( patterns, true );

        if ( isInclusionNotExpected() )
        {
            assertTrue( filter.include( artifact2 ) );
            assertFalse( filter.include( artifact1 ) );
        }
        else
        {
            assertFalse( filter.include( artifact2 ) );
            assertTrue( filter.include( artifact1 ) );
        }
    }
    
    @Test
    public void testShouldIncludeJarsWithAndWithoutClassifier()
    {
        final String groupId = "com.mycompany.myproject";
        final String artifactId = "some-artifact-id";

        Artifact artifact = mock( Artifact.class );
        when( artifact.getGroupId() ).thenReturn( groupId );
        when( artifact.getArtifactId() ).thenReturn( artifactId );
        when( artifact.getType() ).thenReturn( "jar" );
        when( artifact.getBaseVersion() ).thenReturn( "version" );

        final List<String> patterns = new ArrayList<>();
        patterns.add( "com.mycompany.*:*:jar:*:*" );

        final ArtifactFilter filter = createFilter( patterns );

        if ( isInclusionNotExpected() )
        {
            assertFalse( filter.include( artifact ) );
        }
        else
        {
            assertTrue( filter.include( artifact ) );
        }
    }

    @Test
    public void testWithVersionRange()
    {
        final String groupId = "com.mycompany.myproject";
        final String artifactId = "some-artifact-id";

        Artifact artifact = mock( Artifact.class );
        when( artifact.getGroupId() ).thenReturn( groupId );
        when( artifact.getArtifactId() ).thenReturn( artifactId );
        when( artifact.getType() ).thenReturn( "jar" );
        when( artifact.getBaseVersion() ).thenReturn( "1.1" );

        final List<String> patterns = new ArrayList<>();
        patterns.add( "com.mycompany.myproject:some-artifact-id:jar:*:[1.0,2.0)" );

        final ArtifactFilter filter = createFilter( patterns );

        if ( isInclusionNotExpected() )
        {
            assertFalse( filter.include( artifact ) );
        }
        else
        {
            assertTrue( filter.include( artifact ) );
        }
    }
}
