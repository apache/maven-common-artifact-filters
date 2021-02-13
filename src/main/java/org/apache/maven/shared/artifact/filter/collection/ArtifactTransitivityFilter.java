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

import org.apache.maven.RepositoryUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.DependencyResolutionResult;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingResult;
import org.eclipse.aether.graph.Dependency;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This filter will exclude everything that is not a dependency of the selected artifact.
 *
 * @author <a href="mailto:brianf@apache.org">Brian Fox</a>
 */
public class ArtifactTransitivityFilter
    extends AbstractArtifactsFilter
{
    /**
     * List of dependencyConflictIds of transitiveArtifacts
     */
    private Set<String> transitiveArtifacts;

    /**
     * <p>
     * Use {@link org.apache.maven.execution.MavenSession#getProjectBuildingRequest()} to get the buildingRequest.
     * The projectBuilder should be resolved with CDI.
     * </p>
     * <pre>
     *   // For Mojo
     *   &#64;Component
     *   private ProjectBuilder projectBuilder;
     *
     *   // For Components
     *   &#64;Requirement // or &#64;Inject
     *   private ProjectBuilder projectBuilder;
     * </pre>
     *
     * @param artifact        the artifact to resolve the dependencies from
     * @param buildingRequest the buildingRequest
     * @param projectBuilder  the projectBuilder
     * @throws ProjectBuildingException if the project descriptor could not be successfully built
     */
    public ArtifactTransitivityFilter( Artifact artifact, ProjectBuildingRequest buildingRequest,
                                       ProjectBuilder projectBuilder )
        throws ProjectBuildingException
    {
        ProjectBuildingRequest request = new DefaultProjectBuildingRequest( buildingRequest );

        request.setResolveDependencies( true );

        ProjectBuildingResult buildingResult = projectBuilder.build( artifact, request );

        DependencyResolutionResult resolutionResult = buildingResult.getDependencyResolutionResult();
        if ( resolutionResult != null )
        {
            for ( Dependency dependency : resolutionResult.getDependencies() )
            {
                Artifact mavenArtifact = RepositoryUtils.toArtifact( dependency.getArtifact() );
                transitiveArtifacts.add( mavenArtifact.getDependencyConflictId() );
            }
        }
    }

    /** {@inheritDoc} */
    public Set<Artifact> filter( Set<Artifact> artifacts )
    {
        Set<Artifact> result = new LinkedHashSet<>();
        for ( Artifact artifact : artifacts )
        {
            if ( artifactIsATransitiveDependency( artifact ) )
            {
                result.add( artifact );
            }
        }
        return result;
    }

    /**
     * Compares the artifact to the list of dependencies to see if it is directly included by this project
     *
     * @param artifact representing the item to compare.
     * @return true if artifact is a transitive dependency
     */
    public boolean artifactIsATransitiveDependency( Artifact artifact )
    {
        return transitiveArtifacts.contains( artifact.getDependencyConflictId() );
    }
}
