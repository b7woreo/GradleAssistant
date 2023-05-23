package gradle.assistant.graphic

import java.io.File

interface Graphic {
    fun render(output: File, builder: Builder.() -> Unit)

    interface Builder {
        fun node(content: String, shape: Shape)
        fun edge(from: String, to: String)
    }

    sealed class Shape {
        object Box : Shape()
        object Oval : Shape()
        object Diamond : Shape()
    }
}

class MermaidGraphic : Graphic {
    private fun template(content: String): String {
        return """
        <!DOCTYPE html>
        <html lang="en">
        <body>
          <pre id="mermaid">$content</pre>
          <script type="module">
            import mermaid from 'https://cdn.jsdelivr.net/npm/mermaid@10/dist/mermaid.esm.min.mjs';
            import he from 'https://cdn.jsdelivr.net/npm/he@1.2.0/+esm';
            const { svg, _ } = await mermaid.render('mermaid', he.decode(document.getElementById('mermaid').innerHTML));
            document.body.innerHTML = svg;
            document.getElementById('mermaid').setAttribute('width', '99999%');
            document.getElementById('mermaid').setAttribute('height', '99999%');
          </script>
        </body>
        </html>
        """.trimIndent()
    }

    override fun render(output: File, builder: Graphic.Builder.() -> Unit) {
        val content = MermaidGraphicBuilder().apply {
            builder()
        }.build()
        output.writeText(template(content))
    }
}

private class MermaidGraphicBuilder : Graphic.Builder {

    private var currentId = 0
    private val ids = mutableMapOf<String, Int>()
    private val statements = mutableSetOf<String>()

    override fun node(content: String, shape: Graphic.Shape) {
        if (ids[content] != null) return

        ids[content] = currentId++

        when (shape) {
            is Graphic.Shape.Box -> {
                statements.add("${ids[content]}[\"$content\"]")
            }

            is Graphic.Shape.Oval -> {
                statements.add("${ids[content]}([\"$content\"])")
            }

            is Graphic.Shape.Diamond -> {
                statements.add("${ids[content]}{{\"$content\"}}")
            }
        }
    }

    override fun edge(from: String, to: String) {
        val fromId = ids[from] ?: return
        val toId = ids[to] ?: return
        statements.add("$fromId --> $toId")
    }

    fun build(): String {
        return "flowchart TD\n${statements.joinToString(separator = "\n")}"
    }
}