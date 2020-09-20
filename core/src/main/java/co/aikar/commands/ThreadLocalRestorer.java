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

package co.aikar.commands;

import kotlin.coroutines.CoroutineContext;
import kotlin.jvm.functions.Function2;
import kotlinx.coroutines.ThreadContextElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ThreadLocalRestorer implements ThreadContextElement<Void> {

    private final CommandOperationContext context;
    private final Key key = new Key();

    public ThreadLocalRestorer(CommandOperationContext context) {
        this.context = context;
    }

    @Override
    public void restoreThreadContext(@NotNull CoroutineContext coroutineContext, Void unused) {
        CommandManager.commandOperationContext.get().pop();
    }

    @Override
    public Void updateThreadContext(@NotNull CoroutineContext coroutineContext) {
        CommandManager.commandOperationContext.get().push(this.context);
        return null;
    }

    @NotNull
    @Override
    public CoroutineContext plus(@NotNull CoroutineContext coroutineContext) {
        return DefaultImpls.plus(this, coroutineContext);
    }

    @NotNull
    @Override
    public Key getKey() {
        return this.key;
    }

    @Override
    public <R> R fold(R r, @NotNull Function2<? super R, ? super Element, ? extends R> function2) {
        return DefaultImpls.fold(this, r, function2);
    }

    @Nullable
    @Override
    public <E extends Element> E get(@NotNull CoroutineContext.Key<E> key) {
        return DefaultImpls.get(this, key);
    }

    @NotNull
    @Override
    public CoroutineContext minusKey(@NotNull CoroutineContext.Key key) {
        return DefaultImpls.minusKey(this, key);
    }

    public static final class Key implements kotlin.coroutines.CoroutineContext.Key<ThreadLocalRestorer> {
    }
}
