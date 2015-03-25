String content = "The content of the URL"

include_processor (filter: {it.startsWith("http")}) {
    document, reader, target, attributes ->
    reader.push_include(content, target, target, 1, attributes);					
}
