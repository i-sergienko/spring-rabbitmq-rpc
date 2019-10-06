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

package org.bakeneko.rabbitmq.rpc;

import org.bakeneko.rabbitmq.rpc.context.ContextSupportImpl;
import org.bakeneko.rabbitmq.rpc.context.PropertyReferenceResolverImpl;
import org.bakeneko.rabbitmq.rpc.factory.RabbitClientAnnotationProcessorImpl;
import org.bakeneko.rabbitmq.rpc.factory.RabbitClientFactoryImpl;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The {@link ImportBeanDefinitionRegistrar} used to register {@link RabbitClient} bean definitions.
 *
 * @author Ivan Sergienko
 */
public class RabbitClientRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware, BeanClassLoaderAware {
    private ClassPathScanner classpathScanner;
    private ClassLoader classLoader;
    private ResourceLoader resourceLoader;

    private static final String PROPERTIES_RESOLVER_BEAN_NAME = PropertyReferenceResolverImpl.class.getName();
    private static final String CONTEXT_SUPPORT_BEAN_NAME = ContextSupportImpl.class.getName();
    private static final String ANNOTATION_PROCESSOR_BEAN_NAME = RabbitClientAnnotationProcessorImpl.class.getName();
    private static final String CLIENT_FACTORY_BEAN_NAME = RabbitClientFactoryImpl.class.getName();

    public RabbitClientRegistrar() {
        classpathScanner = new ClassPathScanner();
        classpathScanner.addIncludeFilter(new AnnotationTypeFilter(RabbitClient.class));
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry registry) {
        Assert.notNull(annotationMetadata, "AnnotationMetadata must not be null!");
        Assert.notNull(registry, "BeanDefinitionRegistry must not be null!");
        Assert.notNull(this.resourceLoader, "ResourceLoader must not be null!");
        if (annotationMetadata.getAnnotationAttributes(EnableRabbitRPC.class.getName()) != null) {
            registerDefinitionIfMissing(PROPERTIES_RESOLVER_BEAN_NAME, PropertyReferenceResolverImpl.class, registry);
            registerDefinitionIfMissing(CONTEXT_SUPPORT_BEAN_NAME, ContextSupportImpl.class, registry);
            registerDefinitionIfMissing(ANNOTATION_PROCESSOR_BEAN_NAME, RabbitClientAnnotationProcessorImpl.class, registry);
            registerDefinitionIfMissing(CLIENT_FACTORY_BEAN_NAME, RabbitClientFactoryImpl.class, registry);

            getBasePackages(annotationMetadata).forEach(p -> createRabbitClients(p, registry));
        }
    }

    private <T> void registerDefinitionIfMissing(String beanName, Class<T> beanType, BeanDefinitionRegistry registry) {
        if (!registry.containsBeanDefinition(beanName)) {
            registry.registerBeanDefinition(beanName, new RootBeanDefinition(beanType));
        }
    }

    private Set<String> getBasePackages(AnnotationMetadata importingClassMetadata) {
        Map<String, Object> attributes = importingClassMetadata.getAnnotationAttributes(EnableRabbitRPC.class.getCanonicalName());

        Set<String> basePackages = new HashSet<>();
        for (String pkg : (String[]) attributes.get("value")) {
            if (StringUtils.hasText(pkg)) {
                basePackages.add(pkg);
            }
        }
        for (String pkg : (String[]) attributes.get("basePackages")) {
            if (StringUtils.hasText(pkg)) {
                basePackages.add(pkg);
            }
        }
        for (Class<?> clazz : (Class[]) attributes.get("basePackageClasses")) {
            basePackages.add(ClassUtils.getPackageName(clazz));
        }

        if (basePackages.isEmpty()) {
            basePackages.add(
                    ClassUtils.getPackageName(importingClassMetadata.getClassName()));
        }
        return basePackages;
    }

    private void createRabbitClients(String basePackage, BeanDefinitionRegistry registry) {
        try {
            for (BeanDefinition beanDefinition : classpathScanner.findCandidateComponents(basePackage)) {
                Class<?> clazz = Class.forName(beanDefinition.getBeanClassName());
                String beanName = getBeanName(clazz);

                GenericBeanDefinition proxyBeanDefinition = new GenericBeanDefinition();
                proxyBeanDefinition.setBeanClass(clazz);

                ConstructorArgumentValues args = new ConstructorArgumentValues();
                args.addGenericArgumentValue(classLoader);
                args.addGenericArgumentValue(clazz);
                proxyBeanDefinition.setConstructorArgumentValues(args);

                proxyBeanDefinition.setFactoryBeanName(CLIENT_FACTORY_BEAN_NAME);
                proxyBeanDefinition.setFactoryMethodName("forType");

                registry.registerBeanDefinition(beanName, proxyBeanDefinition);
            }
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    private String getBeanName(Class<?> clazz) {
        String qualifier = clazz.isAnnotationPresent(Qualifier.class) ? clazz.getAnnotation(Qualifier.class).value() : null;

        if (qualifier != null && !"".equals(qualifier)) {
            return qualifier;
        } else {
            return ClassUtils.getShortNameAsProperty(clazz);
        }
    }

    private class ClassPathScanner extends ClassPathScanningCandidateComponentProvider {

        ClassPathScanner() {
            super(false);
        }

        @Override
        protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
            return beanDefinition.getMetadata().isIndependent();
        }

    }
}
