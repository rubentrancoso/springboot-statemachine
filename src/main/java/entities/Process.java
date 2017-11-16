package entities;

import java.util.Map;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.MapKeyColumn;
import javax.persistence.Transient;

import machine.Events;
import machine.Tasks;

@Entity
public class Process {

	@Transient
	private Events event;
	
	@Column(name="ID")
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Integer id;

	private Tasks state;
	
	private String name;

	@ElementCollection
	@JoinTable(name="GLOBALS", joinColumns=@JoinColumn(name="ID"))
	@MapKeyColumn (name="RANGE_ID")
	@Column(name="VALUE")
	private Map<String, String> globals;
	
	@ElementCollection
	@JoinTable(name="INPUT", joinColumns=@JoinColumn(name="ID"))
	@MapKeyColumn (name="RANGE_ID")
	@Column(name="VALUE")	
	private Map<String, String> input;
	
	private byte[] context;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Tasks getState() {
		return state;
	}

	public void setState(Tasks state) {
		this.state = state;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setGlobals(Map<String, String> globals) {
		this.globals = globals;
	}	

	public Map<String, String> getGlobals() {
		return globals;
	}
	
	public void setInput(Map<String, String> input) {
		this.input = input;
	}	
	
	public Map<String, String> getInput() {
		return input;
	}

	public String toString() {
		return "[id=" + this.id + ", state=" + this.state + ", globals=" + this.globals + ", input=" + this.input + ", context=" + this.getContext() + "]";
	}

	public byte[] getContext() {
		return context;
	}

	public void setContext(byte[] context) {
		this.context = context;
	}

	public Events getEvent() {
		return event;
	}

	public void setEvent(Events event) {
		this.event = event;
	}	
	
}
