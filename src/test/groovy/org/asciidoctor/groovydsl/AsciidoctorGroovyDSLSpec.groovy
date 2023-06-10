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
import org.asciidoctor.Options
import org.asciidoctor.SafeMode
import org.jsoup.Jsoup
import spock.lang.Specification

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
        Asciidoctor.Factory.create().convert(TEST_DOC_BLOCK, Options.builder().build())

        then:
        def e = thrown(ServiceConfigurationError)
        e.message.contains('Provider org.asciidoctor.jruby.internal.JRubyAsciidoctor could not be instantiated')
        e.cause.message.contains('Error registering extension from class')
        e.cause.cause.message.contains('This error is on purpose')
    }

    def 'Should clear registry on exception when registering from a String'() {

        given:
        AsciidoctorExtensions.extensions 'throw new Exception(\'This error is on purpose\')'

        when:
        Asciidoctor.Factory.create().convert(TEST_DOC_BLOCK, Options.builder().build())

        then:
        def e = thrown(ServiceConfigurationError)
        e.message.contains('Provider org.asciidoctor.jruby.internal.JRubyAsciidoctor could not be instantiated')
        e.cause.message.contains('Error registering extension from string')
        e.cause.cause.message.contains('This error is on purpose')
    }

    def 'Should clear registry on exception when registering from a File'() {
        given:
        AsciidoctorExtensions.extensions(new File('src/test/resources/error.groovy'))

        when:
        Asciidoctor.Factory.create().convert(TEST_DOC_BLOCK, Options.builder().build())

        then:
        def e = thrown(ServiceConfigurationError)
        e.message.contains('Provider org.asciidoctor.jruby.internal.JRubyAsciidoctor could not be instantiated')
        e.cause.message.contains('Error registering extension from file')
        e.cause.cause.message.contains('This error is on purpose')
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
        String rendered = Asciidoctor.Factory.create().convert(TEST_DOC_BLOCK, Options.builder().build())

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
        String rendered = Asciidoctor.Factory.create().convert(TEST_DOC_BLOCK, Options.builder().build())

        then:
        rendered.contains('BUT THIS SHOULD BE UPPERCASE')
        rendered.contains('and this should be lowercase')
        rendered.contains('Ignore this.')
    }

    def 'Should apply BlockProcessor from Extension file'() {
        given:
        AsciidoctorExtensions.extensions(new File('src/test/resources/testblockextensions.groovy'))

        when:
        String rendered = Asciidoctor.Factory.create().convert(TEST_DOC_BLOCK, Options.builder().build())

        then:
        rendered.contains('BUT THIS SHOULD BE UPPERCASE')
        rendered.contains('and this should be lowercase')
        rendered.contains('Ignore this.')
    }

    def 'Should apply Postprocessor from String'() {
        given:
        String extension = new File('src/test/resources/testpostprocessorextension.groovy').text
        AsciidoctorExtensions.extensions(extension)

        when:
        String rendered = Asciidoctor.Factory.create().convert(TEST_DOC_BLOCK, Options.builder().build())

        then:
        rendered.contains('Copyright Acme, Inc.')
    }

    def 'Should apply Postprocessor from Closure'() {
        given:
        String copyright = "Copyright Acme, Inc."
        AsciidoctorExtensions.extensions {
            postprocessor {
                document, output ->
                    if (document.isBasebackend("html")) {
                        org.jsoup.nodes.Document doc = Jsoup.parse(output, "UTF-8")

                        def contentElement = doc.getElementsByTag("body")
                        contentElement.append(copyright)
                        doc.html()
                    } else {
                        throw new IllegalArgumentException("Expected html!")
                    }
            }
        }

        when:
        String rendered = Asciidoctor.Factory.create().convert(TEST_DOC_BLOCK, Options.builder().build())

        then:
        rendered.contains('Copyright Acme, Inc.')
    }

    def 'Should apply Postprocessor from Extension file'() {
        given:
        AsciidoctorExtensions.extensions(new File('src/test/resources/testpostprocessorextension.groovy'))

        when:
        String rendered = Asciidoctor.Factory.create().convert(TEST_DOC_BLOCK, Options.builder().build())

        then:
        rendered.contains('Copyright Acme, Inc.')
    }

    def 'Should apply Preprocessor from String'() {
        given:
        String extension = new File('src/test/resources/testpreprocessorextension.groovy').text
        AsciidoctorExtensions.extensions(extension)

        when:
        String rendered = Asciidoctor.Factory.create().convert(TEST_DOC_BLOCK, Options.builder().build())

        then:
        !rendered.contains('Ignore this.')
    }

    def 'Should apply Preprocessor from Closure'() {
        given:
        AsciidoctorExtensions.extensions {
            preprocessor {
                document, reader ->
                    newReader(reader.lines.tail())
            }
        }

        when:
        String rendered = Asciidoctor.Factory.create().convert(TEST_DOC_BLOCK, Options.builder().build())

        then:
        !rendered.contains('Ignore this.')
    }

    def 'Should apply Preprocessor from Extension file'() {
        given:
        AsciidoctorExtensions.extensions(new File('src/test/resources/testpreprocessorextension.groovy'))

        when:
        String rendered = Asciidoctor.Factory.create().convert(TEST_DOC_BLOCK, Options.builder().build())

        then:
        !rendered.contains('Ignore this.')
    }

    def 'Should apply Includeprocessor from String'() {
        given:
        String extension = new File('src/test/resources/testincludeprocessorextension.groovy').text
        AsciidoctorExtensions.extensions(extension)

        when:
        String rendered = Asciidoctor.Factory.create().convert(TEST_DOC_BLOCK, Options.builder().build())

        then:
        rendered.contains('The content of the URL')
    }

    def 'Should apply Includeprocessor from Closure'() {
        given:
        AsciidoctorExtensions.extensions {
            include_processor(filter: { it.startsWith("http") }) {
                document, reader, target, attributes ->
                    reader.pushInclude("The content of the URL", target, target, 1, attributes)
            }
        }

        when:
        String rendered = Asciidoctor.Factory.create().convert(TEST_DOC_BLOCK, Options.builder().build())

        then:
        rendered.contains('The content of the URL')
    }

    def 'Should apply IncludeProcessor from Extension file'() {
        given:
        AsciidoctorExtensions.extensions(new File('src/test/resources/testincludeprocessorextension.groovy'))

        when:
        String rendered = Asciidoctor.Factory.create().convert(TEST_DOC_BLOCK, Options.builder().build())

        then:
        rendered.contains('The content of the URL')
    }

    def 'Should apply BlockMacroProcessor from String'() {
        given:
        String extension = new File('src/test/resources/testblockmacroextension.groovy').text
        AsciidoctorExtensions.extensions(extension)

        when:
        String rendered = Asciidoctor.Factory.create().convert(TEST_DOC_BLOCK, Options.builder().build())

        then:
        rendered.contains("https://gist.github.com/123456.js")
    }

    def 'Should apply BlockMacroProcessor from Closure'() {
        given:
        AsciidoctorExtensions.extensions {
            block_macro("gist") {
                parent, target, attributes ->
                    String content = """<div class="content">
<script src="https://gist.github.com/${target}.js"></script>
</div>"""
                    createBlock(parent, "pass", [content], attributes)
            }
        }

        when:
        String rendered = Asciidoctor.Factory.create().convert(TEST_DOC_BLOCK, Options.builder().build())

        then:
        rendered.contains("https://gist.github.com/123456.js")
    }

    def 'Should apply BlockMacroProcessor from Extension file'() {
        given:
        AsciidoctorExtensions.extensions(new File('src/test/resources/testblockmacroextension.groovy'))

        when:
        String rendered = Asciidoctor.Factory.create().convert(TEST_DOC_BLOCK, Options.builder().build())

        then:
        rendered.contains("https://gist.github.com/123456.js")
    }

    def 'Should apply InlineMacroProcessor from String'() {
        given:
        String extension = new File('src/test/resources/testinlinemacroprocessorextension.groovy').text
        AsciidoctorExtensions.extensions(extension)

        when:
        String rendered = Asciidoctor.Factory.create().convert(TEST_DOC_BLOCK, Options.builder().build())

        then:
        rendered.contains('<a href="gittutorial.html">gittutorial</a>')
    }

    def 'Should apply InlineMacroProcessor from Closure'() {
        given:

        AsciidoctorExtensions.extensions {
            inline_macro('man') {
                parent, target, attributes ->
                    def options = ["type": ":link", "target": target + ".html"]
                    createPhraseNode(parent, "anchor", target, attributes, options)
            }
        }

        when:
        String rendered = Asciidoctor.Factory.create().convert(TEST_DOC_BLOCK, Options.builder().build())

        then:
        rendered.contains('<a href="gittutorial.html">gittutorial</a>')
    }

    def 'Should apply InlineMacroProcessor from Extension file'() {
        given:
        AsciidoctorExtensions.extensions(new File('src/test/resources/testinlinemacroprocessorextension.groovy'))

        when:
        String rendered = Asciidoctor.Factory.create().convert(TEST_DOC_BLOCK, Options.builder().build())

        then:
        rendered.contains('<a href="gittutorial.html">gittutorial</a>')
    }

    def 'Should apply Treeprocessor from String'() {
        given:
        String extension = new File('src/test/resources/testtreeprocessorextension.groovy').text
        AsciidoctorExtensions.extensions(extension)

        when:
        String rendered = Asciidoctor.Factory.create().convert('''
$ echo "Hello, World!"

$ gem install asciidoctor
''', Options.builder().build())

        then:
        rendered.contains('<div class="listingblock terminal">')
        rendered.contains('<span class="command">gem install asciidoctor</span>')
    }

    def 'Should apply Treeprocessor from Closure'() {
        given:
        AsciidoctorExtensions.extensions {
            treeprocessor {
                document ->
                    def blocks = document.blocks
                    blocks.eachWithIndex { block, i ->
                        def lines = block.lines
                        if (lines.size() > 0 && lines[0].startsWith('$')) {
                            Map attributes = block.attributes
                            attributes["role"] = "terminal"
                            def resultLines = lines.collect {
                                it.startsWith('$') ? "<span class=\"command\">${it.substring(2)}</span>".toString() : it
                            }
                            blocks[i] = createBlock(document, "listing", resultLines, attributes, [:])
                        }
                    }
            }
        }

        when:
        String rendered = Asciidoctor.Factory.create().convert('''
$ echo "Hello, World!"

$ gem install asciidoctor
''', Options.builder().build())

        then:
        rendered.contains('<div class="listingblock terminal">')
        rendered.contains('<span class="command">gem install asciidoctor</span>')
    }

    def 'Should apply Treeprocessor from Extension file'() {
        given:
        AsciidoctorExtensions.extensions(new File('src/test/resources/testtreeprocessorextension.groovy'))

        when:
        String rendered = Asciidoctor.Factory.create().convert('''
$ echo "Hello, World!"

$ gem install asciidoctor
''', Options.builder().build())

        then:
        rendered.contains('<div class="listingblock terminal">')
        rendered.contains('<span class="command">gem install asciidoctor</span>')
    }

    def 'Should apply DocinfoProcessor from String'() {
        given:
        String extension = new File('src/test/resources/testdocinfoprocessorextension.groovy').text
        AsciidoctorExtensions.extensions(extension)

        when:
        String rendered = Asciidoctor.Factory.create().convert(
                '''
= Hello

World''',
                Options.builder().standalone(true).safe(SafeMode.SERVER).toFile(false).build())

        then:
        // (?ms) Multiline regexp with dotall (= '.' matches newline as well)
        rendered ==~ /(?ms).*<head>.*<meta name="hello" content="world">.*<\/head>.*/
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
        String rendered = Asciidoctor.Factory.create().convert(
                '''
= Hello

World''',
                Options.builder().standalone(true).safe(SafeMode.SERVER).toFile(false).build())

        then:
        // (?ms) Multiline regexp with dotall (= '.' matches newline as well)
        rendered ==~ /(?ms).*<head>.*<meta name="hello" content="world">.*<\/head>.*/
    }

    def 'Should apply DocinfoProcessor from Extension file'() {
        given:
        AsciidoctorExtensions.extensions(new File('src/test/resources/testdocinfoprocessorextension.groovy'))

        when:
        String rendered = Asciidoctor.Factory.create().convert(
                '''
= Hello

World''',
                Options.builder().standalone(true).safe(SafeMode.SERVER).toFile(false).build())

        then:
        // (?ms) Multiline regexp with dotall (= '.' matches newline as well)
        rendered ==~ /(?ms).*<head>.*<meta name="hello" content="world">.*<\/head>.*/
    }

    def 'Should apply BlockMacroProcessor from Closure with string parameter'() {
        given:

        AsciidoctorExtensions.extensions {
            block_macro('capitalize') {
                parent, target, attributes ->
                    def capitalLines = target.toLowerCase()
                            .tokenize('_')
                            .collect { it.capitalize() }
                            .join(' ')
                    createBlock(parent, 'pass', [capitalLines], attributes)
            }
        }

        when:
        String rendered = Asciidoctor.Factory.create().convert(TEST_DOC_BLOCK, Options.builder().build())

        then:
        rendered.contains('This Sets The First Letter In Capitals And Removes Underscores')
        rendered.contains('Ignore this.')
    }

    def 'Should apply BlockMacroProcessor from Closure with named parameter'() {
        given:

        AsciidoctorExtensions.extensions {
            block_macro(name: 'capitalize') {
                parent, target, attributes ->
                    def capitalLines = target.toLowerCase()
                            .tokenize('_')
                            .collect { it.capitalize() }
                            .join(' ')
                    createBlock(parent, 'pass', [capitalLines], attributes)
            }
        }

        when:
        String rendered = Asciidoctor.Factory.create().convert(TEST_DOC_BLOCK, Options.builder().build())

        then:
        rendered.contains('This Sets The First Letter In Capitals And Removes Underscores')
        rendered.contains('Ignore this.')
    }

    def 'Should apply InlineMacroProcessor from Closure with named parameter'() {
        given:

        AsciidoctorExtensions.extensions {
            inline_macro(name: 'man') {
                parent, target, attributes ->
                    def options = ["type": ":link", "target": target + ".html"]
                    createPhraseNode(parent, "anchor", target, attributes, options)
            }
        }

        when:
        String rendered = Asciidoctor.Factory.create().convert(TEST_DOC_BLOCK, Options.builder().build())

        then:
        rendered.contains('<a href="gittutorial.html">gittutorial</a>')
    }

    def 'Should apply InlineMacroProcessor from Closure with string parameter'() {
        given:

        AsciidoctorExtensions.extensions {
            inline_macro('man') {
                parent, target, attributes ->
                    def options = ["type": ":link", "target": target + ".html"]
                    createPhraseNode(parent, "anchor", target, attributes, options)
            }
        }

        when:
        String rendered = Asciidoctor.Factory.create().convert(TEST_DOC_BLOCK, Options.builder().build())

        then:
        rendered.contains('<a href="gittutorial.html">gittutorial</a>')
    }

}
