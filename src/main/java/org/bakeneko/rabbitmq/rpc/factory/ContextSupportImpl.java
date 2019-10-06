package org.bakeneko.rabbitmq.rpc.factory;

import org.springframework.context.ApplicationContext;

public class ContextSupportImpl implements ContextSupport {
    private ApplicationContext context;

    public ContextSupportImpl(ApplicationContext context) {
        this.context = context;
    }

    @Override
    public <T> T getBean(String name, Class<T> type) {
        return context.getBean(name, type);
    }
}
