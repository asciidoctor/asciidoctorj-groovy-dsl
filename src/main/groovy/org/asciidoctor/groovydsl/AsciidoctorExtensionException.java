package org.asciidoctor.groovydsl;


/**
 * Generic exception to manage Asciidoctor Extension DSL processing errors
 */
public class AsciidoctorExtensionException extends Exception {

    public AsciidoctorExtensionException() {
    }

    public AsciidoctorExtensionException(String message) {
        super(message);
    }

    public AsciidoctorExtensionException(String message, Throwable cause) {
        super(message, cause);
    }

    public AsciidoctorExtensionException(Throwable cause) {
        super(cause);
    }

}
