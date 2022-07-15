package com.example.cqrs.other;

import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import static java.lang.String.format;

@Service
@AllArgsConstructor
public class OtherServiceImpl implements OtherService {

    private final ApplicationContext context;

    @Override
    public String getSomeString() {
        int count = context.getBeanDefinitionCount();
        return format("Bean definition count is %s",count );
    }
}
