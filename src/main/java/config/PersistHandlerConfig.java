package config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.persist.RepositoryStateMachinePersist;

import entities.ProcessRepository;
import machine.Events;
import machine.Tasks;
import persist.JpaStateMachineContextRepository;
import persist.JpaStateMachinePersister;

@Configuration
public class PersistHandlerConfig {

	private static final Logger logger = LoggerFactory.getLogger(PersistHandlerConfig.class);

	@Autowired
	ProcessRepository processRepository;	
	
	@Bean
	public JpaStateMachinePersister<Tasks, Events> jpaStateMachinePersister(StateMachinePersist<Tasks, Events, String> stateMachinePersist) {
		logger.info("public JpaStateMachinePersister<States, Events> jpaStateMachinePersister(StateMachinePersist<States, Events, String> stateMachinePersist)");
		return new JpaStateMachinePersister<Tasks, Events>(stateMachinePersist);
	}
	
	@Bean
	public StateMachinePersist<Tasks, Events, String> stateMachinePersist() {
		logger.info("public StateMachinePersist<States, Events, String> stateMachinePersist()");
		JpaStateMachineContextRepository<Tasks, Events> repository = new JpaStateMachineContextRepository<Tasks, Events>(processRepository);
		return new RepositoryStateMachinePersist<Tasks, Events>(repository);
	}		

}