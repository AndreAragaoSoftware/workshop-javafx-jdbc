package model.services;

import java.util.List;

import model.dao.DaoFactory;
import model.dao.DepartmentDao;
import model.entities.Department;

public class DepartmentService {

	private DepartmentDao dao = DaoFactory.createDepartmentDao();
	
	public List<Department> findAll() {
		return dao.findAll();
	}
	
	public void saveOrUpdate(Department obj) {
		// testando se id é igual a null
		// se for é pq o obj é novo e precisa ser inserido
		if (obj.getId() == null) {
			dao.insert(obj);
		}
		// caso contrario vai fazer um up date 
		else {
			dao.update(obj);
		}
	}
	
	// metodo para deletar o departamento
	public void remove(Department obj) {
		dao.deleteById(obj.getId());
	}
}
