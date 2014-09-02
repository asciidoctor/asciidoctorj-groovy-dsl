import org.jsoup.*

String copyright = "Copyright Acme, Inc."

postprocessor {
    document, output ->
    if(document.basebackend("html")) {
        org.jsoup.nodes.Document doc = Jsoup.parse(output, "UTF-8")

        def contentElement = doc.getElementsByTag("body")
        contentElement.append(copyright)
        doc.html()
    } else {
        throw new IllegalArgumentException("Expected html!")
    }
}
