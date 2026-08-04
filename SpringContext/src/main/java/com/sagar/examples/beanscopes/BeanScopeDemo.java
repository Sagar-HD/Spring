package com.sagar.examples.beanscopes;

import com.sagar.examples.beanscopes.config.MyConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class BeanScopeDemo {
    public static void main(String[] args) {

        ApplicationContext context=new AnnotationConfigApplicationContext(MyConfig.class);
        SingletonBean singletonBean1=context.getBean(SingletonBean.class);
        SingletonBean singletonBean2=context.getBean(SingletonBean.class);
        System.out.println(singletonBean1==singletonBean2);
        //prototype bean are always lazy initialisation
        //diffrent instace each time
       //Spring manages the creation of prototype beans,
        // but after handing the object to your application,
        // it no longer tracks or destroys that specific instance.
        PrototypeBean prototypeBean1=context.getBean(PrototypeBean.class);
        PrototypeBean prototypeBean2=context.getBean(PrototypeBean.class);
        System.out.println(prototypeBean1==prototypeBean2);

    }

}
