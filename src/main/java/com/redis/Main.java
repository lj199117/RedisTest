package com.redis;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.redis.service.PersonService;

/**
 * 測試AOP切面
 * @author <a href="mailto:lijin@webull.com">李锦</a>
 * @since 0.1.0
 */
public class Main {
	public static void main(String[] args) {
		ApplicationContext appContext = new ClassPathXmlApplicationContext("/conf/app.xml");
		PersonService personService = appContext.getBean(PersonService.class);
		String personName = "Jim";
		personService.addPerson(personName);
		personService.deletePerson(personName);
		personService.editPerson(personName);
		((ClassPathXmlApplicationContext) appContext).close();
	}
}
