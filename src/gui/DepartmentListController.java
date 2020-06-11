package gui;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import application.Main;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.entities.Department;
import model.services.DepartmentService;

public class DepartmentListController implements Initializable {
	
	private DepartmentService service;
	
	//referenciando a tableView com o departamento
	@FXML
	private TableView<Department> tableViewDepartment;
	
	//referenciando a coluna id com o department. O Integer é porque vai puxa o Id
	@FXML
	private TableColumn<Department, Integer> tableColumnId;
	
	@FXML
	private TableColumn<Department, String> tableColumnName;
	
	@FXML
	private Button btNew;
	
	private ObservableList<Department> obsList;
	
	
	@FXML
	public void onBtNewAction() {
		System.out.println("onBtNewAction");
	}
	
	public void setDepartmentService(DepartmentService service) {
		this.service = service;
	}
	
	@Override
	public void initialize(URL url, ResourceBundle rb) {
		initializeNodes();
	}

	private void initializeNodes() {
		//padrão para iniciar o comportamento das colunas
		tableColumnId.setCellValueFactory(new PropertyValueFactory<>("id"));
		tableColumnName.setCellValueFactory(new PropertyValueFactory<>("name"));
		
		//macete para que a tableView acompanhe a janela
		Stage stage = (Stage) Main.getMainScene().getWindow();
		tableViewDepartment.prefHeightProperty().bind(stage.heightProperty());
		
	}
	// metodo responsavel por pegar os serviços e jogar na obsList
	public void updateTableView() {
		// se o programador esquecer de instanciar o service
		if (service == null) {
			throw new IllegalStateException("Sevice was null");
		}
		// list vai receber todos os dados do findAll
		List<Department> list = service.findAll();
		obsList = FXCollections.observableArrayList(list);
		//pega os items e joga na tela
		tableViewDepartment.setItems(obsList);
	}

}
