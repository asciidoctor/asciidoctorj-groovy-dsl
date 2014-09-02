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
package org.asciidoctor.groovydsl.extensions

import org.asciidoctor.ast.DocumentRuby
import org.asciidoctor.extension.IncludeProcessor
import org.asciidoctor.extension.PreprocessorReader

class DelegatingIncludeProcessor extends IncludeProcessor {

    private final Closure filter
    private final Closure cl

    DelegatingIncludeProcessor(Map options, Closure filter, Closure cl) {
        super(options)
        this.filter = filter
        this.cl = cl
        filter.delegate = this
        cl.delegate = this
        
    }
    boolean handles(String target) {
        filter.call(target)
    }
    void process(DocumentRuby document, PreprocessorReader reader, String target, Map<String, Object> attributes) {
        cl.call(document, reader, target, attributes)
    }

}

