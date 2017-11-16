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
package app;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.persist.StateMachinePersister;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import entities.Process;
import entities.ProcessRepository;
import machine.Events;
import machine.Tasks;
import machine.Utils;

@Controller
public class StateMachineController {

	private static final Logger logger = LoggerFactory.getLogger(StateMachineController.class);

	@Autowired
	private StateMachine<Tasks, Events> stateMachine;

	@Autowired
	private StateMachinePersister<Tasks, Events, String> stateMachinePersister;

	@Autowired
	ProcessRepository processRepository;
	
	HashMap<Tasks, Events> defaultStep = new HashMap<Tasks, Events>();
	Multimap<Tasks, Events> alternateStep = ArrayListMultimap.create();

	StateMachineController() {
		defaultStep.put(Tasks.NOT_STARTED, Events.WORK);
		defaultStep.put(Tasks.WORKING, Events.END);
		defaultStep.put(Tasks.FAILING, Events.TRY_AGAIN);
		defaultStep.put(Tasks.PENDING, Events.WORK);
		
		alternateStep.put(Tasks.WORKING, Events.FAIL);
	}
	
	@RequestMapping("/")
	String home() {
		return "index.html";
	}

	@RequestMapping(value="/process/{processId}", method=RequestMethod.GET)
	@ResponseBody
	String getProcess(@PathVariable("processId") Integer processId) {
		Process process = processRepository.findById(processId);
		return Utils.buildJson(process);
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
	
	@RequestMapping(value="/process/{processId}", method=RequestMethod.POST)
	@ResponseBody
	String executeProcess(@PathVariable("processId") Integer processId, @RequestBody Process process) {
		logger.info("submetendo evento");
		Process result = processRepository.findByName(process.getName());
		if(result != null) {
			try {
				resetStateMachineFromStore(process);
				Events sentEvent = process.getEvent();
				Events nextEvent = getNextEvent(process.getState());
				if(sentEvent != null)
					nextEvent = sentEvent;
				feedMachine(processId, Utils.buildMessage(nextEvent, process.getInput()));
				result = processRepository.findById(processId);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return Utils.buildJson(result);
	}
	
	private Events getNextEvent(Tasks tasks) {
		return defaultStep.get(tasks);
	}
	
	@RequestMapping(value="/process/{processId}", method=RequestMethod.DELETE)
	@ResponseBody
	void deleteProcess(@PathVariable("processId") Integer processId) {
		processRepository.delete(processId);
	}	
	
	@RequestMapping(value="/process", method=RequestMethod.POST)
	@ResponseBody
	String addProcess(@RequestBody Process process) {
		Page<Process> page;
		Process result = processRepository.findByName(process.getName());
		if(result == null) {
			result = processRepository.save(process);
		}
		page = processRepository.findAll(new PageRequest(0, 1));
		return Utils.buildJson(page);
	}

	@RequestMapping("/process")
	@ResponseBody
	String paginateAll(@RequestParam(value="page", defaultValue="0") Integer page, @RequestParam(value="size", defaultValue="10") Integer size, @RequestParam(value="state", defaultValue="") String state) {
		Page<Process> list;
		if(state.isEmpty()) {
			list = processRepository.findAll(new PageRequest(page, size));
		} else {
			list = processRepository.findByState(new PageRequest(page, size), Tasks.valueOf(state));
		}
		return Utils.buildJson(list);
	}

	@RequestMapping(value="/task", method=RequestMethod.GET)
	@ResponseBody
	String listTasks() {
		return Utils.buildJson(Tasks.values());
	}

	// Obtém os eventos alternativos para uma task
	@RequestMapping(value="/events/{fromstate}", method=RequestMethod.GET)
	@ResponseBody
	String listEvents(@PathVariable("fromstate") String fromstate) {
		try {
			Tasks.valueOf(fromstate); // checa se o valor existe
			Events event = defaultStep.get(Tasks.valueOf(fromstate));
		    Collection<Events> alternates = alternateStep.get(Tasks.valueOf(fromstate));
		    List<Events> list = new ArrayList<Events>();
		    if(event != null) {
		    	list = new ArrayList<Events>(alternates);
		    	list.add(0, event);
		    }
			return Utils.buildJson(list);
		} catch (Exception e) {
			return Utils.buildJson(new String[0]);
		}
	}

	
	private void feedMachine(Integer processId, Message<Events> message) throws Exception {
		stateMachine.sendEvent(message);
		stateMachinePersister.persist(stateMachine, Integer.toString(processId));
	}

	private StateMachine<Tasks, Events> resetStateMachineFromStore(Process process) throws Exception {
		StateMachine<Tasks, Events> restoredStateMachine = stateMachinePersister.restore(stateMachine, Integer.toString(process.getId()));
		restoredStateMachine = overwriteGlobals(restoredStateMachine, process);
		return restoredStateMachine;
	}
	
	private StateMachine<Tasks, Events> overwriteGlobals(StateMachine<Tasks, Events> stateMachine, Process process) {
		Map<String, String> globals = process.getGlobals();

		// Remove all global entries
	    Map<Object, Object> variables = stateMachine.getExtendedState().getVariables();
	    Iterator<Map.Entry<Object,Object>> it2 = variables.entrySet().iterator();
	    while (it2.hasNext()) {
	        Map.Entry<Object,Object> pair = (Map.Entry<Object,Object>)it2.next();
	        String prefixed_key = (String)pair.getKey();
	        if(prefixed_key.startsWith("global_")) {
	        	variables.remove(pair.getKey());
	        }
	    }		
		
		// Add or merge new entries
		Iterator<Map.Entry<String,String>> it1 = globals.entrySet().iterator();
	    while (it1.hasNext()) {
	        Map.Entry<String,String> pair = (Map.Entry<String,String>)it1.next();
	        stateMachine.getExtendedState().getVariables().put("global_" + pair.getKey(), pair.getValue());
	    }

		return stateMachine;
	}

}
