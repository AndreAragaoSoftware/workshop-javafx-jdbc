package model.services;

import java.util.List;

import model.dao.DaoFactory;
import model.dao.SellerDao;
import model.entities.Seller;

public class SellerService {

	private SellerDao dao = DaoFactory.createSellerDao();
	
	public List<Seller> findAll() {
		return dao.findAll();
	}
	
	public void saveOrUpdate(Seller obj) {
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
	public void remove(Seller obj) {
		dao.deleteById(obj.getId());
	}
}
