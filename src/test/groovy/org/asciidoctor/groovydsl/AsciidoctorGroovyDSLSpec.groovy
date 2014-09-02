/*
 * Copyright 2013-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.asciidoctor.groovydsl

import org.asciidoctor.SafeMode
import org.asciidoctor.Asciidoctor
import spock.lang.Specification
import org.asciidoctor.groovydsl.AsciidoctorExtensions
/**
 * Asciidoctor task inline extensions specification
 *
 */
class AsciidoctorGroovyDSLSpec extends Specification {

    private static final String TEST_DOC_BLOCK = '''Ignore this.

[BIG]
But this should be uppercase

[small]
And THIS should be lowercase\n\

.Gemfile
[source,ruby]
----
include::https://raw.github.com/asciidoctor/asciidoctor/master/Gemfile[]
----

.My Gist
gist::123456[]

See man:gittutorial[7] to get started.

blacklisted is a blacklisted word.
 
'''
    
    File testRootDir

    def setup() {
        testRootDir = new File('.')
    }


    def 'Should apply BlockProcessor from Script as String'() {
        given:

        AsciidoctorExtensions.extensions '''
            block(name: 'BIG', contexts: [':paragraph']) {
                parent, reader, attributes ->
                def upperLines = reader.readLines()
                .collect {it.toUpperCase()}
                .inject('') {a, b -> a + '\\n' + b}

                createBlock(parent, 'paragraph', [upperLines], attributes, [:])
            }
            block('small') {
                parent, reader, attributes ->
                def lowerLines = reader.readLines()
                .collect {it.toLowerCase()}
                .inject('') {a, b -> a + '\\n' + b}
                
                createBlock(parent, 'paragraph', [lowerLines], attributes, [:])
            }
        
'''
        when:
        String rendered = Asciidoctor.Factory.create().render(TEST_DOC_BLOCK, [:])

        then:
        rendered.contains('BUT THIS SHOULD BE UPPERCASE')
        rendered.contains('and this should be lowercase')
        rendered.contains('Ignore this.')
    }
    
    
    def 'Should apply BlockProcessor from Closure'() {
        given:

        AsciidoctorExtensions.extensions {
            block(name: 'BIG', contexts: [':paragraph']) {
                parent, reader, attributes ->
                def upperLines = reader.readLines()
                .collect {it.toUpperCase()}
                .inject('') {a, b -> a + '\n' + b}

                createBlock(parent, 'paragraph', [upperLines], attributes, [:])
            }
            block('small') {
                parent, reader, attributes ->
                def lowerLines = reader.readLines()
                .collect {it.toLowerCase()}
                .inject('') {a, b -> a + '\n' + b}
                
                createBlock(parent, 'paragraph', [lowerLines], attributes, [:])
            }
        }

        when:
        String rendered = Asciidoctor.Factory.create().render(TEST_DOC_BLOCK, [:])

        then:
        rendered.contains('BUT THIS SHOULD BE UPPERCASE')
        rendered.contains('and this should be lowercase')
        rendered.contains('Ignore this.')
    }
    
    def 'Should apply BlockProcessor from Extension file'() {
        given:

        AsciidoctorExtensions.extensions(new File('src/test/resources/testblockextensions.groovy'))
        when:
        String rendered = Asciidoctor.Factory.create().render(TEST_DOC_BLOCK, [:])

        then:
        rendered.contains('BUT THIS SHOULD BE UPPERCASE')
        rendered.contains('and this should be lowercase')
        rendered.contains('Ignore this.')
    }

    def 'Should apply Postprocessor from Extension file'() {
        given:
        AsciidoctorExtensions.extensions(new File('src/test/resources/testpostprocessorextension.groovy'))

        when:
        String rendered = Asciidoctor.Factory.create().render(TEST_DOC_BLOCK, [:])

        then:
        rendered.contains('Copyright Acme, Inc.')
    }

    def 'Should apply Preprocessor from Extension file'() {
        given:
        AsciidoctorExtensions.extensions(new File('src/test/resources/testpreprocessorextension.groovy'))

        when:
        String rendered = Asciidoctor.Factory.create().render(TEST_DOC_BLOCK, [:])

        then:
        !rendered.contains('Ignore this.')
    }

    def 'Should apply Includeprocessor from Extension file'() {
        given:
        AsciidoctorExtensions.extensions(new File('src/test/resources/testincludeprocessorextension.groovy'))

        when:
        String rendered = Asciidoctor.Factory.create().render(TEST_DOC_BLOCK, [:])

        then:
        rendered.contains('The content of the URL')
        println rendered
    }

    def 'Should apply BlockMacroProcessor from Extension file'() {
        given:
        AsciidoctorExtensions.extensions(new File('src/test/resources/testblockmacroextension.groovy'))

        when:
        String rendered = Asciidoctor.Factory.create().render(TEST_DOC_BLOCK, [:])

        then:
        rendered.contains("https://gist.github.com/123456.js")
    }

    def 'Should apply InlineMacroProcessor from Extension file'() {
        given:
        AsciidoctorExtensions.extensions(new File('src/test/resources/testinlinemacroprocessorextension.groovy'))

        when:
        String rendered = Asciidoctor.Factory.create().render(TEST_DOC_BLOCK, [:])

        then:
        rendered.contains('<a href="gittutorial.html">gittutorial</a>')
    }

    def 'Should apply Treeprocessor from Extension file'() {
        given:
        AsciidoctorExtensions.extensions(new File('src/test/resources/testtreeprocessorextension.groovy'))

        when:
        String rendered = Asciidoctor.Factory.create().render('''
$ echo "Hello, World!"

$ gem install asciidoctor
''', [:])

        then:
        rendered.contains('<span class="command">gem install asciidoctor</span>')
    }

}
