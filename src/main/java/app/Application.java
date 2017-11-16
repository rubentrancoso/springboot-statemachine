package app;


import java.util.HashMap;

// https://github.com/spring-projects/spring-statemachine/blob/master/spring-statemachine-samples/eventservice/src/main/java/demo/eventservice/StateMachineConfig.java

import java.util.Random;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import entities.Process;
import entities.ProcessRepository;
import machine.Utils;

@EnableAutoConfiguration
@EnableJpaRepositories (basePackageClasses = {ProcessRepository.class})
@EntityScan (basePackages = {"entities"})
@SpringBootApplication(scanBasePackages={"app", "config", "machine", "entities", "persist"})
public class Application  extends SpringBootServletInitializer {
	
	public static void main(String[] args) throws Exception {
		ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
		ProcessRepository repository = context.getBean(ProcessRepository.class);
		for(int i=0;i<150;i++) {
			Process process = new Process();
			process.setName(Utils.nameFactory(new Random(), "ABCDEFGHIJKLMNOPQRSTUVWXYZ", 10));
			process.setState(Utils.stateFactory());
			
			HashMap<String,String> globals = new HashMap<String,String>();
			globals.put("a", "string:this is a global string");
			globals.put("b", "boolean:false");
			globals.put("c", "int:0");
			process.setGlobals(globals);

			HashMap<String,String> input = new HashMap<String,String>();
			input.put("d", "string:this is a input text");
			input.put("e", "boolean:true");
			input.put("f", "int:1");
			process.setInput(input);
			
			repository.save(process);
		}
	}

}
