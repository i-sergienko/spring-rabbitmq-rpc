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

import org.springframework.core.env.Environment;
import org.springframework.util.PropertyPlaceholderHelper;

import java.util.regex.Pattern;

/**
 * @author Ivan Sergienko
 */
public class PropertiesResolverImpl implements PropertiesResolver {
    private Environment environment;

    private PropertyPlaceholderHelper propertyPlaceholderHelper = new PropertyPlaceholderHelper("${", "}", ":", true);
    private Pattern propertyReferencePattern = Pattern.compile("^\\$\\{.*}$");

    public PropertiesResolverImpl(Environment environment) {
        this.environment = environment;
    }

    @Override
    public String replaceIfProperty(String value) {
        if (value != null && !value.isEmpty() && propertyReferencePattern.matcher(value).matches()) {
            return propertyPlaceholderHelper.replacePlaceholders(value, name -> environment.getRequiredProperty(name));
        } else {
            return value;
        }
    }
}
