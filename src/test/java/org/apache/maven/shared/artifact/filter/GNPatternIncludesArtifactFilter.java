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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
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
public class GNPatternIncludesArtifactFilter implements ArtifactFilter, StatisticsReportingArtifactFilter {
    /** Holds the set of compiled patterns */
    private final Set<Pattern> patterns;

    /** Whether the dependency trail should be checked */
    private final boolean actTransitively;

    /** Set of patterns that have been triggered */
    private final Set<Pattern> patternsTriggered = new HashSet<>();

    /** Set of artifacts that have been filtered out */
    private final List<Artifact> filteredArtifact = new ArrayList<>();

    /**
     * <p>Constructor for PatternIncludesArtifactFilter.</p>
     *
     * @param patterns The pattern to be used.
     */
    public GNPatternIncludesArtifactFilter(final Collection<String> patterns) {
        this(patterns, false);
    }

    /**
     * <p>Constructor for PatternIncludesArtifactFilter.</p>
     *
     * @param patterns The pattern to be used.
     * @param actTransitively transitive yes/no.
     */
    public GNPatternIncludesArtifactFilter(final Collection<String> patterns, final boolean actTransitively) {
        this.actTransitively = actTransitively;
        final Set<Pattern> pat = new LinkedHashSet<>();
        if (patterns != null && !patterns.isEmpty()) {
            for (String pattern : patterns) {

                Pattern p = compile(pattern);
                pat.add(p);
            }
        }
        this.patterns = pat;
    }

    /** {@inheritDoc} */
    public boolean include(final Artifact artifact) {
        final boolean shouldInclude = patternMatches(artifact);

        if (!shouldInclude) {
            addFilteredArtifact(artifact);
        }

        return shouldInclude;
    }

    /**
     * <p>patternMatches.</p>
     *
     * @param artifact to check for.
     * @return true if the match is true false otherwise.
     */
    protected boolean patternMatches(final Artifact artifact) {
        // Check if the main artifact matches
        char[][] artifactGatvCharArray = new char[][] {
            emptyOrChars(artifact.getGroupId()),
            emptyOrChars(artifact.getArtifactId()),
            emptyOrChars(artifact.getType()),
            emptyOrChars(artifact.getClassifier()),
            emptyOrChars(artifact.getBaseVersion())
        };
        Boolean match = match(artifactGatvCharArray);
        if (match != null) {
            return match;
        }

        if (actTransitively) {
            final List<String> depTrail = artifact.getDependencyTrail();

            if (depTrail != null && depTrail.size() > 1) {
                for (String trailItem : depTrail) {
                    char[][] depGatvCharArray = tokenizeAndSplit(trailItem);
                    match = match(depGatvCharArray);
                    if (match != null) {
                        return match;
                    }
                }
            }
        }

        return false;
    }

    private Boolean match(char[][] gatvCharArray) {
        for (Pattern pattern : patterns) {
            if (pattern.matches(gatvCharArray)) {
                patternsTriggered.add(pattern);
                return !(pattern instanceof NegativePattern);
            }
        }

        return null;
    }

    /**
     * <p>addFilteredArtifact.</p>
     *
     * @param artifact add artifact to the filtered artifacts list.
     */
    protected void addFilteredArtifact(final Artifact artifact) {
        filteredArtifact.add(artifact);
    }

    /** {@inheritDoc} */
    public void reportMissedCriteria(final Logger logger) {
        // if there are no patterns, there is nothing to report.
        if (!patterns.isEmpty()) {
            final List<Pattern> missed = new ArrayList<>(patterns);
            missed.removeAll(patternsTriggered);

            if (!missed.isEmpty() && logger.isWarnEnabled()) {
                final StringBuilder buffer = new StringBuilder();

                buffer.append("The following patterns were never triggered in this ");
                buffer.append(getFilterDescription());
                buffer.append(':');

                for (Pattern pattern : missed) {
                    buffer.append("\no  '").append(pattern).append("'");
                }

                buffer.append("\n");

                logger.warn(buffer.toString());
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "Includes filter:" + getPatternsAsString();
    }

    /**
     * <p>getPatternsAsString.</p>
     *
     * @return pattern as a string.
     */
    protected String getPatternsAsString() {
        final StringBuilder buffer = new StringBuilder();
        for (Pattern pattern : patterns) {
            buffer.append("\no '").append(pattern).append("'");
        }

        return buffer.toString();
    }

    /**
     * <p>getFilterDescription.</p>
     *
     * @return description.
     */
    protected String getFilterDescription() {
        return "artifact inclusion filter";
    }

    /** {@inheritDoc} */
    public void reportFilteredArtifacts(final Logger logger) {
        if (!filteredArtifact.isEmpty() && logger.isDebugEnabled()) {
            final StringBuilder buffer =
                    new StringBuilder("The following artifacts were removed by this " + getFilterDescription() + ": ");

            for (Artifact artifactId : filteredArtifact) {
                buffer.append('\n').append(artifactId.getId());
            }

            logger.debug(buffer.toString());
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return a boolean.
     */
    public boolean hasMissedCriteria() {
        // if there are no patterns, there is nothing to report.
        if (!patterns.isEmpty()) {
            final List<Pattern> missed = new ArrayList<>(patterns);
            missed.removeAll(patternsTriggered);

            return !missed.isEmpty();
        }

        return false;
    }

    private static final char[] EMPTY = new char[0];

    private static final char[] ANY = new char[] {'*'};

    static char[] emptyOrChars(String str) {
        return str != null && !str.isEmpty() ? str.toCharArray() : EMPTY;
    }

    static char[] anyOrChars(char[] str) {
        return str.length > 1 || (str.length == 1 && str[0] != '*') ? str : ANY;
    }

    static char[][] tokenizeAndSplit(String pattern) {
        String[] stokens = pattern.split(":");
        char[][] tokens = new char[stokens.length][];
        for (int i = 0; i < stokens.length; i++) {
            tokens[i] = emptyOrChars(stokens[i]);
        }
        return tokens;
    }

    @SuppressWarnings("InnerAssignment")
    static boolean match(char[] patArr, char[] strArr, boolean isVersion) {
        int patIdxStart = 0;
        int patIdxEnd = patArr.length - 1;
        int strIdxStart = 0;
        int strIdxEnd = strArr.length - 1;
        char ch;

        boolean containsStar = false;
        for (char aPatArr : patArr) {
            if (aPatArr == '*') {
                containsStar = true;
                break;
            }
        }

        if (!containsStar) {
            if (isVersion && (patArr[0] == '[' || patArr[0] == '(')) {
                return isVersionIncludedInRange(String.valueOf(strArr), String.valueOf(patArr));
            }
            // No '*'s, so we make a shortcut
            if (patIdxEnd != strIdxEnd) {
                return false; // Pattern and string do not have the same size
            }
            for (int i = 0; i <= patIdxEnd; i++) {
                ch = patArr[i];
                if (ch != '?' && ch != strArr[i]) {
                    return false; // Character mismatch
                }
            }
            return true; // String matches against pattern
        }

        if (patIdxEnd == 0) {
            return true; // Pattern contains only '*', which matches anything
        }

        // Process characters before first star
        while ((ch = patArr[patIdxStart]) != '*' && strIdxStart <= strIdxEnd) {
            if (ch != '?' && ch != strArr[strIdxStart]) {
                return false; // Character mismatch
            }
            patIdxStart++;
            strIdxStart++;
        }
        if (strIdxStart > strIdxEnd) {
            // All characters in the string are used. Check if only '*'s are
            // left in the pattern. If so, we succeeded. Otherwise failure.
            for (int i = patIdxStart; i <= patIdxEnd; i++) {
                if (patArr[i] != '*') {
                    return false;
                }
            }
            return true;
        }

        // Process characters after last star
        while ((ch = patArr[patIdxEnd]) != '*' && strIdxStart <= strIdxEnd) {
            if (ch != '?' && ch != strArr[strIdxEnd]) {
                return false; // Character mismatch
            }
            patIdxEnd--;
            strIdxEnd--;
        }
        if (strIdxStart > strIdxEnd) {
            // All characters in the string are used. Check if only '*'s are
            // left in the pattern. If so, we succeeded. Otherwise failure.
            for (int i = patIdxStart; i <= patIdxEnd; i++) {
                if (patArr[i] != '*') {
                    return false;
                }
            }
            return true;
        }

        // process pattern between stars. padIdxStart and patIdxEnd point
        // always to a '*'.
        while (patIdxStart != patIdxEnd && strIdxStart <= strIdxEnd) {
            int patIdxTmp = -1;
            for (int i = patIdxStart + 1; i <= patIdxEnd; i++) {
                if (patArr[i] == '*') {
                    patIdxTmp = i;
                    break;
                }
            }
            if (patIdxTmp == patIdxStart + 1) {
                // Two stars next to each other, skip the first one.
                patIdxStart++;
                continue;
            }
            // Find the pattern between padIdxStart & padIdxTmp in str between
            // strIdxStart & strIdxEnd
            int patLength = (patIdxTmp - patIdxStart - 1);
            int strLength = (strIdxEnd - strIdxStart + 1);
            int foundIdx = -1;
            strLoop:
            for (int i = 0; i <= strLength - patLength; i++) {
                for (int j = 0; j < patLength; j++) {
                    ch = patArr[patIdxStart + j + 1];
                    if (ch != '?' && ch != strArr[strIdxStart + i + j]) {
                        continue strLoop;
                    }
                }

                foundIdx = strIdxStart + i;
                break;
            }

            if (foundIdx == -1) {
                return false;
            }

            patIdxStart = patIdxTmp;
            strIdxStart = foundIdx + patLength;
        }

        // All characters in the string are used. Check if only '*'s are left
        // in the pattern. If so, we succeeded. Otherwise failure.
        for (int i = patIdxStart; i <= patIdxEnd; i++) {
            if (patArr[i] != '*') {
                return false;
            }
        }
        return true;
    }

    static boolean isVersionIncludedInRange(final String version, final String range) {
        try {
            return VersionRange.createFromVersionSpec(range).containsVersion(new DefaultArtifactVersion(version));
        } catch (final InvalidVersionSpecificationException e) {
            return false;
        }
    }

    static Pattern compile(String pattern) {
        if (pattern.startsWith("!")) {
            return new NegativePattern(pattern, compile(pattern.substring(1)));
        } else {
            char[][] stokens = tokenizeAndSplit(pattern);
            char[][] tokens = new char[stokens.length][];
            for (int i = 0; i < stokens.length; i++) {
                tokens[i] = anyOrChars(stokens[i]);
            }
            if (tokens.length > 5) {
                throw new IllegalArgumentException("Invalid pattern: " + pattern);
            }
            //
            // Check the tokens and build an appropriate Pattern
            // Special care needs to be taken if the first or the last part is '*'
            // because this allows the '*' to match multiple tokens
            //
            if (tokens.length == 1) {
                if (tokens[0] == ANY) {
                    // *
                    return all(pattern);
                } else {
                    // [pat0]
                    return match(pattern, tokens[0], 0);
                }
            }
            if (tokens.length == 2) {
                if (tokens[0] == ANY) {
                    if (tokens[1] == ANY) {
                        // *:*
                        return all(pattern);
                    } else {
                        // *:[pat1]
                        return match(pattern, tokens[1], 0, 3);
                    }
                } else {
                    if (tokens[1] == ANY) {
                        // [pat0]:*
                        return match(pattern, tokens[0], 0);
                    } else {
                        // [pat0]:[pat1]
                        Pattern m00 = match(tokens[0], 0);
                        Pattern m11 = match(tokens[1], 1);
                        return and(pattern, m00, m11);
                    }
                }
            }
            if (tokens.length == 3) {
                if (tokens[0] == ANY) {
                    if (tokens[1] == ANY) {
                        if (tokens[2] == ANY) {
                            // *:*:*
                            return all(pattern);
                        } else {
                            // *:*:[pat2]
                            return match(pattern, tokens[2], 2, 3);
                        }
                    } else {
                        if (tokens[2] == ANY) {
                            // *:[pat1]:*
                            return match(pattern, tokens[1], 1, 2);
                        } else {
                            // *:[pat1]:[pat2]
                            Pattern m11 = match(tokens[1], 1);
                            Pattern m12 = match(tokens[1], 2);
                            Pattern m22 = match(tokens[2], 2);
                            Pattern m23 = match(tokens[2], 3);
                            return or(pattern, and(m11, m22), and(m12, m23));
                        }
                    }
                } else {
                    if (tokens[1] == ANY) {
                        if (tokens[2] == ANY) {
                            // [pat0]:*:*
                            return match(pattern, tokens[0], 0, 1);
                        } else {
                            // [pat0]:*:[pat2]
                            Pattern m00 = match(tokens[0], 0);
                            Pattern m223 = match(tokens[2], 2, 3);
                            return and(pattern, m00, m223);
                        }
                    } else {
                        if (tokens[2] == ANY) {
                            // [pat0]:[pat1]:*
                            Pattern m00 = match(tokens[0], 0);
                            Pattern m11 = match(tokens[1], 1);
                            return and(pattern, m00, m11);
                        } else {
                            // [pat0]:[pat1]:[pat2]
                            Pattern m00 = match(tokens[0], 0);
                            Pattern m11 = match(tokens[1], 1);
                            Pattern m22 = match(tokens[2], 2);
                            return and(pattern, m00, m11, m22);
                        }
                    }
                }
            }
            if (tokens.length >= 4) {
                List<Pattern> patterns = new ArrayList<>();
                for (int i = 0; i < tokens.length; i++) {
                    if (tokens[i] != ANY) {
                        patterns.add(match(tokens[i], i));
                    }
                }
                return and(pattern, patterns.toArray(new Pattern[0]));
            }
            throw new IllegalStateException();
        }
    }

    /** Creates a positional matching pattern */
    private static Pattern match(String pattern, char[] token, int posVal) {
        return match(pattern, token, posVal, posVal);
    }

    /** Creates a positional matching pattern */
    private static Pattern match(char[] token, int posVal) {
        return match("", token, posVal, posVal);
    }

    /** Creates a positional matching pattern */
    private static Pattern match(String pattern, char[] token, int posMin, int posMax) {
        boolean hasWildcard = false;
        for (char ch : token) {
            if (ch == '*' || ch == '?') {
                hasWildcard = true;
                break;
            }
        }
        if (hasWildcard || posMax == 4) {
            return new PosPattern(pattern, token, posMin, posMax);
        } else {
            return new EqPattern(pattern, token, posMin, posMax);
        }
    }

    /** Creates a positional matching pattern */
    private static Pattern match(char[] token, int posMin, int posMax) {
        return new PosPattern("", token, posMin, posMax);
    }

    /** Creates an AND pattern */
    private static Pattern and(String pattern, Pattern... patterns) {
        return new AndPattern(pattern, patterns);
    }

    /** Creates an AND pattern */
    private static Pattern and(Pattern... patterns) {
        return and("", patterns);
    }

    /** Creates an OR pattern */
    private static Pattern or(String pattern, Pattern... patterns) {
        return new OrPattern(pattern, patterns);
    }

    /** Creates an OR pattern */
    private static Pattern or(Pattern... patterns) {
        return or("", patterns);
    }

    /** Creates a match-all pattern */
    private static Pattern all(String pattern) {
        return new MatchAllPattern(pattern);
    }

    /**
     * Abstract class for patterns
     */
    abstract static class Pattern {
        private final String pattern;

        Pattern(String pattern) {
            this.pattern = Objects.requireNonNull(pattern);
        }

        public abstract boolean matches(char[][] parts);

        /**
         * Returns a string containing a fixed artifact gatv coordinates
         * or null if the pattern can not be translated.
         */
        public String translateEquals() {
            return null;
        }

        /**
         * Check if the this pattern is a fixed pattern on the specified pos.
         */
        protected String translateEquals(int pos) {
            return null;
        }

        @Override
        public String toString() {
            return pattern;
        }
    }

    /**
     * Simple pattern which performs a logical AND between one or more patterns.
     */
    static class AndPattern extends Pattern {
        private final Pattern[] patterns;

        AndPattern(String pattern, Pattern[] patterns) {
            super(pattern);
            this.patterns = patterns;
        }

        @Override
        public boolean matches(char[][] parts) {
            for (Pattern pattern : patterns) {
                if (!pattern.matches(parts)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public String translateEquals() {
            String[] strings = new String[patterns.length];
            for (int i = 0; i < patterns.length; i++) {
                strings[i] = patterns[i].translateEquals(i);
                if (strings[i] == null) {
                    return null;
                }
            }
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < strings.length; i++) {
                if (i > 0) {
                    sb.append(":");
                }
                sb.append(strings[i]);
            }
            return sb.toString();
        }
    }

    /**
     * Simple pattern which performs a logical OR between one or more patterns.
     */
    static class OrPattern extends Pattern {
        private final Pattern[] patterns;

        OrPattern(String pattern, Pattern[] patterns) {
            super(pattern);
            this.patterns = patterns;
        }

        @Override
        public boolean matches(char[][] parts) {
            for (Pattern pattern : patterns) {
                if (pattern.matches(parts)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * A positional matching pattern, to check if a token in the gatv coordinates
     * having a position between posMin and posMax (both inclusives) can match
     * the pattern.
     */
    static class PosPattern extends Pattern {
        private final char[] patternCharArray;
        private final int posMin;
        private final int posMax;

        PosPattern(String pattern, char[] patternCharArray, int posMin, int posMax) {
            super(pattern);
            this.patternCharArray = patternCharArray;
            this.posMin = posMin;
            this.posMax = posMax;
        }

        @Override
        public boolean matches(char[][] parts) {
            for (int i = posMin; i <= posMax; i++) {
                if (match(patternCharArray, parts[i], i == 4)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Looks for an exact match in the gatv coordinates between
     * posMin and posMax (both inclusives)
     */
    static class EqPattern extends Pattern {
        private final char[] token;
        private final int posMin;
        private final int posMax;

        EqPattern(String pattern, char[] patternCharArray, int posMin, int posMax) {
            super(pattern);
            this.token = patternCharArray;
            this.posMin = posMin;
            this.posMax = posMax;
        }

        @Override
        public boolean matches(char[][] parts) {
            for (int i = posMin; i <= posMax; i++) {
                if (Arrays.equals(token, parts[i])) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public String translateEquals() {
            return translateEquals(0);
        }

        public String translateEquals(int pos) {
            return posMin == pos && posMax == pos && (pos < 3 || (token[0] != '[' && token[0] != '('))
                    ? String.valueOf(token)
                    : null;
        }
    }

    /**
     * Matches all input
     */
    static class MatchAllPattern extends Pattern {
        MatchAllPattern(String pattern) {
            super(pattern);
        }

        @Override
        public boolean matches(char[][] parts) {
            return true;
        }
    }

    /**
     * Negative pattern
     */
    static class NegativePattern extends Pattern {
        private final Pattern inner;

        NegativePattern(String pattern, Pattern inner) {
            super(pattern);
            this.inner = inner;
        }

        @Override
        public boolean matches(char[][] parts) {
            return inner.matches(parts);
        }
    }
}
