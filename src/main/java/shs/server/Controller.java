package shs.server;


import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import shs.common.Message;
import shs.common.MessageType;
import shs.common.MsgAddObject;
import shs.common.MsgBooleanResult;
import shs.common.MsgConnection;
import shs.common.MsgDeleteObject;
import shs.common.MsgIntResult;
import shs.common.MsgListObject;
import shs.common.MsgUpdateObject;
import shs.common.Tool;

public class Controller {

	/***
	 *  Initialize poolConnection Object
	 */
	private JDBCConnectionPool connectionPool;
	private Connection connection;

	/**
	 * Constructor
	 */
	public Controller(JDBCConnectionPool connectionPool) {
		this.connectionPool = connectionPool;
		this.connection = connectionPool.getConnection();
	}


	public void closeController() {
		connectionPool.closeConnection(connection);
	}

	/**
	 * 
	 * @param request
	 * @return
	 */
	public String treatmentRequest(String request) {
		Message input = Tool.jsonToMessage(request);
		Boolean resultBoolean;
		Integer resultInteger;
		List<List<String>> resultList; 
		switch(input.getType()) {
		case PING :
			return Tool.messageToJSON(new Message(MessageType.PING));
		case CONNECTION : 
			resultBoolean = connection(((MsgConnection)input).getUsername(), ((MsgConnection)input).getPassword());
			MsgBooleanResult answer1 = new MsgBooleanResult(resultBoolean);
			return Tool.messageToJSON(answer1);
		case ADDOBJECT :
			resultList = addObject(((MsgAddObject)input).getObject());
			MsgListObject answer2 = new MsgListObject(resultList); 
			return Tool.messageToJSON(answer2);
		case NUMBEROBJECT :
			resultInteger = nbObject();
			MsgIntResult answer3 = new MsgIntResult(resultInteger);
			return Tool.messageToJSON(answer3);
		case LISTOBJECT : 
			resultList = listObject(); 
			MsgListObject answer4 = new MsgListObject(resultList); 
			return Tool.messageToJSON(answer4);
		case DELETEOBJECT : 
			resultList = deleteObeject(((MsgDeleteObject)input).getObject()); 
			MsgListObject answer5 = new MsgListObject(resultList); 
			return Tool.messageToJSON(answer5);
		case UPDATEOBJECT : 
			resultBoolean = updateObject(((MsgUpdateObject)input).getObject()); 
			MsgBooleanResult answer6 = new MsgBooleanResult(resultBoolean); 
			return Tool.messageToJSON(answer6);
		case LISTZONES :
			resultList = listZones();
			MsgListObject answer7 = new MsgListObject(resultList); 
			return Tool.messageToJSON(answer7);
		case LISTPIECES :
			resultList = listPieces();
			MsgListObject answer8 = new MsgListObject(resultList); 
			return Tool.messageToJSON(answer8);
		case LISTRESIDENCES :
			resultList = listResidences();
			MsgListObject answer9 = new MsgListObject(resultList); 
			return Tool.messageToJSON(answer9);
		case LISTREFERENTIELS :
			resultList = listReferentiels();
			MsgListObject answer10 = new MsgListObject(resultList); 
			return Tool.messageToJSON(answer10);
		default:
			Tool.logger.info("#Error : Controller > treatmentRequest : Unknow request " + request);
			return "";
		}	
	}

	/**
	 * Check connection. 
	 * 
	 * @param identifiant
	 * @param password
	 * @return
	 */
	private boolean connection(String identifiant, String password) {

		String request = "Select count(*) from Personnel where Identifiant_Personnel='" + identifiant + "' and MotDePasse_Personnel = '" + password + "'"; 
		
		Tool.logger.info("Connection - Controller");
		

		try {
			Statement statement = connection.createStatement();
			ResultSet resultat = statement.executeQuery(request);
			resultat.next(); 
			if (resultat.getInt(1)==1) {
				Tool.logger.info("Connection SUCCEED");

				return true; 
			}
			else {
				Tool.logger.info("Connection FAILED");

				return false; 
			}
		}catch (SQLException e) {
			Tool.logger.info("Connection FAILED - SQL EXCEPTION");
			System.out.println(request);
			e.printStackTrace();

			return false; 
		}
	}

	/**
	 * Return the object number associate to the account.
	 * 
	 * @return
	 */
	private int nbObject() {
		String request = "Select count(*) from Capteurs";  


		try {
			Statement statement = connection.createStatement();
			ResultSet resultat = statement.executeQuery(request);
			resultat.next(); 
			Tool.logger.info("nbObject SUCCEED");

			return resultat.getInt(1); 

		}catch (SQLException e) {
			Tool.logger.info("nbObject FAILED - SQL EXCEPTION");

			return 0; 
		}
	}

	/**
	 * Add an object to the base.
	 * 
	 * @param typeCapteur
	 * @return
	 */
	private List<List<String>> addObject(String typeCapteur) {
		String request = "INSERT INTO Capteurs (Type_Capteur, Etat_Capteur, ID_Emplacement) VALUES ('"+ typeCapteur +"', 1, 1)"; 

		try {
			Statement statement = connection.createStatement();
			statement.executeUpdate(request);
			Tool.logger.info("addObject SUCCEED");

			return listObject(); 

		}catch (SQLException e) {
			Tool.logger.info("addObject FAILED - SQL EXCEPTION : " + request);
			e.printStackTrace();

			return listObject(); 
		}
	}

	/**
	 * 
	 * @param idCapteur
	 * @return
	 */
	private List<List<String>> deleteObeject(String idCapteur) {
		String request = "DELETE FROM Capteurs WHERE ID_Capteur ='" +idCapteur + "'";
		try {
			Statement statement = connection.createStatement(); 
			statement.executeUpdate(request); 
			Tool.logger.info("deleteObject SUCCED");
			return listObject(); 
		}catch(SQLException e) {
			Tool.logger.info("deleteObject FAILED - SQL EXCEPTION : " + request);
			e.printStackTrace();
			return listObject(); 
		}
	}

	/**
	 * 
	 * @param attribute
	 * @return
	 */
	private boolean updateObject(List<String> attribute) {
		String requestEmplacement = "SELECT ID_Emplacement FROM Emplacement INNER JOIN Residences ON Residences.ID_Residence=Emplacement.ID_Residence "
				+ "					WHERE Nom_Residence='" + attribute.get(3) + "' AND Zone_Emplacement='" + attribute.get(4) + "' AND Piece_Emplacement='" + attribute.get(5) + "'";
		String request = "";
		try {
			Statement statement = connection.createStatement(); 
			ResultSet resultEmplacement = statement.executeQuery(requestEmplacement); 
			if(!resultEmplacement.next()) {
				// The "Emplacement" doesn't exit, we have to create it
				// We get the Residence ID
				String requestResidence = "SELECT ID_Residence FROM Residences WHERE Nom_Residence='" + attribute.get(3) + "'";
				ResultSet resultResidence = statement.executeQuery(requestResidence); 
				resultResidence.next();
				int id = resultResidence.getInt(1);
				// We create the "Emplacement" with the Residence ID
				String createEmplacement = "INSERT INTO Emplacement(Piece_Emplacement, Zone_Emplacement, ID_Residence) VALUES('" +attribute.get(5) + "', '" + attribute.get(4)+ "', '" + id + "')" ;
				statement.executeUpdate(createEmplacement); 
				// We get the ID from the "Emplacement" we just created
				String requestEmplacementNew = "SELECT ID_Emplacement FROM Emplacement WHERE Zone_Emplacement='" + attribute.get(4) + "' AND Piece_Emplacement='" + attribute.get(5) + "'"
												+ " AND ID_Residence='" + id + "'";
				ResultSet resultEmplacementNew = statement.executeQuery(requestEmplacementNew); 
				resultEmplacementNew.next();
				int idNew = resultEmplacementNew.getInt(1);
				request = "UPDATE Capteurs SET Type_Capteur = '"+ attribute.get(1) +"', Etat_Capteur = '"+ attribute.get(2) + "', ID_Emplacement = '" + idNew + "' WHERE ID_Capteur ='"+ attribute.get(0)+"'";
			}
			else {
				request = "UPDATE Capteurs SET Type_Capteur = '"+ attribute.get(1) +"', Etat_Capteur = '"+ attribute.get(2) + "', ID_Emplacement = '" + resultEmplacement.getInt(1) + "' WHERE ID_Capteur ='"+ attribute.get(0)+"'";
			}
			
			statement.executeUpdate(request); 
			return true; 
		}catch(SQLException e) {
			Tool.logger.info("updateObject FAILED - SQL EXCEPTION : " + request);
			e.printStackTrace();
			return false; 
		}

	}

	/**
	 * getList()
	 * @return a list given by the select request
	 */

	private List<List<String>> getList(String sql){
		List<List<String>> list = new ArrayList<List<String>>();

		try {
			Statement statement = connection.createStatement(); 
			ResultSet result = statement.executeQuery(sql); 
			ResultSetMetaData resultMetada = result.getMetaData(); 
			while(result.next()) {
				List<String> record = new ArrayList<String>(); 
				for (int i = 1; i<= resultMetada.getColumnCount(); i++) {
					record.add(result.getString(i)); 
				}
				list.add(record); 
			}

			return list; 
		}catch (SQLException e) {
			Tool.logger.info("getList FAILED - SQL EXCEPTION :\n " + sql);
			e.printStackTrace();
			return null; 
		}
	}

	private List<List<String>> listObject(){
		String request = "SELECT ID_Capteur, Type_Capteur, Etat_Capteur, Nom_Residence, "
				+ "Zone_emplacement, Piece_Emplacement FROM Capteurs INNER JOIN Emplacement ON "
				+ "Capteurs.ID_Emplacement=Emplacement.ID_Emplacement INNER JOIN Residences ON "
				+ "Emplacement.ID_Residence = Residences.ID_Residence;"; 
		return getList(request);

	}


	private List<List<String>> listResidences() {
		String request = "SELECT * FROM Residences INNER JOIN Adresse ON Residences.ID_Addresse=Adresse.ID_Addresse;"; 
		return getList(request);
	}


	private List<List<String>> listPieces() {
		String request = "SELECT DISTINCT Piece_Emplacement FROM Emplacement;";
		return getList(request);
	}


	private List<List<String>> listZones() {
		String request = "SELECT DISTINCT Zone_Emplacement FROM Emplacement;";
		return getList(request);
	}
	
	private List<List<String>> listReferentiels(){
		String request = "SELECT Type_Capteur FROM Referentiel_Capteurs;";
		return getList(request);
	}

}
