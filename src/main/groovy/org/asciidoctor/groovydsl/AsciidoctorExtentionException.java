package org.asciidoctor.groovydsl;

import groovy.transform.InheritConstructors;

/**
 * Generic exception to manage Asciidoctor Extension DSL processing errors
 */
@InheritConstructors
public class AsciidoctorExtentionException extends Exception {


    public AsciidoctorExtentionException() {
    }

    public AsciidoctorExtentionException(String message) {
        super(message);
    }

    public AsciidoctorExtentionException(String message, Throwable cause) {
        super(message, cause);
    }

    public AsciidoctorExtentionException(Throwable cause) {
        super(cause);
    }

}
