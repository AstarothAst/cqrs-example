package com.example.cqrs.other;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import static java.lang.String.format;

@Component
@RequestScope
public class RequestScopeBeanImpl implements RequestScopeBean {

    private String str;

    @Override
    public String getStr() {
        return str;
    }

    @Override
    public void setStr(Long val) {
        this.str = format("RequestScope значение установлено в значение %s", val);
    }
}
