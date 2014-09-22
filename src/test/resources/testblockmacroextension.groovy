blockmacro (name: "gist") {
    parent, target, attributes ->
    String content = """<div class="content"> 
<script src="https://gist.github.com/${target}.js"></script> 
</div>"""
    createBlock(parent, "pass", [content], attributes, config);
}

