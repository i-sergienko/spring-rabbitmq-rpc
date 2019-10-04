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

import java.lang.reflect.Method;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Ivan Sergienko
 */
public class ReflectionUtils {

    /**
     * Generates a {@link String} representation of the {@link Method} signature.
     *
     * @param method the {@link Method} to generate the signature {@link String} representation of.
     * @return the {@link String} representation of the method signature.
     */
    public static String methodNameSignatureAware(Method method) {
        return String.format("%s::%s::%s",
                method.getAnnotatedReturnType().getType().getTypeName(),
                method.getName(),
                Stream.of(method.getParameterTypes()).map(Class::getName).collect(Collectors.joining("::"))
        );
    }
}
