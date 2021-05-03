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
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.testing.ArtifactStubFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:brianf@apache.org">Brian Fox</a>
 */
public class TestScopeFilter
{
    Set<Artifact> artifacts;

    @Before
    public void setUp()
        throws Exception
    {
        ArtifactStubFactory factory = new ArtifactStubFactory( null, false );
        artifacts = factory.getScopedArtifacts();
    }

    @Test
    public void testScopeCompile()
        throws ArtifactFilterException
    {
        ScopeFilter filter = new ScopeFilter( Artifact.SCOPE_COMPILE, null );
        Set<Artifact> result = filter.filter( artifacts );
        assertEquals( 3, result.size() );

    }

    @Test
    public void testScopeRuntime()
        throws ArtifactFilterException
    {
        ScopeFilter filter = new ScopeFilter( Artifact.SCOPE_RUNTIME, null );
        Set<Artifact> result = filter.filter( artifacts );
        assertEquals( 2, result.size() );
    }

    @Test
    public void testScopeTest()
        throws ArtifactFilterException
    {
        ScopeFilter filter = new ScopeFilter( Artifact.SCOPE_TEST, null );
        Set<Artifact> result = filter.filter( artifacts );
        assertEquals( 5, result.size() );
    }

    @Test
    public void testScopeProvided()
        throws ArtifactFilterException
    {
        ScopeFilter filter = new ScopeFilter( Artifact.SCOPE_PROVIDED, null );
        Set<Artifact> result = filter.filter( artifacts );
        assertTrue( result.size() > 0 );
        for ( Artifact artifact : result )
        {
            assertEquals( Artifact.SCOPE_PROVIDED, artifact.getScope() );
        }
    }

    @Test
    public void testScopeSystem()
        throws ArtifactFilterException
    {
        ScopeFilter filter = new ScopeFilter( Artifact.SCOPE_SYSTEM, null );
        Set<Artifact> result = filter.filter( artifacts );
        assertTrue( result.size() > 0 );
        for ( Artifact artifact : result )
        {
            assertEquals( Artifact.SCOPE_SYSTEM, artifact.getScope() );
        }
    }

    @Test
    public void testScopeFilterNull()
        throws ArtifactFilterException
    {
        ScopeFilter filter = new ScopeFilter( null, null );
        Set<Artifact> result = filter.filter( artifacts );
        assertEquals( 5, result.size() );
    }

    @Test
    public void testScopeFilterEmpty()
        throws ArtifactFilterException
    {
        ScopeFilter filter = new ScopeFilter( "", "" );
        Set<Artifact> result = filter.filter( artifacts );
        assertEquals( 5, result.size() );
    }

    @Test
    public void testExcludeProvided()
        throws ArtifactFilterException
    {
        ScopeFilter filter = new ScopeFilter( "", Artifact.SCOPE_PROVIDED );
        Set<Artifact> result = filter.filter( artifacts );
        assertNotNull( result );
        assertTrue( result.size() > 0 );
        for ( Artifact artifact : result )
        {
            assertFalse( Artifact.SCOPE_PROVIDED.equalsIgnoreCase( artifact.getScope() ) );
        }
    }

    @Test
    public void testExcludeSystem()
        throws ArtifactFilterException
    {
        ScopeFilter filter = new ScopeFilter( "", Artifact.SCOPE_SYSTEM );
        Set<Artifact> result = filter.filter( artifacts );
        assertNotNull( result );
        assertTrue( result.size() > 0 );
        for ( Artifact artifact : result )
        {
            assertFalse( Artifact.SCOPE_SYSTEM.equalsIgnoreCase( artifact.getScope() ) );
        }
    }

    @Test
    public void testExcludeCompile()
        throws ArtifactFilterException
    {
        ScopeFilter filter = new ScopeFilter( "", Artifact.SCOPE_COMPILE );
        Set<Artifact> result = filter.filter( artifacts );
        assertEquals( 2, result.size() );
    }

    @Test
    public void testExcludeTest()
    {
        try
        {
            ScopeFilter filter = new ScopeFilter( "", Artifact.SCOPE_TEST );
            filter.filter( artifacts );
            Assert.fail( "Expected an Exception" );
        }
        catch ( ArtifactFilterException ignored )
        {
        }
    }

    @Test
    public void testBadScope()
    {
        ScopeFilter filter = new ScopeFilter( "cOmpile", "" );
        try
        {
            filter.filter( artifacts );
            Assert.fail( "Expected an Exception" );
        }
        catch ( ArtifactFilterException ignored )
        {

        }
        try
        {
            filter = new ScopeFilter( "", "coMpile" );
            filter.filter( artifacts );
            Assert.fail( "Expected an Exception" );
        }
        catch ( ArtifactFilterException ignored )
        {

        }
    }

    @Test
    public void testSettersGetters()
    {
        ScopeFilter filter = new ScopeFilter( "include", "exclude" );
        assertEquals( "include", filter.getIncludeScope() );
        assertEquals( "exclude", filter.getExcludeScope() );

        filter.setExcludeScope( "a" );
        filter.setIncludeScope( "b" );
        assertEquals( "b", filter.getIncludeScope() );
        assertEquals( "a", filter.getExcludeScope() );
    }
}
