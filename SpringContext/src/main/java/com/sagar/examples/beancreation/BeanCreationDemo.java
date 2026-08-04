package com.sagar.examples.beancreation;


import com.sagar.examples.beancreation.config.MyConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class BeanCreationDemo {
    public static void main(String[] args) {
        ApplicationContext context=new AnnotationConfigApplicationContext(MyConfig.class);

        //bean from component annotation
        Person person1=context.getBean(Person.class);
        System.out.println(person1.getName());

        Person person2=context.getBean("person",Person.class);
        System.out.println(person2.getName());


        //bean creation by @Component annotation
        SpecialPerson specialPerson=context.getBean("sagar",SpecialPerson.class);
        System.out.println(specialPerson.getName());
        SpecialPerson specialPerson2=context.getBean("god",SpecialPerson.class);
        System.out.println(specialPerson2.getName());




    }
}
