package persist;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.MessageHeaders;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachineContextRepository;
import org.springframework.statemachine.kryo.MessageHeadersSerializer;
import org.springframework.statemachine.kryo.StateMachineContextSerializer;
import org.springframework.statemachine.kryo.UUIDSerializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import entities.Process;
import entities.ProcessRepository;
import machine.Tasks;
import machine.Utils;

public class JpaStateMachineContextRepository<S, E> implements StateMachineContextRepository<S, E, StateMachineContext<S, E>> {

	private static final Logger logger = LoggerFactory.getLogger(JpaStateMachineContextRepository.class);
	
	ProcessRepository processRepository;
	
	private static final ThreadLocal<Kryo> kryoThreadLocal = new ThreadLocal<Kryo>() {

		@SuppressWarnings("rawtypes")
		@Override
		protected Kryo initialValue() {
			Kryo kryo = new Kryo();
			kryo.addDefaultSerializer(StateMachineContext.class, new StateMachineContextSerializer());
			kryo.addDefaultSerializer(MessageHeaders.class, new MessageHeadersSerializer());
			kryo.addDefaultSerializer(UUID.class, new UUIDSerializer());
			return kryo;
		}
	};

	public JpaStateMachineContextRepository(ProcessRepository processRepository) {
		this.processRepository = processRepository;
	}

	@Override
	public void save(StateMachineContext<S, E> context, String processId) {
		logger.info(" . save()");
		Process process = processRepository.findById(Integer.parseInt(processId));
		process.setContext(serialize(context));
		process.setGlobals(Utils.getGlobalsHashMap(context));
		process.setState(((Tasks)context.getState()));
		processRepository.save(process);
	}

	@Override
	public StateMachineContext<S, E> getContext(String processId) {
		logger.info(" . getContext("+ processId + ")");
		Process process = processRepository.findById(Integer.parseInt(processId));
		return deserialize(process.getContext());
	}

	private byte[] serialize(StateMachineContext<S, E> context) {
		Kryo kryo = kryoThreadLocal.get();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Output output = new Output(out);
		kryo.writeObject(output, context);
		output.close();
		return out.toByteArray();
	}

	@SuppressWarnings("unchecked")
	private StateMachineContext<S, E> deserialize(byte[] data) {
		if (data == null || data.length == 0) {
			return null;
		}
		Kryo kryo = kryoThreadLocal.get();
		ByteArrayInputStream in = new ByteArrayInputStream(data);
		Input input = new Input(in);
		return kryo.readObject(input, StateMachineContext.class);
	}

}
