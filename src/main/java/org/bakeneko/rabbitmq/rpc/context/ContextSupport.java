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

package org.bakeneko.rabbitmq.rpc.context;

/**
 * Utility for retrieval of Spring beans by name and type programmatically.
 *
 * @author Ivan Sergienko
 */
@FunctionalInterface
public interface ContextSupport {

    /**
     * Retrieves a required bean with specified {@code name} and {@code type} from Spring context.
     *
     * @param name name of the bean to be retrieved.
     * @param type type of the bean to be retrieved
     * @param <T>  generic type parameter of the bean to be retrieved.
     * @return a Spring bean with the specified name and type.
     * @see org.springframework.context.ApplicationContext#getBean(String, Class)
     */
    <T> T getBean(String name, Class<T> type);
}
