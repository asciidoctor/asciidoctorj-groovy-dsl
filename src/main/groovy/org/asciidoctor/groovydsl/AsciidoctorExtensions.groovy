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

import groovy.transform.CompileStatic
import org.asciidoctor.Asciidoctor
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer

import java.nio.file.Files
import java.nio.file.Path

/**
 * An instance of this class holds all extension closure and scripts.
 * It evaluates the blocks and scripts and forwards the extracted extensions
 * to the GroovyExtensionRegistry which is service implementation
 * of org.asciidoctor.extension.spi.wExtensionRegistry
 */
@CompileStatic
class AsciidoctorExtensions {

    /** Add an extension from a closure
     *
     * @param cl Closure containing an extension
     */
    void addExtension(@DelegatesTo(AsciidoctorExtensionHandler) Closure cl) {
        registeredExtensions.add(cl)
    }

    /** Add an extension via a string.
     *
     * @param groovyScript String containing extension.
     */
    void addExtension(final String groovyScript) {
        registeredExtensions.add(groovyScript)
    }

    /** Add an extension via a file.
     *
     * @param groovyScript File containing extension
     */
    void addExtension(final File groovyScript) {
        registeredExtensions.add(groovyScript)
    }

    /** Add an extension via a path instance
     *
     * @param groovyScript Path pointing to an extension
     */
    void addExtension(final Path groovyScript) {
        registeredExtensions.add(groovyScript)
    }

    /** Remove all extensions.
     *
     */
    void clearExtensions() {
        registeredExtensions.clear()
    }

    /** Register all extension with an instance of Asciidoctor.
     *
     * @param asciidoctor Asciidoctor instance awaiting extensions.
     * @throw AsciidoctorExtensionException
     */
    @SuppressWarnings('UnnecessarySetter')
    void registerExtensionsWith(Asciidoctor asciidoctor) {
        AsciidoctorExtensionHandler extensionHandler = new AsciidoctorExtensionHandler(asciidoctor)
        for (def it : registeredExtensions) {
            switch (it) {
                case Closure:
                    try {
                        ((Closure) it).delegate = extensionHandler
                        ((Closure) it).call()
                    } catch (e) {
                        throw new AsciidoctorExtensionException("Error registering extension from class in ${it.class.name}", e)
                    }
                    break
                case String:
                    GroovyShell shell = makeGroovyShell()
                    DelegatingScript script = (DelegatingScript) shell.parse((String) it)
                    script.setDelegate(extensionHandler)
                    try {
                        script.run()
                    } catch (e) {
                        registeredExtensions.clear()
                        throw new AsciidoctorExtensionException('Error registering extension from string', e)
                    }
                    break
                case File:
                    File file = (File) it
                    GroovyShell shell = makeGroovyShell()
                    file.withReader { reader ->
                        DelegatingScript script = (DelegatingScript) shell.parse(reader, file.name)
                        script.setDelegate(extensionHandler)
                        try {
                            script.run()
                        } catch (e) {
                            throw new AsciidoctorExtensionException("Error registering extension from file ${file.name}", e)
                        }
                    }
                    break
                case Path:
                    Path path = (Path) it
                    GroovyShell shell = makeGroovyShell()
                    Files.newBufferedReader(path).withReader { reader ->
                        DelegatingScript script = (DelegatingScript) shell.parse(reader, path.toString())
                        script.setDelegate(extensionHandler)
                        try {
                            script.run()
                        } catch (e) {
                            throw new AsciidoctorExtensionException("Error registering extension from file ${path}", e)
                        }
                    }
                    break
            }
        }
    }

    /** Adds an extension to the AsciidoctorExtension singleton instance.
     *
     * @param cl Closure containing an instance
     */
    static void extensions(@DelegatesTo(AsciidoctorExtensionHandler) Closure cl) {
        INSTANCE.addExtension(cl)
    }

    /** Adds an extension to the AsciidoctorExtension singleton instance.
     *
     * @param s String containing an instance
     */
    static void extensions(String s) {
        INSTANCE.addExtension(s)
    }

    /** Adds an extension to the AsciidoctorExtension singleton instance.
     *
     * @param f File containing an instance
     */
    static void extensions(File f) {
        INSTANCE.addExtension(f)
    }

    /** Attempt to register all exteniosn with ASciidoctor instance.
     *
     * This method has the side-effect of removing all extensions as well.
     *
     * @param asciidoctor
     */
    static void registerTo(Asciidoctor asciidoctor) {
        try {
            INSTANCE.registerExtensionsWith(asciidoctor)
        } finally {
            INSTANCE.clearExtensions()
        }
    }

    private static GroovyShell makeGroovyShell() {
        def config = new CompilerConfiguration()

        config.scriptBaseClass = DelegatingScript.name

        ImportCustomizer importCustomizer = new ImportCustomizer()
        importCustomizer.addStarImports(
            'org.asciidoctor',
            'org.asciidoctor.ast',
            'org.asciidoctor.extension')

        config.addCompilationCustomizers(
            importCustomizer
        )

        new GroovyShell(new Binding(), config)
    }

    private final List<Object> registeredExtensions = []
    private static final AsciidoctorExtensions INSTANCE = new AsciidoctorExtensions()
}
