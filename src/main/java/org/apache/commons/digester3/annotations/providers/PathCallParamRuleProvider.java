/* $Id$
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.digester3.annotations.providers;

import org.apache.commons.digester3.PathCallParamRule;
import org.apache.commons.digester3.annotations.AnnotationRuleProvider;
import org.apache.commons.digester3.annotations.reflect.MethodArgument;
import org.apache.commons.digester3.annotations.rules.PathCallParam;

/**
 * Provides instances of {@link PathCallParamRule}.
 * 
 * @since 2.1
 */
public final class PathCallParamRuleProvider
    implements AnnotationRuleProvider<PathCallParam, MethodArgument, PathCallParamRule>
{

    private int index;

    /**
     * {@inheritDoc}
     */
    public void init( PathCallParam annotation, MethodArgument element )
    {
        this.index = element.getIndex();
    }

    /**
     * {@inheritDoc}
     */
    public PathCallParamRule get()
    {
        return new PathCallParamRule( this.index );
    }

}
