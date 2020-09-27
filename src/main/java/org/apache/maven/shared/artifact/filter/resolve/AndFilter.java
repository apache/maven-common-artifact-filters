package org.apache.maven.shared.artifact.filter.resolve;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.util.Collection;
import java.util.Collections;

/**
 * A filter that combines zero or more other filters using a logical {@code AND}.
 * 
 * @author Robert Scholte
 * @since 3.0
 * 
 * @see org.eclipse.aether.util.filter.AndDependencyFilter
 */
public class AndFilter
    implements TransformableFilter
{
    private final Collection<TransformableFilter> filters;

    /**
     * The default constructor specifying a collection of filters which all must be matched. 
     * 
     * @param filters the filters, may not be {@code null}
     */
    public AndFilter( Collection<TransformableFilter> filters )
    {
        this.filters = Collections.unmodifiableCollection( filters );
    }

    /**
     * Get the filters
     * 
     * @return the filters, never {@code null} 
     */
    public Collection<TransformableFilter> getFilters()
    {
        return filters;
    }

    /**
     * Transform this filter to a tool specific implementation
     * 
     * @param transformer the transformer, may not be {@code null}
     */
    @Override
    public <T> T transform( FilterTransformer<T> transformer )
    {
        return transformer.transform( this );
    }

}
