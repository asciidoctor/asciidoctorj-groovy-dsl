block (name: "BIG", contexts: [":paragraph"]) {
    parent, reader, attributes ->
    def upperLines = reader.readLines()
    .collect {it.toUpperCase()}
    .inject("") {a, b -> a + '\\n' + b}

    createBlock(parent, "paragraph", [upperLines], attributes, [:])
}
block("small") {
    parent, reader, attributes ->
    def lowerLines = reader.readLines()
    .collect {it.toLowerCase()}
    .inject("") {a, b -> a + '\\n' + b}
                
    createBlock(parent, "paragraph", [lowerLines], attributes, [:])
}
