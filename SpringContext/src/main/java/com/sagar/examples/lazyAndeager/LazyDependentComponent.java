package com.sagar.examples.lazyAndeager;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class LazyDependentComponent {
    private  LazyComponent lazyComponent;
    public LazyDependentComponent(@Lazy LazyComponent lazyComponent){
        System.out.println("lazy dependent created using proxy injection");
        this.lazyComponent=lazyComponent;
    }
}
