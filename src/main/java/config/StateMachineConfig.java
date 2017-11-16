/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import machine.Events;
import machine.Tasks;
import machine.Utils;

@Configuration
@EnableStateMachine
public class StateMachineConfig extends EnumStateMachineConfigurerAdapter<Tasks, Events> {

	private static final Logger logger = LoggerFactory.getLogger(StateMachineConfig.class);
	
	@Override
	public void configure(StateMachineStateConfigurer<Tasks, Events> states) throws Exception {
		logger.info("public void configure(StateMachineStateConfigurer<States, Events> states)");
        states
        .withStates()
            .initial(Tasks.NOT_STARTED, resetTries())
            .state(Tasks.WORKING, null, increaseTries())
            .state(Tasks.FAILING)
            .state(Tasks.PENDING, resetTries(), null)
            .end(Tasks.DONE);
	}

	@Override
	public void configure(StateMachineTransitionConfigurer<Tasks, Events> transitions) throws Exception {
		logger.info("public void configure(StateMachineTransitionConfigurer<States, Events> transitions)");
        transitions
        .withExternal()
            .source(Tasks.NOT_STARTED)
            .target(Tasks.WORKING)
            .event(Events.WORK)
        .and()
        .withExternal()
            .source(Tasks.WORKING)
            .target(Tasks.DONE)
            .event(Events.END)
        .and()
        .withExternal()
            .source(Tasks.WORKING)
            .target(Tasks.FAILING)
            .event(Events.FAIL)
            .guardExpression("extendedState.variables.get('global_tries')<'int:3'")
        .and()
        .withExternal()
            .source(Tasks.WORKING)
            .target(Tasks.PENDING)
            .event(Events.FAIL)
            .guardExpression("extendedState.variables.get('global_tries')>='int:3'")
        .and()
        .withExternal()
            .source(Tasks.PENDING)
            .target(Tasks.WORKING)
            .event(Events.WORK)
        .and()
        .withExternal()
            .source(Tasks.FAILING)
            .target(Tasks.WORKING)
            .event(Events.TRY_AGAIN)
        .and()
        .withInternal()
        	.source(Tasks.FAILING)
        	.action(tryAgain())
        	.timerOnce(3000);
	}
    
    /**
     * Reinicia a quantidade de tentativas
     * @return Ação que reinicia a quantidade de tentativas
     */
    @Bean
    public Action<Tasks, Events> resetTries() {
    	return new Action<Tasks, Events>() {
			
			@Override
			public void execute(StateContext<Tasks, Events> context) {
				Utils.setGlobalInteger(context, "tries", 0);
				logger.info("Reiniciando tentativas [STM: " + context.getStateMachine().getId() + "]");
			}
		};
    }
    
    /**
     * Definição de ação que contabiliza uma nova tentativa
     * @return Ação que contabiliza uma nova tentativa
     */
    @Bean
    public Action<Tasks, Events> increaseTries() {
    	return new Action<Tasks, Events>() {
			
			@Override
			public void execute(StateContext<Tasks, Events> context) {
				if(Utils.globalContainsKey(context, "tries") && Utils.getGlobalInteger(context, "tries") != null) {
					Utils.setGlobalInteger(context, "tries", Utils.getGlobalInteger(context, "tries") + 1);
				} else {
					Utils.setGlobalInteger(context, "tries", 1);
				}
				logger.info("Registranto tentativa " + Utils.getGlobalInteger(context, "tries") + " [STM: " + context.getStateMachine().getId() + "]");
			}
		};
    }

    @Bean
    public Action<Tasks, Events> tryAgain() {
    	return new Action<Tasks, Events>() {
			
			@Override
			public void execute(StateContext<Tasks, Events> context) {
				logger.info("Tentando mais uma vez... [STM: " + context.getStateMachine().getId() + "]");
				context.getStateMachine().sendEvent(Events.TRY_AGAIN);
			}
		};
    }    

}
