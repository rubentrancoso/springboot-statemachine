package persist;

import org.springframework.statemachine.StateMachinePersist;
import org.springframework.statemachine.persist.AbstractStateMachinePersister;
import org.springframework.statemachine.persist.StateMachinePersister;

/**
 * Implementation of a {@link StateMachinePersister} to be used with a redis.
 *
 * @author Janne Valkealahti
 *
 * @param <S>
 *            the type of state
 * @param <E>
 *            the type of event
 */
public class JpaStateMachinePersister<S, E> extends AbstractStateMachinePersister<S, E, String> {

	/**
	 * Instantiates a new redis state machine persister.
	 *
	 * @param stateMachinePersist
	 *            the state machine persist
	 */
	public JpaStateMachinePersister(StateMachinePersist<S, E, String> stateMachinePersist) {
		super(stateMachinePersist);
	}
}
