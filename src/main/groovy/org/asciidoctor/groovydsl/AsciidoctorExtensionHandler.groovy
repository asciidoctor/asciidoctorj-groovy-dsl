/*
 * Copyright 2013-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.asciidoctor.groovydsl

import org.asciidoctor.groovydsl.extensions.DelegatingBlockMacroProcessor
import org.asciidoctor.groovydsl.extensions.DelegatingBlockProcessor
import org.asciidoctor.groovydsl.extensions.DelegatingIncludeProcessor
import org.asciidoctor.groovydsl.extensions.DelegatingPostprocessor
import org.asciidoctor.groovydsl.extensions.DelegatingPreprocessor
import org.asciidoctor.groovydsl.extensions.DelegatingInlineMacroProcessor
import org.asciidoctor.groovydsl.extensions.DelegatingTreeprocessor
import org.asciidoctor.Asciidoctor

class AsciidoctorExtensionHandler {

    private static final String OPTION_NAME = 'name'

    private static final String OPTION_FILTER = 'filter'

    private static final String OPTION_CONTEXTS = 'contexts'

    private final Asciidoctor asciidoctor
    
    AsciidoctorExtensionHandler(Asciidoctor asciidoctor) {
        this.asciidoctor = asciidoctor
    }
    
    void block(String blockName, Closure cl) {
        block([(OPTION_NAME): blockName], cl)
    }

    void block(Map options=[:], Closure cl) {
        if (!options.containsKey(OPTION_NAME)) {
            throw new IllegalArgumentException('Block must define a name!')
        }
        if (!options.containsKey(OPTION_CONTEXTS)) {
            //TODO: What are sensible defaults?
            options[OPTION_CONTEXTS] = [':open', ':paragraph']
        }
        asciidoctor.javaExtensionRegistry().block(new DelegatingBlockProcessor(options, cl))
    }

    void blockmacro(Map options, Closure cl) {
        asciidoctor.javaExtensionRegistry().blockMacro(new DelegatingBlockMacroProcessor(options[OPTION_NAME], options, cl))
    }

    void blockmacro(String name, Closure cl) {
        blockMacro([OPTION_NAME: name], cl)
    }


    void postprocessor(Map options=[:], Closure cl) {
        asciidoctor.javaExtensionRegistry().postprocessor(new DelegatingPostprocessor(options, cl))
    }

    void preprocessor(Map options=[:], Closure cl) {
        asciidoctor.javaExtensionRegistry().preprocessor(new DelegatingPreprocessor(options, cl))
    }

    void includeprocessor(Map options=[:], Closure cl) {
        Closure filter = options[OPTION_FILTER]
        Map optionsWithoutFilter = options - options.subMap([OPTION_FILTER])
        asciidoctor.javaExtensionRegistry().includeProcessor(new DelegatingIncludeProcessor(optionsWithoutFilter, filter, cl))
    }

    void inlinemacro(Map options, Closure cl) {
        asciidoctor.javaExtensionRegistry().inlineMacro(new DelegatingInlineMacroProcessor(options[OPTION_NAME], options, cl))
    }

    void inlinemacro(String macroName, Closure cl) {
        inlineMacro([OPTION_NAME: macroName], closure: cl)
    }

    void treeprocessor(Map options=[:], Closure cl) {
        asciidoctor.javaExtensionRegistry().treeprocessor(new DelegatingTreeprocessor(options, cl))
    }

}

