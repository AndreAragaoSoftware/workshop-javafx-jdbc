package gui;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import application.Main;
import db.DbIntegrityException;
import gui.listeners.DataChangeListener;
import gui.util.Alerts;
import gui.util.Utils;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.entities.Seller;
import model.services.SellerService;

public class SellerListController implements Initializable, DataChangeListener {

	private SellerService service;

	// referenciando a tableView com o departamento
	@FXML
	private TableView<Seller> tableViewSeller;

	// referenciando a coluna id com o department. O Integer é porque vai puxa o Id
	@FXML
	private TableColumn<Seller, Integer> tableColumnId;

	@FXML
	private TableColumn<Seller, String> tableColumnName;

	@FXML
	private TableColumn<Seller, Seller> tableColumnEDIT;
	
	@FXML
	private TableColumn<Seller, String> tableColumnEmail;
	
	@FXML
	private TableColumn<Seller, Date> tableColumnBirthDate;
	
	@FXML
	private TableColumn<Seller, Double> tableColumnBaseSalary;

	@FXML
	private TableColumn<Seller, Seller> tableColumnREMOVE;

	@FXML
	private Button btNew;

	private ObservableList<Seller> obsList;

	@FXML
	public void onBtNewAction(ActionEvent event) {
		Stage parentStage = Utils.currentStage(event);
		Seller obj = new Seller();
		createDialogForm(obj, "/gui/SellerForm.fxml", parentStage);
	}

	public void setSellerService(SellerService service) {
		this.service = service;
	}

	@Override
	public void initialize(URL url, ResourceBundle rb) {
		initializeNodes();
	}

	private void initializeNodes() {
		// padrão para iniciar o comportamento das colunas
		// O nome entre parentese tem que esta igual ao atributo do pacote model.entities
		tableColumnId.setCellValueFactory(new PropertyValueFactory<>("id"));
		tableColumnName.setCellValueFactory(new PropertyValueFactory<>("name"));
		tableColumnEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
		tableColumnBirthDate.setCellValueFactory(new PropertyValueFactory<>("birthDate"));
		//metodo criado na pasta Utils para formata data
		Utils.formatTableColumnDate(tableColumnBirthDate, "dd/MM/yyyy");
		tableColumnBaseSalary.setCellValueFactory(new PropertyValueFactory<>("baseSalary"));
		//metodo criado na pasta Utils para formata numeros double/ o 2 significa as casas decimais
		Utils.formatTableColumnDouble(tableColumnBaseSalary, 2);

		// macete para que a tableView acompanhe a janela
		Stage stage = (Stage) Main.getMainScene().getWindow();
		tableViewSeller.prefHeightProperty().bind(stage.heightProperty());

	}

	// metodo responsavel por pegar os serviços e jogar na obsList
	public void updateTableView() {
		// se o programador esquecer de instanciar o service
		if (service == null) {
			throw new IllegalStateException("Sevice was null");
		}
		// list vai receber todos os dados do findAll
		List<Seller> list = service.findAll();
		obsList = FXCollections.observableArrayList(list);
		// pega os items e joga na tela
		tableViewSeller.setItems(obsList);
		// chamando o metodo de atulização dos botões
		initEditButtons();
		// chamando o metodo de remoção 
		initRemoveButtons();
	}

	private void createDialogForm(Seller obj, String absoluteName, Stage parentStage) {
		try {
			// Instanciando FXMLLoader para poder abrir nova tela
			FXMLLoader loader = new FXMLLoader(getClass().getResource(absoluteName));
			// carregando o pane
			Pane pane = loader.load();

			// pegou o controller da tela que foi carregada acima
			SellerFormController controller = loader.getController();
			// setando o comtrolador
			controller.setSeller(obj);
			// setando o SellerService
			controller.setSellerService(new SellerService());
			// evento que faz a atualização da lista quando adcionado um novo departamento
			controller.subscribeDataChangeListener(this);
			// carregar o obj no formulario
			controller.upDateFormData();

			Stage dialogStage = new Stage();
			dialogStage.setTitle("Enter Seller data");
			dialogStage.setScene(new Scene(pane));
			// não pode ser rederizada
			dialogStage.setResizable(false);
			// chamando o palco
			dialogStage.initOwner(parentStage);
			// só pode fazer outra coisa se for fechada
			dialogStage.initModality(Modality.WINDOW_MODAL);
			dialogStage.showAndWait();

		} catch (IOException e) {
			Alerts.showAlert("IO Exception", "Error loading view", e.getMessage(), AlertType.ERROR);
		}
	}

	@Override
	public void onDataChanged() {
		// Aparti do momento que esse evento for disparado ele vai atualizar
		updateTableView();
	}

	// metodo para colocar os botões de atualização
	private void initEditButtons() {
		tableColumnEDIT.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
		tableColumnEDIT.setCellFactory(param -> new TableCell<Seller, Seller>() {
			private final Button button = new Button("edit");

			@Override
			protected void updateItem(Seller obj, boolean empty) {
				super.updateItem(obj, empty);
				if (obj == null) {
					setGraphic(null);
					return;
				}
				setGraphic(button);
				button.setOnAction(
				event -> createDialogForm(obj, "/gui/SellerForm.fxml", Utils.currentStage(event)));
			}
		});
	}

	private void initRemoveButtons() {
		tableColumnREMOVE.setCellValueFactory(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
		tableColumnREMOVE.setCellFactory(param -> new TableCell<Seller, Seller>() {
			private final Button button = new Button("remove");

			@Override
			protected void updateItem(Seller obj, boolean empty) {
				super.updateItem(obj, empty);
				if (obj == null) {
					setGraphic(null);
					return;
				}
				setGraphic(button);
				button.setOnAction(event -> removeEntity(obj));
			}
		});
	}

	// esse metodo tem que ser criado fora do initRemoveButtons()
	private void removeEntity(Seller obj) {
		Optional<ButtonType> result = Alerts.showConfirmation("Confirmatio", "Are you sure to delete?");

		// comfirmação que o usuario apertou no botão ok
		if (result.get() == ButtonType.OK) {
			if (service == null) {
				throw new IllegalStateException("Service was null");
			}
			try {
				//excluindo o departamento
				service.remove(obj);
				//atualizando a tabela
				updateTableView();
			} catch (DbIntegrityException e) {
				Alerts.showAlert("Error removing objecrt", null, e.getMessage(), AlertType.ERROR);
			}

		}
	}

}
