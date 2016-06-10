throw new Exception('This error is on purpose')

block (name: "BIG", contexts: [":paragraph"]) {
    parent, reader, attributes ->
    def upperLines = reader.readLines()
    .collect {it.toUpperCase()}
    .inject("") {a, b -> a + '\\n' + b}

    createBlock(parent, "paragraph", [upperLines], attributes, [:])
}
