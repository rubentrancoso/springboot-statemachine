package notscan;

import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachineSystemConstants;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.guard.Guard;
import org.springframework.util.ObjectUtils;

import machine.Events;
import machine.States;

@Configuration
public class StateMachineConfig extends EnumStateMachineConfigurerAdapter<States, Events> {

//tag::snippetAA[]
@Override
public void configure(StateMachineStateConfigurer<States, Events> states) throws Exception {
	states
		.withStates()
			.initial(States.READY)
			.fork(States.FORK)
			.state(States.TASKS)
			.join(States.JOIN)
			.choice(States.CHOICE)
			.state(States.ERROR)
			.and()
			.withStates()
				.parent(States.TASKS)
				.initial(States.T1)
				.end(States.T1E)
				.and()
			.withStates()
				.parent(States.ERROR)
				.initial(States.AUTOMATIC)
				.state(States.AUTOMATIC, automaticAction(), null)
				.state(States.MANUAL);
}
//end::snippetAA[]

//tag::snippetAB[]
@Override
public void configure(StateMachineTransitionConfigurer<States, Events> transitions) throws Exception {
	transitions
		.withExternal()
			.source(States.READY).target(States.FORK)
			.event(Events.RUN)
			.and()
		.withFork()
			.source(States.FORK).target(States.TASKS)
			.and()
		.withExternal()
			.source(States.T1).target(States.T1E)
			.and()
		.withJoin()
			.source(States.TASKS).target(States.JOIN)
			.and()
		.withExternal()
			.source(States.JOIN).target(States.CHOICE)
			.and()
		.withChoice()
			.source(States.CHOICE)
			.first(States.ERROR, tasksChoiceGuard())
			.last(States.READY)
			.and()
		.withExternal()
			.source(States.ERROR).target(States.READY)
			.event(Events.CONTINUE)
			.and()
		.withExternal()
			.source(States.AUTOMATIC).target(States.MANUAL)
			.event(Events.FALLBACK)
			.and()
		.withInternal()
			.source(States.MANUAL)
			.action(fixAction())
			.event(Events.FIX);
}
//end::snippetAB[]

//tag::snippetAC[]
@Bean
public Guard<States, Events> tasksChoiceGuard() {
	return new Guard<States, Events>() {

		@Override
		public boolean evaluate(StateContext<States, Events> context) {
			Map<Object, Object> variables = context.getExtendedState().getVariables();
			return !(ObjectUtils.nullSafeEquals(variables.get("T1"), true));
		}
	};
}
//end::snippetAC[]

//tag::snippetAD[]
@Bean
public Action<States, Events> automaticAction() {
	return new Action<States, Events>() {

		@Override
		public void execute(StateContext<States, Events> context) {
			Map<Object, Object> variables = context.getExtendedState().getVariables();
			if (ObjectUtils.nullSafeEquals(variables.get("T1"), true)) {
				context.getStateMachine().sendEvent(Events.CONTINUE);
			} else {
				context.getStateMachine().sendEvent(Events.FALLBACK);
			}
		}
	};
}

@Bean
public Action<States, Events> fixAction() {
	return new Action<States, Events>() {

		@Override
		public void execute(StateContext<States, Events> context) {
			Map<Object, Object> variables = context.getExtendedState().getVariables();
			variables.put("T1", true);
			context.getStateMachine().sendEvent(Events.CONTINUE);
		}
	};
}
//end::snippetAD[]

@Bean
public Tasks tasks() {
	return new Tasks();
}

//tag::snippetAE[]
@Bean(name = StateMachineSystemConstants.TASK_EXECUTOR_BEAN_NAME)
public TaskExecutor taskExecutor() {
	ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
	taskExecutor.setCorePoolSize(5);
	return taskExecutor;
}
//end::snippetAE[]

}