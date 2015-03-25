inline_macro (name: "man") {
    parent, target, attributes ->
    options=["type": ":link", "target": target + ".html"]
    createInline(parent, "anchor", target, attributes, options).render()
}
