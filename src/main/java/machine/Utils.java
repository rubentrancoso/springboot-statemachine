package machine;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachineContext;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

public class Utils {

	public static Message<Events> buildMessage(Events event, Map<String, String> inputData) {
		MessageBuilder<Events> builder = MessageBuilder.withPayload(event);
		Iterator<Entry<String, String>> it = inputData.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, String> pair = it.next();
			builder.setHeader("input_" + pair.getKey(), pair.getValue());
		}
		Message<Events> message = builder.build();
		return message;
	}

	public static Message<Events> buildMessage(Events event) {
		MessageBuilder<Events> builder = MessageBuilder.withPayload(event);
		Message<Events> message = builder.build();
		return message;
	}

	public static String nameFactory(Random rng, String characters, int length) {
		char[] text = new char[length];
		for (int i = 0; i < length; i++) {
			text[i] = characters.charAt(rng.nextInt(characters.length()));
		}
		return new String(text);
	}

	public static Tasks stateFactory() {
		Random rng = new Random();
		return Tasks.values()[rng.nextInt(Tasks.values().length)];
	}

	public static String buildJson(Object object) {
		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		String json = "";
		try {
			json = ow.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return json;
	}

	public static void showInput(Message<Events> message) {
		if (message != null) {
			MessageHeaders heders = message.getHeaders();
			Set<String> keys = heders.keySet();
			for (String key : keys) {
				if (key.startsWith("input_"))
					System.out.println("key: " + key + "/value: " + heders.get(key));
			}
		}
	}

	public static void showGlobals(StateContext<Tasks, Events> stateContext) {
		Map<Object, Object> variables = stateContext.getExtendedState().getVariables();
		Iterator<Map.Entry<Object, Object>> it = variables.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<Object, Object> pair = (Map.Entry<Object, Object>) it.next();
			if (((String) pair.getKey()).startsWith("global_"))
				System.out.println("key: " + pair.getKey() + "/value: " + pair.getValue());
		}
	}

	public static boolean globalContainsKey(StateContext<Tasks, Events> context, String key) {
		return context.getExtendedState().getVariables().containsKey("global_" + key);
	}

	public static void setGlobalInteger(StateContext<Tasks, Events> context, String key, Integer value) {
		context.getExtendedState().getVariables().put("global_" + key, "int:" + value.toString());
	}

	public static void setGlobalBoolean(StateContext<Tasks, Events> context, String key, Boolean value) {
		context.getExtendedState().getVariables().put("global_" + key, "boolean:" + value.toString());
	}

	public static void setGlobalString(StateContext<Tasks, Events> context, String key, String value) {
		context.getExtendedState().getVariables().put("global_" + key, "int:" + value);
	}

	public static Integer getGlobalInteger(StateContext<Tasks, Events> context, String key) {
		String value = ((String) context.getExtendedState().getVariables().get("global_" + key)).substring(4);
		return Integer.parseInt(value);
	}

	public static Boolean getGlobalBoolean(StateContext<Tasks, Events> context, String key) {
		String value = ((String) context.getExtendedState().getVariables().get("global_" + key)).substring(8);
		return Boolean.getBoolean(value);
	}

	public static String getGlobalString(StateContext<Tasks, Events> context, String key) {
		String value = ((String) context.getExtendedState().getVariables().get("global_" + key)).substring(7);
		return value;
	}

	public static <E, S> Map<String, String> getGlobalsHashMap(StateMachineContext<S, E> context) {
		// retrieve all entries back to process
		HashMap<String, String> finalGlobals = new HashMap<String, String>();

		Map<Object, Object> variables = context.getExtendedState().getVariables();
		Iterator<Map.Entry<Object, Object>> it3 = variables.entrySet().iterator();
		while (it3.hasNext()) {
			Map.Entry<Object, Object> pair = (Map.Entry<Object, Object>) it3.next();
			String prefixed_key = (String) pair.getKey();
			if (prefixed_key.startsWith("global_")) {
				String key = prefixed_key.substring(7);
				finalGlobals.put(key, (String) pair.getValue());
			}
		}
		return finalGlobals;
	}

}
