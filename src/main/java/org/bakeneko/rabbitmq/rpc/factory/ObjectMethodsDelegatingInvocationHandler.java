/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bakeneko.rabbitmq.rpc.factory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * An {@link InvocationHandler} decorator for handling the {@link Object} methods. Useful for dynamic {@link java.lang.reflect.Proxy} generation.
 *
 * @author Ivan Sergienko
 */
public class ObjectMethodsDelegatingInvocationHandler implements InvocationHandler {
    private InvocationHandler handler;

    public ObjectMethodsDelegatingInvocationHandler(InvocationHandler handler) {
        this.handler = handler;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getDeclaringClass() == Object.class) {
            if (method.equals(Object.class.getMethod("hashCode", null))) {
                return System.identityHashCode(proxy);
            } else if (method.equals(Object.class.getMethod("equals", Object.class))) {
                return proxy == args[0];
            } else if (method.equals(Object.class.getMethod("toString", null))) {
                return String.valueOf(System.identityHashCode(proxy));
            } else {
                throw new InternalError("Unexpected Object method invoked: " + method);
            }
        } else {
            return handler.invoke(proxy, method, args);
        }
    }
}
