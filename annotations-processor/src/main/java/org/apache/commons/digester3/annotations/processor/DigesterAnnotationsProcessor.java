package org.apache.commons.digester3.annotations.processor;

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

import static com.sun.codemodel.JMod.FINAL;
import static com.sun.codemodel.JMod.PROTECTED;
import static com.sun.codemodel.JMod.PUBLIC;
import static com.sun.codemodel.JType.parse;
import static java.lang.String.format;
import static java.util.Arrays.asList;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import org.apache.commons.digester3.annotations.rules.BeanPropertySetter;
import org.apache.commons.digester3.annotations.rules.CallMethod;
import org.apache.commons.digester3.annotations.rules.CallParam;
import org.apache.commons.digester3.annotations.rules.CreationRule;
import org.apache.commons.digester3.annotations.rules.FactoryCreate;
import org.apache.commons.digester3.annotations.rules.ObjectCreate;
import org.apache.commons.digester3.annotations.rules.PathCallParam;
import org.apache.commons.digester3.annotations.rules.SetNext;
import org.apache.commons.digester3.annotations.rules.SetProperty;
import org.apache.commons.digester3.annotations.rules.SetRoot;
import org.apache.commons.digester3.annotations.rules.SetTop;
import org.apache.commons.digester3.binder.AbstractRulesModule;
import org.kohsuke.MetaInfServices;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JPackage;

/**
 * @since 3.3
 */
@MetaInfServices( Processor.class )
public class DigesterAnnotationsProcessor
    extends AbstractProcessor
{

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> getSupportedAnnotationTypes()
    {
        return new HashSet<String>( asList( BeanPropertySetter.class.getName(),
                                            CallMethod.class.getName(),
                                            CallParam.class.getName(),
                                            CreationRule.class.getName(),
                                            FactoryCreate.class.getName(),
                                            ObjectCreate.class.getName(),
                                            PathCallParam.class.getName(),
                                            SetNext.class.getName(),
                                            SetProperty.class.getName(),
                                            SetRoot.class.getName(),
                                            SetTop.class.getName() ) );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean process( Set<? extends TypeElement> annotations, RoundEnvironment environment )
    {
        // processingEnv is a predefined member in AbstractProcessor class
        // Messager allows the processor to output messages to the environment
        final FormattingMessager messager = new FormattingMessager( processingEnv.getMessager() );
        final DigesterElementVisitor elementVisitor = new DigesterElementVisitor( messager );

        // TODO get these values from -A parameters
        String packageName = getClass().getPackage().getName();
        String className = "GeneratedRulesModule";

        final JCodeModel codeModel = new JCodeModel();

        final JPackage modulePackage = codeModel._package( packageName );

        boolean success = false;

        try
        {
            final JDefinedClass rulesModuleClass = modulePackage._class( FINAL | PUBLIC, className );
            rulesModuleClass.javadoc().add( format( "Generated by Apache Commons Digester at %s", new Date() ) );
            rulesModuleClass._extends( AbstractRulesModule.class );

            final JMethod configureMethod = rulesModuleClass.method( PROTECTED,
                                                                     parse( codeModel, "void" ),
                                                                     "configure" );
            configureMethod.javadoc().add( "{@inheritDoc}" );
            configureMethod.annotate( Override.class );
            final JBlock configureMethodBody = configureMethod.body();

            for ( Element element : environment.getRootElements() )
            {
                element.accept( elementVisitor, null );
            }

            // Loop through the annotations that we are going to process
            /* for ( TypeElement annotation : annotations )
            {
                // Get the members
                for ( Element element : environment.getElementsAnnotatedWith( annotation ) )
                {
                    messager.error( "Processing @%s %s", annotation, element );
                }
            } */

            codeModel.build( new FilerCodeWriter( processingEnv.getFiler() ) );

            success = true;
        }
        catch ( JClassAlreadyExistsException e )
        {
            messager.error( "Class %s.%s has been already defined", packageName, className );
        }
        catch ( IOException e )
        {
            messager.error( "Impossible to generate class %s.%s: %s", packageName, className, e.getMessage() );
        }

        return success;
    }

}
