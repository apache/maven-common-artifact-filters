package org.apache.maven.shared.artifact.filter.collection;

/* 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.testing.ArtifactStubFactory;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:brianf@apache.org">Brian Fox</a>
 */
public class TestTypeFilter
{
    Set<Artifact> artifacts;

    @Before
    public void setUp()
        throws Exception
    {
        ArtifactStubFactory factory = new ArtifactStubFactory( null, false );
        artifacts = factory.getTypedArtifacts();
    }

    @Test
    public void testTypeParsing()
    {
        TypeFilter filter = new TypeFilter( "war,jar", "sources,zip," );
        List<String> includes = filter.getIncludes();
        List<String> excludes = filter.getExcludes();

        assertEquals( 2, includes.size() );
        assertEquals( 2, excludes.size() );
        assertEquals( "war", includes.get( 0 ) );
        assertEquals( "jar", includes.get( 1 ) );
        assertEquals( "sources", excludes.get( 0 ) );
        assertEquals( "zip", excludes.get( 1 ) );
    }

    @Test
    public void testFiltering()
    {
        TypeFilter filter = new TypeFilter( "war,jar", "war,zip," );
        Set<Artifact> result = filter.filter( artifacts );
        assertEquals( 1, result.size() );

        for ( Artifact artifact : result )
        {
            assertEquals( "jar", artifact.getType() );
        }
    }

    @Test
    public void testFiltering2()
    {
        TypeFilter filter = new TypeFilter( null, "war,jar," );
        Set<Artifact> result = filter.filter( artifacts );
        assertEquals( 3, result.size() );

        for ( Artifact artifact : result )
        {
            assertTrue( !artifact.getType().equals( "war" ) && !artifact.getType().equals( "jar" ) );
        }
    }

    @Test
    public void testFiltering3()
    {
        TypeFilter filter = new TypeFilter( null, null );
        Set<Artifact> result = filter.filter( artifacts );
        assertEquals( 5, result.size() );
    }

    @Test
    public void testFilteringOrder()
            throws IOException
    {
        TypeFilter filter = new TypeFilter( "war,jar", "zip" );
        Set<Artifact> artifacts = new LinkedHashSet<>();

        ArtifactStubFactory factory = new ArtifactStubFactory( null, false );
        artifacts.add( factory.createArtifact( "g", "a", "1.0", Artifact.SCOPE_COMPILE, "jar", null ) );
        artifacts.add( factory.createArtifact( "g", "b", "1.0", Artifact.SCOPE_COMPILE, "zip", null ) );
        artifacts.add( factory.createArtifact( "g", "c", "1.0", Artifact.SCOPE_COMPILE, "war", null ) );

        Set<Artifact> result = filter.filter( artifacts );

        assertEquals( 2, result.size() );

        List<Artifact> resultList = new ArrayList<>( result );

        assertEquals( "a", resultList.get(0).getArtifactId() );
        assertEquals( "c", resultList.get(1).getArtifactId() );
    }
}
