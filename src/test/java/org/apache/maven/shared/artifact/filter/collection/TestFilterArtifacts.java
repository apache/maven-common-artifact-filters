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
package org.apache.maven.shared.artifact.filter.collection;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.testing.ArtifactStubFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * @author <a href="mailto:brianf@apache.org">Brian Fox</a>
 */
class TestFilterArtifacts {
    @Test
    void checkNullFilters() throws Exception {

        // TODO: convert these old tests to use the abstract test case for dep
        // plugin
        File outputFolder = new File("target/filters/");

        FileUtils.deleteDirectory(outputFolder);

        ArtifactStubFactory fact = new ArtifactStubFactory(outputFolder, false);

        Set<Artifact> artifacts = fact.getReleaseAndSnapshotArtifacts();
        FilterArtifacts fa = new FilterArtifacts();

        fa.filter(artifacts);

        // make sure null filters don't hurt anything.
        fa.addFilter(null);

        fa.filter(artifacts);
        assertEquals(0, fa.getFilters().size());

        ArrayList<ArtifactsFilter> filters = new ArrayList<>();
        filters.add(null);
        filters.add(null);
        fa.setFilters(filters);

        assertEquals(2, fa.getFilters().size());

        fa.filter(artifacts);
    }

    @Test
    void checkArtifactFilter() {
        Set<Artifact> a = new HashSet<>();
        FilterArtifacts fa = new FilterArtifacts();
        ArtifactsFilter scope = new ScopeFilter("compile", "system");
        ArtifactsFilter type = new TypeFilter("jar", "war");
        ArtifactsFilter trans = new ProjectTransitivityFilter(a, true);

        assertEquals(0, fa.getFilters().size());
        fa.addFilter(scope);
        assertEquals(1, fa.getFilters().size());
        fa.addFilter(type);
        assertEquals(2, fa.getFilters().size());
        assertInstanceOf(ScopeFilter.class, fa.getFilters().get(0));
        assertInstanceOf(TypeFilter.class, fa.getFilters().get(1));
        fa.addFilter(1, trans);
        assertEquals(3, fa.getFilters().size());
        assertInstanceOf(ScopeFilter.class, fa.getFilters().get(0));
        assertInstanceOf(ProjectTransitivityFilter.class, fa.getFilters().get(1));
        assertInstanceOf(TypeFilter.class, fa.getFilters().get(2));

        ArrayList<ArtifactsFilter> list = new ArrayList<>(fa.getFilters());

        fa.clearFilters();
        assertEquals(0, fa.getFilters().size());

        fa.setFilters(list);
        assertEquals(3, fa.getFilters().size());
        assertInstanceOf(ScopeFilter.class, fa.getFilters().get(0));
        assertInstanceOf(ProjectTransitivityFilter.class, fa.getFilters().get(1));
        assertInstanceOf(TypeFilter.class, fa.getFilters().get(2));
    }

    @Test
    void checkArtifactFilterWithClassifier() throws Exception {
        File outputFolder = new File("target/filters/");
        FileUtils.deleteDirectory(outputFolder);
        ArtifactStubFactory fact = new ArtifactStubFactory(outputFolder, false);

        Set<Artifact> artifacts = fact.getClassifiedArtifacts();
        FilterArtifacts fa = new FilterArtifacts();
        fa.addFilter(new ClassifierFilter("", "four"));
        Set<Artifact> results = fa.filter(artifacts);
        assertEquals(3, results.size());
        fa.addFilter(new ClassifierFilter("two,three", ""));
        results = fa.filter(artifacts);
        assertEquals(2, results.size());
    }
}
