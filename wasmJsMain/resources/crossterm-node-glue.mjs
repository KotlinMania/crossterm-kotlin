/**
 * JavaScript glue code for Kotlin/Wasm to access Node.js terminal APIs.
 * This file provides the external functions that the Kotlin/Wasm code calls.
 */

/**
 * Check if a file descriptor is a TTY in Node.js.
 * @param {number} fd - File descriptor (0=stdin, 1=stdout, 2=stderr)
 * @returns {boolean} true if the fd refers to a TTY
 */
export function isNodeTty(fd) {
    try {
        if (typeof process === 'undefined') {
            return false;
        }

        const stream = fd === 0 ? process.stdin : fd === 1 ? process.stdout : fd === 2 ? process.stderr : null;
        return stream?.isTTY === true;
    } catch (_) {
        return false;
    }
}

/**
 * Write a string to stdout using Node.js process.stdout.write().
 * @param {string} str - String to write
 */
export function nodeWriteStdout(str) {
    if (typeof process === 'undefined' || !process.stdout) {
        throw new Error('Node.js stdout not available');
    }
    process.stdout.write(str);
}

/**
 * Set raw mode on stdin.
 * @param {boolean} enabled - Whether to enable raw mode
 */
export function nodeStdinSetRawMode(enabled) {
    if (typeof process === 'undefined' || !process.stdin) {
        throw new Error('Node.js stdin not available');
    }
    if (!process.stdin.isTTY) {
        throw new Error('stdin is not a TTY');
    }
    process.stdin.setRawMode(enabled);
}

/**
 * Resume stdin to start reading.
 */
export function nodeStdinResume() {
    if (typeof process === 'undefined' || !process.stdin) {
        throw new Error('Node.js stdin not available');
    }
    if (process.stdin.resume) {
        process.stdin.resume();
    }
}

/**
 * Check if stdin is in raw mode.
 * @returns {boolean} true if stdin is in raw mode
 */
export function nodeStdinIsRaw() {
    try {
        if (typeof process === 'undefined' || !process.stdin) {
            return false;
        }
        return process.stdin.isRaw === true;
    } catch (_) {
        return false;
    }
}

/**
 * Get stdout columns.
 * @returns {number} Number of columns
 */
export function nodeStdoutColumns() {
    if (typeof process === 'undefined' || !process.stdout) {
        throw new Error('Node.js stdout not available');
    }
    if (!process.stdout.isTTY) {
        throw new Error('stdout is not a TTY');
    }
    const cols = process.stdout.columns;
    if (typeof cols !== 'number') {
        throw new Error('stdout.columns is not available');
    }
    return cols;
}

/**
 * Get stdout rows.
 * @returns {number} Number of rows
 */
export function nodeStdoutRows() {
    if (typeof process === 'undefined' || !process.stdout) {
        throw new Error('Node.js stdout not available');
    }
    if (!process.stdout.isTTY) {
        throw new Error('stdout is not a TTY');
    }
    const rows = process.stdout.rows;
    if (typeof rows !== 'number') {
        throw new Error('stdout.rows is not available');
    }
    return rows;
}
