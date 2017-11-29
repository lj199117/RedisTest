package com.redis.service;

import org.springframework.stereotype.Service;

import com.redis.annotation.LiJinLog;

@Service
public class PersonService {
	
	@LiJinLog
	public void addPerson(String personName) {
		System.out.println("add person " + personName);
	}
	
	@LiJinLog
	public boolean deletePerson(String personName) {
		System.out.println("delete person " + personName) ;
		return true;
	}
	
	public void editPerson(String personName) {
		System.out.println("edit person " + personName);
		throw new RuntimeException("edit person throw exception");
	}
	
}
