package io.github.knownitwhy.gdr.dot

fun buildDot(block: DotScope.() -> Unit): String {
    val stmtList = mutableListOf<Stmt>()
    
    object : DotScope {
        override fun edge(from: String, to: String) {
            stmtList.add(EdgeStmt(from, to))
        }

        override fun node(name: String, block: NodeScope.() -> Unit) {
            val stmt = NodeStmt(name)
            object : NodeScope {
                override var shape: Shape?
                    get() = stmt.shape
                    set(value) {
                        stmt.shape = value
                    }
                override var label: String?
                    get() = stmt.label?.value
                    set(value) {
                        stmt.label = if (value != null) Label(value) else null
                    }
                override var color: Int?
                    get() = stmt.color?.intColor
                    set(value) {
                        stmt.color = if (value != null) Color(value) else null
                    }
            }.block()
            stmtList.add(stmt)
        }

    }.block()

    return stmtList.distinct()
        .joinToString(
            prefix = "digraph {",
            postfix = "}",
            separator = ";\n"
        ) { stmt -> stmt.statement() }
}

interface DotScope {

    fun edge(from: String, to: String)

    fun node(name: String, block: NodeScope.() -> Unit)

}

interface NodeScope {
    var shape: Shape?
    var label: String?
    var color: Int?
}

interface Attribute {
    val key: String
    val value: String
}

enum class Shape(override val value: String) : Attribute {
    Box("box"),
    Oval("oval");

    override val key: String = "shape"
}

class Label(override val value: String): Attribute {
    
    override val key: String = "label"
}

class Color(val intColor: Int) : Attribute {
    override val key: String = "color"
    override val value: String = String.format("#%06X", 0xFFFFFF and intColor)
}

private sealed class Stmt {
    abstract fun statement(): String
}

private class EdgeStmt(val from: String, val to: String) : Stmt() {

    override fun statement(): String {
        return "\"${from}\" -> \"${to}\""
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EdgeStmt

        if (from != other.from) return false
        if (to != other.to) return false

        return true
    }

    override fun hashCode(): Int {
        var result = from.hashCode()
        result = 31 * result + to.hashCode()
        return result
    }

}

private class NodeStmt(
    val name: String,
    var shape: Shape? = null,
    var label: Label? = null,
    var color: Color? = null
) : Stmt() {

    override fun statement(): String {
        val attrs = listOfNotNull(shape, label, color).joinToString(
            prefix = "[",
            postfix = "]",
            separator = " "
        ) { attr -> "${attr.key}=\"${attr.value}\"" }
        return "\"$name\" $attrs"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NodeStmt

        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

}