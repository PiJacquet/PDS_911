package shs.simulator;

import java.util.ArrayList;
import java.util.List;

public abstract class CategoryObject implements Runnable{

	protected String referencedName;
	protected List<String> listObjects;

	public CategoryObject(String referencedName, List<String> listObjects) {
		this.referencedName=referencedName;
		this.listObjects=listObjects;
	}
	
	public CategoryObject() {
		referencedName="Unknow";
		listObjects= new ArrayList<String>();
	}	

	private Integer getRandomID() throws InterruptedException {
		if(listObjects.isEmpty())
			throw new InterruptedException();
		// Choice of the sensor
		int id = (int)(Math.random()*listObjects.size());
		String activeSensor = listObjects.get(id);
		System.out.println("A new report was generated by a '" + referencedName + "' sensor (ID " + listObjects.get(id) + ")");
		// Transmission of the call
		return Integer.valueOf(activeSensor);
	}

	public abstract void launchAlert(Integer id);
	
	public void run() {
		try {
			while(true) {
				int id = getRandomID();
				launchAlert(id);
				// Delay
				Thread.sleep((long) (DataConfigSimu.getMIN_TIME_AUTO() + 
						Math.random()*(DataConfigSimu.getMAX_TIME_AUTO()-DataConfigSimu.getMIN_TIME_AUTO())));
			}
		} catch (InterruptedException e) {
			System.out.println("There is no '" + referencedName + "' available");
		}

	}

}
