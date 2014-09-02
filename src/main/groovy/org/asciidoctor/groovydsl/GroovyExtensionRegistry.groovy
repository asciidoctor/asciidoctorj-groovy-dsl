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
import org.asciidoctor.extension.spi.ExtensionRegistry
/**
 * The service implementation for org.asciidoctor.extension.spi.ExtensionRegistry.
 * It simply delegates the register() call to {@link AsciidoctorExtensions} 
 * that owns all configured extensions and registers it on the Asciidoctor instance.
 */
class GroovyExtensionRegistry implements ExtensionRegistry {
	
    void register(Asciidoctor asciidoctor) {
        AsciidoctorExtensions.registerTo(asciidoctor)
    }

}

