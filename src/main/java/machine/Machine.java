package machine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.annotation.WithStateMachine;

@WithStateMachine
public class Machine {

	private static final Logger logger = LoggerFactory.getLogger(Machine.class);

	@StatesOnTransition(target = Tasks.NOT_STARTED)
	public void toNotStarted(StateContext<Tasks, Events> stateContext, Message<Events> message) {
		Utils.showGlobals(stateContext);
		Utils.showInput(message);
		logger.info("toNotStarted()");
	}

	@StatesOnTransition(target = Tasks.WORKING)
	public void toWorking(StateContext<Tasks, Events> stateContext, Message<Events> message) {
		Utils.showGlobals(stateContext);
		Utils.showInput(message);
		logger.info("toWorking()");
	}

	@StatesOnTransition(target = Tasks.FAILING)
	public void toFailing(StateContext<Tasks, Events> stateContext, Message<Events> message) {
		Utils.showGlobals(stateContext);
		Utils.showInput(message);
		logger.info("toFailing()");
	}

	@StatesOnTransition(target = Tasks.PENDING)
	public void toPending(StateContext<Tasks, Events> stateContext, Message<Events> message) {
		Utils.showGlobals(stateContext);
		Utils.showInput(message);
		logger.info("toPending()");
	}

	@StatesOnTransition(target = Tasks.DONE)
	public void toDone(StateContext<Tasks, Events> stateContext, Message<Events> message) {
		Utils.showGlobals(stateContext);
		Utils.showInput(message);
		logger.info("toState2()");
	}

}
