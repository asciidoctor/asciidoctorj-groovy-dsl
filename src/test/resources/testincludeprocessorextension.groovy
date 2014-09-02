String content = "The content of the URL"

includeprocessor (filter: {it.startsWith("http")}) {
    document, reader, target, attributes ->
    reader.push_include(content, target, target, 1, attributes);					
}
