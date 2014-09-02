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

import org.asciidoctor.Asciidoctor
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer

/**
 * An instance of this class holds all extension closure and scripts.
 * It evaluates the blocks and scripts and forwards the extracted extensions
 * to the GroovyExtensionRegistry which is service implementation
 * of org.asciidoctor.extension.spi.wExtensionRegistry
 */
class AsciidoctorExtensions {

    // Container closures and scripts
    // These get evaluated when the ExtensionRegistry
    // is asked to register its extensions
    static final List<Object> REGISTERED_EXTENSIONS = []

    static void extensions(Closure cl) {
        REGISTERED_EXTENSIONS << cl
    }

    static void extensions(String s) {
        REGISTERED_EXTENSIONS << s
    }

    static void extensions(File f) {
        REGISTERED_EXTENSIONS << f
    }

    static void registerTo(Asciidoctor asciidoctor) {
        AsciidoctorExtensionHandler extensionHandler = new AsciidoctorExtensionHandler(asciidoctor)
        REGISTERED_EXTENSIONS.each {
            switch (it) {
                case Closure:
                    it.delegate = extensionHandler
                    it.call()
                    break
                case String:
                    def shell = makeGroovyShell(extensionHandler)
                    shell.evaluate(it)
                    break
                case File:
                    def shell = makeGroovyShell(extensionHandler)
                    FileReader reader = new FileReader(it)
                    try {
                        shell.evaluate(reader, it.name)
                    } finally {
                        reader.close()
                    }
                    break
            }
        }
        REGISTERED_EXTENSIONS.clear()
    }

    private static GroovyShell makeGroovyShell(AsciidoctorExtensionHandler extensionHandler) {
        def config = new CompilerConfiguration()
        def binding = new Binding([extensionHandler: extensionHandler])

        config.scriptBaseClass = AsciidoctorDSLBaseScriptClass.name

        ImportCustomizer importCustomizer = new ImportCustomizer()
        importCustomizer.addStarImports(
                'org.asciidoctor',
                'org.asciidoctor.ast',
                'org.asciidoctor.extension')

        config.addCompilationCustomizers(
                importCustomizer
                )
        new GroovyShell(binding, config)

    }
}