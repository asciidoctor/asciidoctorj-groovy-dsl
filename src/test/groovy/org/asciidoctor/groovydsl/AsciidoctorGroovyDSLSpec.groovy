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

import org.asciidoctor.Asciidoctor
import org.asciidoctor.SafeMode
import spock.lang.Specification

import static org.asciidoctor.OptionsBuilder.options

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

capitalize::tHIS_sets_the_fIRST_letter_in_cApItAlS_and_REMOVES_underscores[]

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


    def 'Should clear registry on exception when registering from a Closure'() {
        given:
        AsciidoctorExtensions.extensions {
            throw new Exception('This error is on purpose')
        }

        when:
        Asciidoctor.Factory.create().render(TEST_DOC_BLOCK, [:])

        then:
        def e = thrown(AsciidoctorExtentionException)
        e.message.contains('Error registering extension from class')
    }

    def 'Should clear registry on exception when registering from a String'() {

        given:
        AsciidoctorExtensions.extensions 'throw new Exception(\'This error is on purpose\')'

        when:
        Asciidoctor.Factory.create().render(TEST_DOC_BLOCK, [:])

        then:
        def e = thrown(AsciidoctorExtentionException)
        e.message.contains('Error registering extension from string')
    }

    def 'Should clear registry on exception when registering from a File'() {
        given:
        AsciidoctorExtensions.extensions(new File('src/test/resources/error.groovy'))

        when:
        Asciidoctor.Factory.create().render(TEST_DOC_BLOCK, [:])

        then:
        def e = thrown(AsciidoctorExtentionException)
        e.message.contains('Error registering extension from file')
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
                            .collect { it.toUpperCase() }
                            .inject('') { a, b -> a + '\n' + b }

                    createBlock(parent, 'paragraph', [upperLines], attributes, [:])
            }
            block('small') {
                parent, reader, attributes ->
                    def lowerLines = reader.readLines()
                            .collect { it.toLowerCase() }
                            .inject('') { a, b -> a + '\n' + b }

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

    def 'Should apply BlockMacroProcessor from Closure'() {
        given:
        AsciidoctorExtensions.extensions {
            block_macro ("gist") {
                parent, target, attributes ->
                    String content = """<div class="content">
<script src="https://gist.github.com/${target}.js"></script>
</div>"""
                    createBlock(parent, "pass", [content], attributes, config);
            }
        }

        when:
        String rendered = Asciidoctor.Factory.create().render(TEST_DOC_BLOCK, [:])

        then:
        rendered.contains("https://gist.github.com/123456.js")
    }

    def 'Should apply BlockMacroProcessor from Extension file'() {
        given:
        AsciidoctorExtensions.extensions(new File('src/test/resources/testblockmacroextension.groovy'))

        when:
        String rendered = Asciidoctor.Factory.create().render(TEST_DOC_BLOCK, [:])

        then:
        rendered.contains("https://gist.github.com/123456.js")
    }

    def 'Should apply InlineMacroProcessor from Closure'() {
        given:

        AsciidoctorExtensions.extensions {
            inline_macro('man') {
                parent, target, attributes ->
                    def options = ["type": ":link", "target": target + ".html"]
                    createInline(parent, "anchor", target, attributes, options).convert()
            }
        }

        when:
        String rendered = Asciidoctor.Factory.create().render(TEST_DOC_BLOCK, [:])

        then:
        rendered.contains('<a href="gittutorial.html">gittutorial</a>')
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

    def 'Should apply DocinfoProcessor from Closure'() {
        given:
        String metatag = '<meta name="hello" content="world">'
        AsciidoctorExtensions.extensions {
            docinfo_processor {
                document -> metatag
            }
        }

        when:
        String rendered = Asciidoctor.Factory.create().render(
                '''
= Hello

World''',
                options().headerFooter(true).safe(SafeMode.SERVER).toFile(false).get());

        then:
        // (?ms) Multiline regexp with dotall (= '.' matches newline as well)
        rendered ==~ /(?ms).*<head>.*<meta name="hello" content="world">.*<\/head>.*/
    }

    /*
    * Tests deprecated method, to delete at some point.
    */
    def 'Should apply BlockMacroProcessor from Closure with string parameter'() {
        given:

        AsciidoctorExtensions.extensions {
            blockmacro('capitalize') {
                parent, target, attributes ->
                    def capitalLines = target.toLowerCase()
                            .tokenize('_')
                            .collect { it.capitalize() }
                            .join(' ')
                    createBlock(parent, 'pass', [capitalLines], attributes, config)
            }
        }

        when:
        String rendered = Asciidoctor.Factory.create().render(TEST_DOC_BLOCK, [:])

        then:
        rendered.contains('This Sets The First Letter In Capitals And Removes Underscores')
        rendered.contains('Ignore this.')
    }

    /*
    * Tests deprecated method, to delete at some point.
    */
    def 'Should apply BlockMacroProcessor from Closure with named parameter'() {
        given:

        AsciidoctorExtensions.extensions {
            blockmacro(name: 'capitalize') {
                parent, target, attributes ->
                    def capitalLines = target.toLowerCase()
                            .tokenize('_')
                            .collect { it.capitalize() }
                            .join(' ')
                    createBlock(parent, 'pass', [capitalLines], attributes, config)
            }
        }

        when:
        String rendered = Asciidoctor.Factory.create().render(TEST_DOC_BLOCK, [:])

        then:
        rendered.contains('This Sets The First Letter In Capitals And Removes Underscores')
        rendered.contains('Ignore this.')
    }

    /*
    * Tests deprecated method, to delete at some point.
    */
    def 'Should apply InlineMacroProcessor from Closure with named parameter'() {
        given:

        AsciidoctorExtensions.extensions {
            inlinemacro(name: 'man') {
                parent, target, attributes ->
                    def options = ["type": ":link", "target": target + ".html"]
                    createInline(parent, "anchor", target, attributes, options).convert()
            }
        }

        when:
        String rendered = Asciidoctor.Factory.create().render(TEST_DOC_BLOCK, [:])

        then:
        rendered.contains('<a href="gittutorial.html">gittutorial</a>')
    }

    /*
    * Tests deprecated method, to delete at some point.
    */
    def 'Should apply InlineMacroProcessor from Closure with string parameter'() {
        given:

        AsciidoctorExtensions.extensions {
            inlinemacro('man') {
                parent, target, attributes ->
                    def options = ["type": ":link", "target": target + ".html"]
                    createInline(parent, "anchor", target, attributes, options).convert()
            }
        }

        when:
        String rendered = Asciidoctor.Factory.create().render(TEST_DOC_BLOCK, [:])

        then:
        rendered.contains('<a href="gittutorial.html">gittutorial</a>')
    }

    /*
    * Tests deprecated method, to delete at some point.
    */
    def 'Should apply Includeprocessor from Closure'() {
        given:
        AsciidoctorExtensions.extensions {
            includeprocessor (filter: {it.startsWith("http")}) {
                document, reader, target, attributes ->
                    reader.push_include("The content of the URL", target, target, 1, attributes);
            }

        }

        when:
        String rendered = Asciidoctor.Factory.create().render(TEST_DOC_BLOCK, [:])

        then:
        rendered.contains('The content of the URL')
        println rendered
    }

}
