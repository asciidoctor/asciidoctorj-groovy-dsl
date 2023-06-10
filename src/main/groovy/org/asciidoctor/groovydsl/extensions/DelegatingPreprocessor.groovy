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

import org.asciidoctor.ast.Document
import org.asciidoctor.extension.Preprocessor
import org.asciidoctor.extension.PreprocessorReader
import org.asciidoctor.extension.Reader

class DelegatingPreprocessor extends Preprocessor {

    private final Closure cl

    DelegatingPreprocessor(Map options, @DelegatesTo(Preprocessor) Closure cl) {
        super(options)
        this.cl = cl
        cl.delegate = this
    }

    Reader process(Document document, PreprocessorReader reader) {
        cl.call(document, reader)
    }
	
}

