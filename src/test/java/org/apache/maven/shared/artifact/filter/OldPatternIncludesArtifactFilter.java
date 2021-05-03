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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;
import org.slf4j.Logger;

/**
 * TODO: include in maven-artifact in future
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @see StrictPatternIncludesArtifactFilter
 */
public class OldPatternIncludesArtifactFilter
        implements ArtifactFilter, StatisticsReportingArtifactFilter
{
    private final List<String> positivePatterns;

    private final List<String> negativePatterns;

    private final boolean actTransitively;

    private final Set<String> patternsTriggered = new HashSet<>();

    private final List<String> filteredArtifactIds = new ArrayList<>();

    /**
     * @param patterns The pattern to be used.
     */
    public OldPatternIncludesArtifactFilter( final Collection<String> patterns )
    {
        this( patterns, false );
    }

    /**
     * @param patterns The pattern to be used.
     * @param actTransitively transitive yes/no.
     */
    public OldPatternIncludesArtifactFilter( final Collection<String> patterns, final boolean actTransitively )
    {
        this.actTransitively = actTransitively;
        final List<String> pos = new ArrayList<>();
        final List<String> neg = new ArrayList<>();
        if ( patterns != null && !patterns.isEmpty() )
        {
            for ( String pattern : patterns )
            {
                if ( pattern.startsWith( "!" ) )
                {
                    neg.add( pattern.substring( 1 ) );
                }
                else
                {
                    pos.add( pattern );
                }
            }
        }

        positivePatterns = pos;
        negativePatterns = neg;
    }

    /** {@inheritDoc} */
    public boolean include( final Artifact artifact )
    {
        final boolean shouldInclude = patternMatches( artifact );

        if ( !shouldInclude )
        {
            addFilteredArtifactId( artifact.getId() );
        }

        return shouldInclude;
    }

    /**
     * @param artifact to check for.
     * @return true if the match is true false otherwise.
     */
    protected boolean patternMatches( final Artifact artifact )
    {
        return positiveMatch( artifact ) == Boolean.TRUE || negativeMatch( artifact ) == Boolean.FALSE;
    }

    /**
     * @param artifactId add artifact to the filtered artifacts list.
     */
    protected void addFilteredArtifactId( final String artifactId )
    {
        filteredArtifactIds.add( artifactId );
    }

    private Boolean negativeMatch( final Artifact artifact )
    {
        if ( negativePatterns == null || negativePatterns.isEmpty() )
        {
            return null;
        }
        else
        {
            return match( artifact, negativePatterns );
        }
    }

    /**
     * @param artifact check for positive match.
     * @return true/false.
     */
    protected Boolean positiveMatch( final Artifact artifact )
    {
        if ( positivePatterns == null || positivePatterns.isEmpty() )
        {
            return null;
        }
        else
        {
            return match( artifact, positivePatterns );
        }
    }

    private boolean match( final Artifact artifact, final List<String> patterns )
    {
        final String shortId = ArtifactUtils.versionlessKey( artifact );
        final String id = artifact.getDependencyConflictId();
        final String wholeId = artifact.getId();

        if ( matchAgainst( wholeId, patterns, false ) )
        {
            return true;
        }

        if ( matchAgainst( id, patterns, false ) )
        {
            return true;
        }

        if ( matchAgainst( shortId, patterns, false ) )
        {
            return true;
        }

        if ( actTransitively )
        {
            final List<String> depTrail = artifact.getDependencyTrail();

            if ( depTrail != null && depTrail.size() > 1 )
            {
                for ( String trailItem : depTrail )
                {
                    if ( matchAgainst( trailItem, patterns, true ) )
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private boolean matchAgainst( final String value, final List<String> patterns, final boolean regionMatch )
    {
        final String[] tokens = value.split( ":" );
        for ( String pattern : patterns )
        {
            String[] patternTokens = pattern.split( ":" );

            if ( patternTokens.length == 5 && tokens.length < 5 )
            {
                // 4th element is the classifier
                if ( !"*".equals( patternTokens[3] ) )
                {
                    // classifier required, cannot be a match
                    return false;
                }
                patternTokens = new String[] { patternTokens[0], patternTokens[1], patternTokens[2], patternTokens[4] };
            }

            // fail immediately if pattern tokens outnumber tokens to match
            boolean matched = patternTokens.length <= tokens.length;

            for ( int i = 0; matched && i < patternTokens.length; i++ )
            {
                matched = matches( tokens[i], patternTokens[i] );
            }

            // case of starting '*' like '*:jar:*'
            // This really only matches from the end instead.....
            if ( !matched && patternTokens.length < tokens.length && isFirstPatternWildcard( patternTokens ) )
            {
                matched = true;
                int tokenOffset = tokens.length - patternTokens.length;
                for ( int i = 0; matched && i < patternTokens.length; i++ )
                {
                    matched = matches( tokens[i + tokenOffset], patternTokens[i] );
                }
            }

            if ( matched )
            {
                patternsTriggered.add( pattern );
                return true;
            }

            if ( regionMatch && value.contains( pattern ) )
            {
                patternsTriggered.add( pattern );
                return true;
            }

        }
        return false;

    }

    private boolean isFirstPatternWildcard( String[] patternTokens )
    {
        return patternTokens.length > 0 && "*".equals( patternTokens[0] );
    }

    /**
     * Gets whether the specified token matches the specified pattern segment.
     *
     * @param token the token to check
     * @param pattern the pattern segment to match, as defined above
     * @return <code>true</code> if the specified token is matched by the specified pattern segment
     */
    private boolean matches( final String token, final String pattern )
    {
        boolean matches;

        // support full wildcard and implied wildcard
        if ( "*".equals( pattern ) || pattern.length() == 0 )
        {
            matches = true;
        }
        // support contains wildcard
        else if ( pattern.startsWith( "*" ) && pattern.endsWith( "*" ) )
        {
            final String contains = pattern.substring( 1, pattern.length() - 1 );

            matches = token.contains( contains );
        }
        // support leading wildcard
        else if ( pattern.startsWith( "*" ) )
        {
            final String suffix = pattern.substring( 1 );

            matches = token.endsWith( suffix );
        }
        // support trailing wildcard
        else if ( pattern.endsWith( "*" ) )
        {
            final String prefix = pattern.substring( 0, pattern.length() - 1 );

            matches = token.startsWith( prefix );
        }
        // support wildcards in the middle of a pattern segment
        else if ( pattern.indexOf( '*' ) > -1 )
        {
            String[] parts = pattern.split( "\\*" );
            int lastPartEnd = -1;
            boolean match = true;

            for ( String part : parts )
            {
                int idx = token.indexOf( part );
                if ( idx <= lastPartEnd )
                {
                    match = false;
                    break;
                }

                lastPartEnd = idx + part.length();
            }

            matches = match;
        }
        // support versions range
        else if ( pattern.startsWith( "[" ) || pattern.startsWith( "(" ) )
        {
            matches = isVersionIncludedInRange( token, pattern );
        }
        // support exact match
        else
        {
            matches = token.equals( pattern );
        }

        return matches;
    }

    private boolean isVersionIncludedInRange( final String version, final String range )
    {
        try
        {
            return VersionRange.createFromVersionSpec( range ).containsVersion( new DefaultArtifactVersion( version ) );
        }
        catch ( final InvalidVersionSpecificationException e )
        {
            return false;
        }
    }

    /** {@inheritDoc} */
    public void reportMissedCriteria( final Logger logger )
    {
        // if there are no patterns, there is nothing to report.
        if ( !positivePatterns.isEmpty() || !negativePatterns.isEmpty() )
        {
            final List<String> missed = new ArrayList<>();
            missed.addAll( positivePatterns );
            missed.addAll( negativePatterns );

            missed.removeAll( patternsTriggered );

            if ( !missed.isEmpty() && logger.isWarnEnabled() )
            {
                final StringBuilder buffer = new StringBuilder();

                buffer.append( "The following patterns were never triggered in this " );
                buffer.append( getFilterDescription() );
                buffer.append( ':' );

                for ( String pattern : missed )
                {
                    buffer.append( "\no  '" ).append( pattern ).append( "'" );
                }

                buffer.append( "\n" );

                logger.warn( buffer.toString() );
            }
        }
    }

    @Override
    public String toString()
    {
        return "Includes filter:" + getPatternsAsString();
    }

    /**
     * @return pattern as a string.
     */
    protected String getPatternsAsString()
    {
        final StringBuilder buffer = new StringBuilder();
        for ( String pattern : positivePatterns )
        {
            buffer.append( "\no '" ).append( pattern ).append( "'" );
        }

        return buffer.toString();
    }

    /**
     * @return description.
     */
    protected String getFilterDescription()
    {
        return "artifact inclusion filter";
    }

    /** {@inheritDoc} */
    public void reportFilteredArtifacts( final Logger logger )
    {
        if ( !filteredArtifactIds.isEmpty() && logger.isDebugEnabled() )
        {
            final StringBuilder buffer =
                    new StringBuilder( "The following artifacts were removed by this " + getFilterDescription() + ": " );

            for ( String artifactId : filteredArtifactIds )
            {
                buffer.append( '\n' ).append( artifactId );
            }

            logger.debug( buffer.toString() );
        }
    }

    /** {@inheritDoc} */
    public boolean hasMissedCriteria()
    {
        // if there are no patterns, there is nothing to report.
        if ( !positivePatterns.isEmpty() || !negativePatterns.isEmpty() )
        {
            final List<String> missed = new ArrayList<>();
            missed.addAll( positivePatterns );
            missed.addAll( negativePatterns );

            missed.removeAll( patternsTriggered );

            return !missed.isEmpty();
        }

        return false;
    }

}