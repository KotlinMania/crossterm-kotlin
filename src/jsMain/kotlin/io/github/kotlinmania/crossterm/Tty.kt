// port-lint: source tty.rs
package io.github.kotlinmania.crossterm

actual fun isatty(fd: Int): Boolean {
    val req =
        js(
            """(function(){ try { var rq = (new Function('return typeof require === "function" ? require : null'))();
if (!rq) return false; return rq('tty').isatty(fd) === true; } catch (e) { return false; } })()""",
        )
    return req as Boolean
}
