package notscan;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.statemachine.StateMachine;
import org.springframework.stereotype.Component;

import machine.Events;
import machine.States;

@Component
public class Machine {
	
	private final static Log log = LogFactory.getLog(Machine.class);

	@Autowired
	private StateMachine<States, Events> stateMachine;
	
}
