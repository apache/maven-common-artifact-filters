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
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;
import org.slf4j.Logger;

import static java.util.Objects.requireNonNull;

/**
 * TODO: include in maven-artifact in future
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @see StrictPatternIncludesArtifactFilter
 */
public class PatternIncludesArtifactFilter implements ArtifactFilter, StatisticsReportingArtifactFilter
{
    private static final String SEP = System.lineSeparator();

    /**
     * Holds the set of compiled patterns
     */
    private final Set<Pattern> patterns;

    /**
     * Whether the dependency trail should be checked
     */
    private final boolean actTransitively;

    /**
     * Set of patterns that have been triggered
     */
    private final Set<Pattern> patternsTriggered = new HashSet<>();

    /**
     * Set of artifacts that have been filtered out
     */
    private final List<Artifact> filteredArtifact = new ArrayList<>();

    /**
     * <p>Constructor for PatternIncludesArtifactFilter.</p>
     *
     * @param patterns The pattern to be used.
     */
    public PatternIncludesArtifactFilter( final Collection<String> patterns )
    {
        this( patterns, false );
    }

    /**
     * <p>Constructor for PatternIncludesArtifactFilter.</p>
     *
     * @param patterns        The pattern to be used.
     * @param actTransitively transitive yes/no.
     */
    public PatternIncludesArtifactFilter( final Collection<String> patterns, final boolean actTransitively )
    {
        this.actTransitively = actTransitively;
        final Set<Pattern> pat = new LinkedHashSet<>();
        if ( patterns != null && !patterns.isEmpty() )
        {
            for ( String pattern : patterns )
            {
                Pattern p = compile( pattern );
                pat.add( p );
            }
        }
        this.patterns = pat;
    }

    @Override
    public boolean include( final Artifact artifact )
    {
        final boolean shouldInclude = patternMatches( artifact );

        if ( !shouldInclude )
        {
            addFilteredArtifact( artifact );
        }

        return shouldInclude;
    }

    protected boolean patternMatches( final Artifact artifact )
    {
        Boolean match = match( adapt( artifact ) );
        if ( match != null )
        {
            return match;
        }

        if ( actTransitively )
        {
            final List<String> depTrail = artifact.getDependencyTrail();

            if ( depTrail != null && depTrail.size() > 1 )
            {
                for ( String trailItem : depTrail )
                {
                    Artifactoid artifactoid = adapt( trailItem );
                    match = match( artifactoid );
                    if ( match != null )
                    {
                        return match;
                    }
                }
            }
        }

        return false;
    }

    private Boolean match( Artifactoid artifactoid )
    {
        for ( Pattern pattern : patterns )
        {
            if ( pattern.matches( artifactoid ) )
            {
                patternsTriggered.add( pattern );
                return !( pattern instanceof NegativePattern );
            }
        }

        return null;
    }

    /**
     * <p>addFilteredArtifact.</p>
     *
     * @param artifact add artifact to the filtered artifacts list.
     */
    protected void addFilteredArtifact( final Artifact artifact )
    {
        filteredArtifact.add( artifact );
    }

    @Override
    public void reportMissedCriteria( final Logger logger )
    {
        // if there are no patterns, there is nothing to report.
        if ( !patterns.isEmpty() )
        {
            final List<Pattern> missed = new ArrayList<>( patterns );
            missed.removeAll( patternsTriggered );

            if ( !missed.isEmpty() && logger.isWarnEnabled() )
            {
                final StringBuilder buffer = new StringBuilder();

                buffer.append( "The following patterns were never triggered in this " );
                buffer.append( getFilterDescription() );
                buffer.append( ':' );

                for ( Pattern pattern : missed )
                {
                    buffer.append( SEP ) .append( "o  '" ).append( pattern ).append( "'" );
                }

                buffer.append( SEP );

                logger.warn( buffer.toString() );
            }
        }
    }

    @Override
    public String toString()
    {
        return "Includes filter:" + getPatternsAsString();
    }

    protected String getPatternsAsString()
    {
        final StringBuilder buffer = new StringBuilder();
        for ( Pattern pattern : patterns )
        {
            buffer.append( SEP ).append( "o '" ).append( pattern ).append( "'" );
        }

        return buffer.toString();
    }

    protected String getFilterDescription()
    {
        return "artifact inclusion filter";
    }

    @Override
    public void reportFilteredArtifacts( final Logger logger )
    {
        if ( !filteredArtifact.isEmpty() && logger.isDebugEnabled() )
        {
            final StringBuilder buffer = new StringBuilder(
                    "The following artifacts were removed by this " + getFilterDescription() + ": " );

            for ( Artifact artifactId : filteredArtifact )
            {
                buffer.append( SEP ).append( artifactId.getId() );
            }

            logger.debug( buffer.toString() );
        }
    }

    @Override
    public boolean hasMissedCriteria()
    {
        // if there are no patterns, there is nothing to report.
        if ( !patterns.isEmpty() )
        {
            final List<Pattern> missed = new ArrayList<>( patterns );
            missed.removeAll( patternsTriggered );
            return !missed.isEmpty();
        }

        return false;
    }

    private enum Coordinate
    {
        GROUP_ID, ARTIFACT_ID, TYPE, CLASSIFIER, BASE_VERSION
    }

    private interface Artifactoid
    {
        String getCoordinate( Coordinate coordinate );
    }

    private static Artifactoid adapt( final Artifact artifact )
    {
        requireNonNull( artifact );
        return coordinate ->
        {
            requireNonNull( coordinate );
            switch ( coordinate )
            {
                case GROUP_ID:
                    return artifact.getGroupId();
                case ARTIFACT_ID:
                    return artifact.getArtifactId();
                case BASE_VERSION:
                    return artifact.getBaseVersion();
                case CLASSIFIER:
                    return artifact.hasClassifier() ? artifact.getClassifier() : null;
                case TYPE:
                    return artifact.getType();
                default:
            }
            throw new IllegalArgumentException( "unknown coordinate: " + coordinate );
        };
    }

    /**
     * Parses elements of {@link Artifact#getDependencyTrail()} list, they are either {@code G:A:T:V} or if artifact
     * has classifier {@code G:A:T:C:V}, so strictly 4 or 5 segments only.
     */
    private static Artifactoid adapt( final String depTrailString )
    {
        requireNonNull( depTrailString );
        String[] coordinates = depTrailString.split( ":" );
        if ( coordinates.length != 4 && coordinates.length != 5 )
        {
            throw new IllegalArgumentException( "Bad dep trail string: " + depTrailString );
        }
        final HashMap<Coordinate, String> map = new HashMap<>();
        map.put( Coordinate.GROUP_ID, coordinates[0] );
        map.put( Coordinate.ARTIFACT_ID, coordinates[1] );
        map.put( Coordinate.TYPE, coordinates[2] );
        if ( coordinates.length == 5 )
        {
            map.put( Coordinate.CLASSIFIER, coordinates[3] );
            map.put( Coordinate.BASE_VERSION, coordinates[4] );
        }
        else
        {
            map.put( Coordinate.BASE_VERSION, coordinates[3] );
        }

        return coordinate ->
        {
            requireNonNull( coordinate );
            return map.get( coordinate );
        };
    }

    private static final String ANY = "*";

    /**
     * Splits the pattern string into tokens, replacing empty tokens with {@link #ANY} for patterns like {@code ::val}
     * so it retains the position of token.
     */
    private static String[] splitAndTokenize( String pattern )
    {
        String[] stokens = pattern.split( ":" );
        String[] tokens = new String[stokens.length];
        for ( int i = 0; i < stokens.length; i++ )
        {
            String str = stokens[i];
            tokens[i] = str != null && !str.isEmpty() ? str : ANY;
        }
        return tokens;
    }

    /**
     * Compiles pattern string into {@link Pattern}.
     *
     * TODO: patterns seems NOT documented anywhere, so best we have is source below.
     * TODO: patterns in some cases (3, 2 tokens) seems ambiguous, we may need to clean up the specs
     */
    private static Pattern compile( String pattern )
    {
        if ( pattern.startsWith( "!" ) )
        {
            return new NegativePattern( pattern, compile( pattern.substring( 1 ) ) );
        }
        else
        {
            String[] tokens = splitAndTokenize( pattern );
            if ( tokens.length < 1 || tokens.length > 5 )
            {
                throw new IllegalArgumentException( "Invalid pattern: " + pattern );
            }

            ArrayList<Pattern> patterns = new ArrayList<>( 5 );

            if ( tokens.length == 5 )
            {
                // trivial, full pattern w/ classifier: G:A:T:C:V
                patterns.add( toPattern( tokens[0], Coordinate.GROUP_ID ) );
                patterns.add( toPattern( tokens[1], Coordinate.ARTIFACT_ID ) );
                patterns.add( toPattern( tokens[2], Coordinate.TYPE ) );
                patterns.add( toPattern( tokens[3], Coordinate.CLASSIFIER ) );
                patterns.add( toPattern( tokens[4], Coordinate.BASE_VERSION ) );
            }
            else if ( tokens.length == 4 )
            {
                // trivial, full pattern w/ version or classifier: G:A:T:V or G:A:T:C
                patterns.add( toPattern( tokens[0], Coordinate.GROUP_ID ) );
                patterns.add( toPattern( tokens[1], Coordinate.ARTIFACT_ID ) );
                patterns.add( toPattern( tokens[2], Coordinate.TYPE ) );
                patterns.add( toPattern( tokens[3], Coordinate.BASE_VERSION, Coordinate.CLASSIFIER ) );
            }
            else if ( tokens.length == 3 )
            {
                // tricky: may be "*:artifact:*" but also "*:war:*"

                // *:*:* -> ALL
                // *:*:xxx -> TC(xxx)
                // *:xxx:* -> AT(xxx)
                // *:xxx:yyy -> GA(xxx) + TC(XXX)
                // xxx:*:* -> GA(xxx)
                // xxx:*:yyy -> G(xxx) + TC(yyy)
                // xxx:yyy:* -> G(xxx)+A(yyy)
                // xxx:yyy:zzz -> G(xxx)+A(yyy)+T(zzz)
                if ( ANY.equals( tokens[0] ) && ANY.equals( tokens[1] ) && ANY.equals( tokens[2] ) )
                {
                    patterns.add( MATCH_ALL_PATTERN );
                }
                else if ( ANY.equals( tokens[0] ) && ANY.equals( tokens[1] ) )
                {
                    patterns.add( toPattern( pattern, tokens[2], Coordinate.TYPE, Coordinate.CLASSIFIER ) );
                }
                else if ( ANY.equals( tokens[0] ) && ANY.equals( tokens[2] ) )
                {
                    patterns.add( toPattern( pattern, tokens[1], Coordinate.ARTIFACT_ID, Coordinate.TYPE ) );
                }
                else if ( ANY.equals( tokens[0] ) )
                {
                    patterns.add( toPattern( pattern, tokens[1], Coordinate.GROUP_ID, Coordinate.ARTIFACT_ID ) );
                    patterns.add( toPattern( pattern, tokens[2], Coordinate.TYPE, Coordinate.CLASSIFIER ) );
                }
                else if ( ANY.equals( tokens[1] ) && ANY.equals( tokens[2] ) )
                {
                    patterns.add( toPattern( pattern, tokens[0], Coordinate.GROUP_ID, Coordinate.ARTIFACT_ID ) );
                }
                else if ( ANY.equals( tokens[1] ) )
                {
                    patterns.add( toPattern( tokens[0], tokens[0], Coordinate.GROUP_ID ) );
                    patterns.add( toPattern( pattern, tokens[2], Coordinate.TYPE, Coordinate.CLASSIFIER ) );
                }
                else if ( ANY.equals( tokens[2] ) )
                {
                    patterns.add( toPattern( tokens[0], Coordinate.GROUP_ID ) );
                    patterns.add( toPattern( tokens[1], Coordinate.ARTIFACT_ID ) );
                }
                else
                {
                    patterns.add( toPattern( tokens[0], Coordinate.GROUP_ID ) );
                    patterns.add( toPattern( tokens[1], Coordinate.ARTIFACT_ID ) );
                    patterns.add( toPattern( tokens[2], Coordinate.TYPE ) );
                }

            }
            else if ( tokens.length == 2 )
            {
                // tricky: may be "*:artifact" but also "*:war"
                // *:* -> ALL
                // *:xxx -> GATV(xxx)
                // xxx:* -> G(xxx)
                // xxx:yyy -> G(xxx)+A(yyy)

                if ( ANY.equals( tokens[0] ) && ANY.equals( tokens[1] ) )
                {
                    patterns.add( MATCH_ALL_PATTERN );
                }
                else if ( ANY.equals( tokens[0] ) )
                {
                    patterns.add( toPattern( pattern, tokens[1],
                            Coordinate.GROUP_ID, Coordinate.ARTIFACT_ID, Coordinate.TYPE, Coordinate.BASE_VERSION ) );
                }
                else if ( ANY.equals( tokens[1] ) )
                {
                    patterns.add( toPattern( tokens[0], Coordinate.GROUP_ID ) );
                }
                else
                {
                    patterns.add( toPattern( tokens[0], Coordinate.GROUP_ID ) );
                    patterns.add( toPattern( tokens[1], Coordinate.ARTIFACT_ID ) );
                }
            }
            else
            {
                // trivial: G
                patterns.add( toPattern( tokens[0], Coordinate.GROUP_ID ) );
            }

            // build result if needed and retains pattern string
            if ( patterns.size() == 1 )
            {
                Pattern pat = patterns.get( 0 );
                if ( pat == MATCH_ALL_PATTERN )
                {
                    return new MatchAllPattern( pattern );
                }
                else
                {
                    return pat;
                }
            }
            else
            {
                return new AndPattern( pattern, patterns.toArray( new Pattern[0] ) );
            }
        }
    }

    private static Pattern toPattern( final String token, final Coordinate... coordinate )
    {
        return toPattern( token, token, coordinate );
    }

    private static Pattern toPattern( final String pattern, final String token, final Coordinate... coordinate )
    {
        if ( ANY.equals( token ) )
        {
            return MATCH_ALL_PATTERN;
        }
        else
        {
            EnumSet<Coordinate> coordinates = EnumSet.noneOf( Coordinate.class );
            coordinates.addAll( Arrays.asList( coordinate ) );
            return new CoordinateMatchingPattern( pattern, token, coordinates );
        }
    }

    private static final Pattern MATCH_ALL_PATTERN = new MatchAllPattern( ANY );

    private abstract static class Pattern
    {
        protected final String pattern;

        private Pattern( String pattern )
        {
            this.pattern = requireNonNull( pattern );
        }

        public abstract boolean matches( Artifactoid artifact );

        @Override
        public String toString()
        {
            return pattern;
        }
    }

    private static class AndPattern extends Pattern
    {
        private final Pattern[] patterns;

        private AndPattern( String pattern, Pattern[] patterns )
        {
            super( pattern );
            this.patterns = patterns;
        }

        @Override
        public boolean matches( Artifactoid artifactoid )
        {
            for ( Pattern pattern : patterns )
            {
                if ( !pattern.matches( artifactoid ) )
                {
                    return false;
                }
            }
            return true;
        }
    }

    private static class CoordinateMatchingPattern extends Pattern
    {
        private final String token;

        private final EnumSet<Coordinate> coordinates;

        private final boolean containsWildcard;

        private final boolean containsAsterisk;

        private final VersionRange optionalVersionRange;

        private CoordinateMatchingPattern( String pattern, String token, EnumSet<Coordinate> coordinates )
        {
            super( pattern );
            this.token = token;
            this.coordinates = coordinates;
            this.containsAsterisk = token.contains( "*" );
            this.containsWildcard = this.containsAsterisk || token.contains( "?" );
            if ( !this.containsWildcard && coordinates.equals( EnumSet.of( Coordinate.BASE_VERSION ) ) && (
                    token.startsWith( "[" ) || token.startsWith( "(" ) ) )
            {
                try
                {
                    this.optionalVersionRange = VersionRange.createFromVersionSpec( token );
                }
                catch ( InvalidVersionSpecificationException e )
                {
                    throw new IllegalArgumentException( "Wrong version spec: " + token, e );
                }
            }
            else
            {
                this.optionalVersionRange = null;
            }
        }

        @Override
        public boolean matches( Artifactoid artifactoid )
        {
            for ( Coordinate coordinate : coordinates )
            {
                String value = artifactoid.getCoordinate( coordinate );
                if ( Coordinate.BASE_VERSION == coordinate && optionalVersionRange != null )
                {
                    if ( optionalVersionRange.containsVersion( new DefaultArtifactVersion( value ) ) )
                    {
                        return true;
                    }
                }
                else if ( containsWildcard )
                {
                    if ( match( token, containsAsterisk, value ) )
                    {
                        return true;
                    }
                }
                else
                {
                    if ( token.equals( value ) )
                    {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    /**
     * Matches all input
     */
    private static class MatchAllPattern extends Pattern
    {
        private MatchAllPattern( String pattern )
        {
            super( pattern );
        }

        @Override
        public boolean matches( Artifactoid artifactoid )
        {
            return true;
        }
    }

    /**
     * Negative pattern
     */
    private static class NegativePattern extends Pattern
    {
        private final Pattern inner;

        private NegativePattern( String pattern, Pattern inner )
        {
            super( pattern );
            this.inner = inner;
        }

        @Override
        public boolean matches( Artifactoid artifactoid )
        {
            return inner.matches( artifactoid );
        }
    }

    // this beauty below must be salvaged

    @SuppressWarnings( "InnerAssignment" )
    private static boolean match( final String pattern, final boolean containsAsterisk, final String value )
    {
        char[] patArr = pattern.toCharArray();
        char[] strArr = value.toCharArray();
        int patIdxStart = 0;
        int patIdxEnd = patArr.length - 1;
        int strIdxStart = 0;
        int strIdxEnd = strArr.length - 1;
        char ch;

        if ( !containsAsterisk )
        {
            // No '*'s, so we make a shortcut
            if ( patIdxEnd != strIdxEnd )
            {
                return false; // Pattern and string do not have the same size
            }
            for ( int i = 0; i <= patIdxEnd; i++ )
            {
                ch = patArr[i];
                if ( ch != '?' && ch != strArr[i] )
                {
                    return false; // Character mismatch
                }
            }
            return true; // String matches against pattern
        }

        if ( patIdxEnd == 0 )
        {
            return true; // Pattern contains only '*', which matches anything
        }

        // Process characters before first star
        while ( ( ch = patArr[patIdxStart] ) != '*' && strIdxStart <= strIdxEnd )
        {
            if ( ch != '?' && ch != strArr[strIdxStart] )
            {
                return false; // Character mismatch
            }
            patIdxStart++;
            strIdxStart++;
        }
        if ( strIdxStart > strIdxEnd )
        {
            // All characters in the string are used. Check if only '*'s are
            // left in the pattern. If so, we succeeded. Otherwise failure.
            for ( int i = patIdxStart; i <= patIdxEnd; i++ )
            {
                if ( patArr[i] != '*' )
                {
                    return false;
                }
            }
            return true;
        }

        // Process characters after last star
        while ( ( ch = patArr[patIdxEnd] ) != '*' && strIdxStart <= strIdxEnd )
        {
            if ( ch != '?' && ch != strArr[strIdxEnd] )
            {
                return false; // Character mismatch
            }
            patIdxEnd--;
            strIdxEnd--;
        }
        if ( strIdxStart > strIdxEnd )
        {
            // All characters in the string are used. Check if only '*'s are
            // left in the pattern. If so, we succeeded. Otherwise failure.
            for ( int i = patIdxStart; i <= patIdxEnd; i++ )
            {
                if ( patArr[i] != '*' )
                {
                    return false;
                }
            }
            return true;
        }

        // process pattern between stars. padIdxStart and patIdxEnd point
        // always to a '*'.
        while ( patIdxStart != patIdxEnd && strIdxStart <= strIdxEnd )
        {
            int patIdxTmp = -1;
            for ( int i = patIdxStart + 1; i <= patIdxEnd; i++ )
            {
                if ( patArr[i] == '*' )
                {
                    patIdxTmp = i;
                    break;
                }
            }
            if ( patIdxTmp == patIdxStart + 1 )
            {
                // Two stars next to each other, skip the first one.
                patIdxStart++;
                continue;
            }
            // Find the pattern between padIdxStart & padIdxTmp in str between
            // strIdxStart & strIdxEnd
            int patLength = ( patIdxTmp - patIdxStart - 1 );
            int strLength = ( strIdxEnd - strIdxStart + 1 );
            int foundIdx = -1;
            strLoop:
            for ( int i = 0; i <= strLength - patLength; i++ )
            {
                for ( int j = 0; j < patLength; j++ )
                {
                    ch = patArr[patIdxStart + j + 1];
                    if ( ch != '?' && ch != strArr[strIdxStart + i + j] )
                    {
                        continue strLoop;
                    }
                }

                foundIdx = strIdxStart + i;
                break;
            }

            if ( foundIdx == -1 )
            {
                return false;
            }

            patIdxStart = patIdxTmp;
            strIdxStart = foundIdx + patLength;
        }

        // All characters in the string are used. Check if only '*'s are left
        // in the pattern. If so, we succeeded. Otherwise failure.
        for ( int i = patIdxStart; i <= patIdxEnd; i++ )
        {
            if ( patArr[i] != '*' )
            {
                return false;
            }
        }
        return true;
    }
}
