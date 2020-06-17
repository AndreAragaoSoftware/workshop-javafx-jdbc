package gui;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import db.DbException;
import gui.listeners.DataChangeListener;
import gui.util.Alerts;
import gui.util.Constraints;
import gui.util.Utils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import model.entities.Seller;
import model.exceptions.ValidationException;
import model.services.SellerService;

public class SellerFormController implements Initializable {

	// criando uma dependecia
	private Seller entity;

	private SellerService service;
	
	//intanciando lista de atualização do departamento
	private List<DataChangeListener> dataChangeListeners = new ArrayList<>();

	@FXML
	private TextField txtId;
	@FXML
	private TextField txtName;
	@FXML
	private Label labelErrorName;
	@FXML
	private Button btSave;
	@FXML
	private Button btCancel;

	public void setSeller(Seller entity) {
		this.entity = entity;
	}

	public void setSellerService(SellerService service) {
		this.service = service;
	}
	//sobrecrever a lista
	public void subscribeDataChangeListener(DataChangeListener listener) {
		dataChangeListeners.add(listener);
	}

	@FXML
	public void onBtSaveAction(ActionEvent event) {
		if (entity == null) {
			throw new IllegalStateException("Entity was null");
		}
		if (service == null) {
			throw new IllegalStateException("Service was null");
		}
		try {
			// o getFormData retorna o objeto pelo na tabela departmentForm.fxml
			entity = getFormData();
			// salvar no banco de dados
			service.saveOrUpdate(entity);
			notifyDataChangeListeners();
			// Fechar a janela
			Utils.currentStage(event).close();
			
		} catch (DbException e) {
			Alerts.showAlert("Error saving object", null, e.getMessage(), AlertType.ERROR);
		}catch(ValidationException e) {
			setErrorMessages(e.getErrors());
		}

	}
	// metodo para atulizar a lista de departamento
	private void notifyDataChangeListeners() {
		for (DataChangeListener listener : dataChangeListeners) {
			listener.onDataChanged();
		}
		
	}

	private Seller getFormData() {
		// instanciando o obj no Seller
		Seller obj = new Seller();
		
		//instanciando a exception
		ValidationException exception = new ValidationException("Validation error");

		// pegando o texto da caixa txtId
		// foi utilizado o metodo tryParse para tranformar em int
		obj.setId(Utils.tryParseToInt(txtId.getText()));
		
		// o .trim() serve para eliminar os espaços em branco
		if(txtName.getText() == null || txtName.getText().trim().equals("")) {
			//foi add um erro caso o campo estiver vazio
			exception.addError("name", "Fiel can't be empty");
		}
		obj.setName(txtName.getText());
		
		//testando pra ve se existe pelomenos um erro 
		if(exception.getErrors().size() > 0) {
			throw exception;
		}

		return obj;
	}

	@FXML
	public void onBtCancelAction(ActionEvent event) {
		// Fechar a janela
		Utils.currentStage(event).close();
	}

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		initializeNodes();
	}

	// retrinções
	private void initializeNodes() {
		// o txtId só aceita numero Inteiro
		Constraints.setTextFieldInteger(txtId);
		// o txtName só pode ter no maximo 30 caracters
		Constraints.setTextFieldMaxLength(txtName, 30);
	}

	public void upDateFormData() {
		if (entity == null) {
			throw new IllegalStateException("Entity was null");
		}
		// valueOf foi pra converter o id para String
		txtId.setText(String.valueOf(entity.getId()));
		txtName.setText(entity.getName());
	}
	
	//dando um set na labelErrorName caso exista um erro
	private void setErrorMessages(Map<String, String> errors) {
		Set<String> fields = errors.keySet();
		
		if(fields.contains("name")) {
			labelErrorName.setText(errors.get("name"));
		}
	}

}
