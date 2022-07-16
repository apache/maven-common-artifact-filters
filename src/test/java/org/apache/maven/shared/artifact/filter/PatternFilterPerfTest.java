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

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 3, time = 3 )
public class PatternFilterPerfTest {

    @State(Scope.Benchmark)
    static public class OldPatternState {

        @Param({
                "groupId:artifact-00,groupId:artifact-01,groupId:artifact-02,groupId:artifact-03,groupId:artifact-04,groupId:artifact-05,groupId:artifact-06,groupId:artifact-07,groupId:artifact-08,groupId:artifact-09",
                "groupId:artifact-99",
                "groupId:artifact-*",
                "*:artifact-99",
                "*:artifact-*",
                "*:artifact-*:*",
                "*:artifact-99:*",
        })
        public String patterns;

        ArtifactFilter filter;
        Artifact artifact;

        @Setup(Level.Invocation)
        public void setup()
        {
            filter = new OldPatternIncludesArtifactFilter( Arrays.asList( patterns.split( "," ) ) );
            artifact = new DefaultArtifact(
                    "groupId", "artifact-99", "1.0", "runtime",
                    "jar", "", null
            );
        }

    }

    @State(Scope.Benchmark)
    static public class GNPatternState {

        @Param({
                "groupId:artifact-00,groupId:artifact-01,groupId:artifact-02,groupId:artifact-03,groupId:artifact-04,groupId:artifact-05,groupId:artifact-06,groupId:artifact-07,groupId:artifact-08,groupId:artifact-09",
                "groupId:artifact-99",
                "groupId:artifact-*",
                "*:artifact-99",
                "*:artifact-*",
                "*:artifact-*:*",
                "*:artifact-99:*",
        })
        public String patterns;

        ArtifactFilter filter;
        Artifact artifact;

        @Setup(Level.Invocation)
        public void setup()
        {
            filter = new GNPatternIncludesArtifactFilter( Arrays.asList( patterns.split( "," ) ) );
            artifact = new DefaultArtifact(
                    "groupId", "artifact-99", "1.0", "runtime",
                    "jar", "", null
            );
        }

    }

    @State(Scope.Benchmark)
    static public class NewPatternState {

        @Param({
                "groupId:artifact-00,groupId:artifact-01,groupId:artifact-02,groupId:artifact-03,groupId:artifact-04,groupId:artifact-05,groupId:artifact-06,groupId:artifact-07,groupId:artifact-08,groupId:artifact-09",
                "groupId:artifact-99",
                "groupId:artifact-*",
                "*:artifact-99",
                "*:artifact-*",
                "*:artifact-*:*",
                "*:artifact-99:*",
        })
        public String patterns;

        ArtifactFilter filter;
        Artifact artifact;

        @Setup(Level.Invocation)
        public void setup()
        {
            filter = new PatternIncludesArtifactFilter( Arrays.asList( patterns.split( "," ) ) );
            artifact = new DefaultArtifact(
                    "groupId", "artifact-99", "1.0", "runtime",
                    "jar", "", null
            );
        }

    }


    @Benchmark
    public boolean newPatternTest(NewPatternState state )
    {
        return state.filter.include( state.artifact );
    }

    @Benchmark
    public boolean gnPatternTest(GNPatternState state )
    {
        return state.filter.include( state.artifact );
    }

    @Benchmark
    public boolean oldPatternTest(OldPatternState state )
    {
        return state.filter.include( state.artifact );
    }

    public static void main( String... args )
            throws RunnerException
    {
        Options opts = new OptionsBuilder()
                .measurementIterations( 3 )
                .measurementTime( TimeValue.milliseconds( 3000 ) )
                .forks( 1 )
                .include( "org.apache.maven.shared.artifact.filter.PatternFilterPerfTest" )
                .build();
        new Runner( opts ).run();
    }
}
