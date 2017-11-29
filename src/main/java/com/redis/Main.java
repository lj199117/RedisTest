package com.redis;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.redis.service.PersonService;

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
