// port-lint: source style/types/colored.rs
package io.github.kotlinmania.crossterm.style.types

internal actual fun getEnvironmentVariable(name: String): String? {
    return js("(function(){ try { var rq = (new Function('return typeof require === \"function\" ? require : null'))(); if (!rq) return null; return rq('process').env[name] || null; } catch (e) { return null; } })()") as String?
}