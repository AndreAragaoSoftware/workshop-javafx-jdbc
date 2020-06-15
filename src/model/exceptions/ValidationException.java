package model.exceptions;

import java.util.HashMap;
import java.util.Map;

public class ValidationException extends RuntimeException {

	//Essa class carrega o erro que pode ser gerado em todo o DepartmentForm
	
	private static final long serialVersionUID = 1L;
	
	private Map<String, String> errors = new HashMap<>();
	

	public ValidationException(String msg) {
		super(msg);
	}
	
	public Map<String, String> getErrors(){
		return errors;
	}
	
	public void addError(String fielName, String errorMessage) {
		errors.put(fielName, errorMessage);
	}
}
