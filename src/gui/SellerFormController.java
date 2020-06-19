package gui;

import java.net.URL;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import db.DbException;
import gui.listeners.DataChangeListener;
import gui.util.Alerts;
import gui.util.Constraints;
import gui.util.Utils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.util.Callback;
import model.entities.Department;
import model.entities.Seller;
import model.exceptions.ValidationException;
import model.services.DepartmentService;
import model.services.SellerService;

public class SellerFormController implements Initializable {

	// criando uma dependecia
	private Seller entity;

	private SellerService service;

	private DepartmentService departmentService;

	// intanciando lista de atualiza��o do departamento
	private List<DataChangeListener> dataChangeListeners = new ArrayList<>();

	@FXML
	private TextField txtId;
	@FXML
	private TextField txtName;
	@FXML
	private TextField txtEmail;
	// O DatePicker foi criado no Utils.java
	@FXML
	private DatePicker dpBirthDate;
	@FXML
	private TextField txtBaseSalary;
	@FXML
	private ComboBox<Department> comboBoxDepartment;
	@FXML
	private Label labelErrorName;
	@FXML
	private Label labelErrorEmail;
	@FXML
	private Label labelErrorBirthDate;
	@FXML
	private Label labelErrorBaseSalary;
	@FXML
	private Button btSave;
	@FXML
	private Button btCancel;
	// lista criada para a comboBox
	@FXML
	private ObservableList<Department> obsList;

	public void setSeller(Seller entity) {
		this.entity = entity;
	}

	public void setSellerServices(SellerService service, DepartmentService departmentService) {
		this.service = service;
		this.departmentService = departmentService;

	}

	// sobrecrever a lista
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
		} catch (ValidationException e) {
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

		// instanciando a exception
		ValidationException exception = new ValidationException("Validation error");

		// pegando o texto da caixa txtId
		// foi utilizado o metodo tryParse para tranformar em int
		obj.setId(Utils.tryParseToInt(txtId.getText()));

		// o .trim() serve para eliminar os espa�os em branco
		if (txtName.getText() == null || txtName.getText().trim().equals("")) {
			// foi add um erro caso o campo estiver vazio
			exception.addError("name", "Fiel can't be empty");
		}
		obj.setName(txtName.getText());
		
		
		if (txtEmail.getText() == null || txtEmail.getText().trim().equals("")) {
			// foi add um erro caso o campo estiver vazio
			exception.addError("email", "Fiel can't be empty");
		}
		obj.setEmail(txtEmail.getText());
		
		if(dpBirthDate.getValue() == null) {
			exception.addError("birthDate", "Fiel can't be empty");
		}
		else {
		// pegando o valor q ta no datepiker
		Instant instant = Instant.from(dpBirthDate.getValue().atStartOfDay(ZoneId.systemDefault()));
		obj.setBirthDate(Date.from(instant));
		}
		
		if (txtBaseSalary.getText() == null || txtBaseSalary.getText().trim().equals("")) {
			// foi add um erro caso o campo estiver vazio
			exception.addError("baseSalary", "Fiel can't be empty");
		}
		//foi o usado o metodo try para converte o salario
		obj.setBaseSalary(Utils.tryParseToDouble(txtBaseSalary.getText()));;
		
		// relacionando o departamento
		obj.setDepartment(comboBoxDepartment.getValue());
		
		
		// testando pra ve se existe pelomenos um erro
		if (exception.getErrors().size() > 0) {
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

	// retrin��es
	private void initializeNodes() {
		// o txtId s� aceita numero Inteiro
		Constraints.setTextFieldInteger(txtId);
		// o txtName s� pode ter no maximo 30 caracters
		Constraints.setTextFieldMaxLength(txtName, 70);
		Constraints.setTextFieldDouble(txtBaseSalary);
		Constraints.setTextFieldMaxLength(txtEmail, 60);
		Utils.formatDatePicker(dpBirthDate, "dd/MM/yyyy");
		// metodo que inicializa o comboBox
		initializeComboBoxDepartment();
	}

	public void upDateFormData() {
		if (entity == null) {
			throw new IllegalStateException("Entity was null");
		}
		// valueOf foi pra converter o id para String
		txtId.setText(String.valueOf(entity.getId()));
		txtName.setText(entity.getName());
		txtEmail.setText(entity.getEmail());
		Locale.setDefault(Locale.US);
		txtBaseSalary.setText(String.format("%.2f", entity.getBaseSalary()));
		if (entity.getBirthDate() != null) {
			// Esse formato permite que o programa capture a data da maquina do usuario
			dpBirthDate.setValue(LocalDate.ofInstant(entity.getBirthDate().toInstant(), ZoneId.systemDefault()));
		}
		// testando de o vendedor � novo
		if (entity.getDepartment() == null) {
			// pega o primeiro elemento do comboBox
			comboBoxDepartment.getSelectionModel().selectFirst();
		} else {
			comboBoxDepartment.setValue(entity.getDepartment());
		}
	}

	// carregar os objetos associados a comboBox
	public void loadAssociatedObjects() {
		if (departmentService == null) {
			throw new IllegalAccessError("DepartmentServices was null");
		}
		List<Department> list = departmentService.findAll();
		obsList = FXCollections.observableArrayList(list);
		comboBoxDepartment.setItems(obsList);
	}

	// dando um set na labelErrorName caso exista um erro
	private void setErrorMessages(Map<String, String> errors) {
		Set<String> fields = errors.keySet();
		
		// testadando caso no campo name tiver vazio vai gerar um erro na label 
		// caso nao a label fica vazia
		labelErrorName.setText((fields.contains("name") ? errors.get("name") : ""));
		
		//pode ser feito dessa forma tambem
		if (fields.contains("email")) {
			labelErrorEmail.setText(errors.get("email"));
		}
		else {
			labelErrorEmail.setText("");
		}
		
		if (fields.contains("baseSalary")) {
			labelErrorBaseSalary.setText(errors.get("baseSalary"));
		}
		else {
			labelErrorBaseSalary.setText("");
		}
		if (fields.contains("birthDate")) {
			labelErrorBirthDate.setText(errors.get("birthDate"));
		}
		else {
			labelErrorBirthDate.setText("");
		}
		
	}

	private void initializeComboBoxDepartment() {
		Callback<ListView<Department>, ListCell<Department>> factory = lv -> new ListCell<Department>() {
			@Override
			protected void updateItem(Department item, boolean empty) {
				super.updateItem(item, empty);
				setText(empty ? "" : item.getName());
			}
		};
		comboBoxDepartment.setCellFactory(factory);
		comboBoxDepartment.setButtonCell(factory.call(null));
	}

}
