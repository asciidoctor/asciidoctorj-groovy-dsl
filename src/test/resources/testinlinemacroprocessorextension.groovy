inline_macro (name: "man") {
    parent, target, attributes ->
    options = [type: ":link", target: target + ".html"]
    createPhraseNode(parent, "anchor", target, attributes, options)
}
