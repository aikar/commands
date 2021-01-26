/*
 * Copyright (c) 2016-2020 Daniel Ennis (Aikar) - MIT License
 *
 *  Permission is hereby granted, free of charge, to any person obtaining
 *  a copy of this software and associated documentation files (the
 *  "Software"), to deal in the Software without restriction, including
 *  without limitation the rights to use, copy, modify, merge, publish,
 *  distribute, sublicense, and/or sell copies of the Software, and to
 *  permit persons to whom the Software is furnished to do so, subject to
 *  the following conditions:
 *
 *  The above copyright notice and this permission notice shall be
 *  included in all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 *  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 *  LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 *  OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 *  WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package co.aikar.commands.kotlin

import kotlinx.coroutines.ThreadContextElement
import java.util.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class ThreadLocalStackRestorer<T> @JvmOverloads constructor(
        private val localStack: ThreadLocal<Stack<T>>,
        private val data: T = localStack.get().peek()
) : ThreadContextElement<Unit> {

    inner class Key : CoroutineContext.Key<ThreadLocalStackRestorer<T>>

    override val key: CoroutineContext.Key<*> = Key()

    override fun restoreThreadContext(context: CoroutineContext, oldState: Unit) {
        localStack.get().pop()
    }

    override fun updateThreadContext(context: CoroutineContext) {
        localStack.get().push(this.data)
    }
}

// This method is required, because we can't have a local variable
// in code where we're not sure the class (CoroutineContext) exists at runtime.
// If it doesn't, loading the entire class fails before we have a chance to catch it.
@JvmOverloads
fun <T> ThreadLocal<Stack<T>>.toThreadLocalStackRestorerOrEmptyContext(data: T = this.get().peek()) =
        try {
            ThreadLocalStackRestorer(this, data)
        } catch (e: NoClassDefFoundError) {
            EmptyCoroutineContext
        }
