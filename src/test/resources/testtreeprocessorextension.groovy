treeprocessor {
    document ->
    List blocks = document.blocks()
    (0..<blocks.length).each {
        def block = blocks[it]
        def lines = block.lines()
        if (lines.size() > 0 && lines[0].startsWith('$')) {
            Map attributes = block.attributes()
            attributes["role"] = "terminal";
            def resultLines = lines.collect {
                it.startsWith('$') ? "<span class=\"command\">${it.substring(2)}</span>" : it
            }
            blocks[it] = createBlock(document, "listing", resultLines, attributes,[:])
        }
    }
}
