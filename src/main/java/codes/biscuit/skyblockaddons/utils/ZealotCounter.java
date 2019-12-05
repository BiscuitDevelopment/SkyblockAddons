package codes.biscuit.skyblockaddons.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import codes.biscuit.skyblockaddons.SkyblockAddons;

public class ZealotCounter {
	
	private final static String FILENAME = "zealotkills.cfg";
	
	private SkyblockAddons main;
	private File zealotCounterFile;
	
	private int kills;
	
	public ZealotCounter(SkyblockAddons main, File configDir) {
		this.main = main;
		this.zealotCounterFile = new File(configDir.getAbsolutePath() + File.separator + FILENAME);
	}
	
	public void loadCounter() {
		if(zealotCounterFile.exists()) {
			try {
				FileReader reader = new FileReader(zealotCounterFile);
				BufferedReader bufferedReader = new BufferedReader(reader);
				String line = bufferedReader.readLine();
				
				kills = Integer.parseInt(line);
				bufferedReader.close();
				
			} catch (IOException | NumberFormatException ex) {
				ex.printStackTrace();
				System.out.println("SkyblockAddons: There was an error loading the zealot counter.");
				saveCounter();
			}
		} else {
			saveCounter();
		}
	}
	
	public void saveCounter() {
		try {
			zealotCounterFile.createNewFile();
			FileWriter writer = new FileWriter(zealotCounterFile);
			BufferedWriter bufferedWriter = new BufferedWriter(writer);
			
			bufferedWriter.write(Integer.toString(kills));
			bufferedWriter.close();
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println("SkyblockAddons: An error occurred while attempted to save the zealot counter!");
		}
	}
	
	public void addKill() {
		kills++;
		saveCounter();
	}
	
	public void setKills(int kills) {
		this.kills = kills;
		saveCounter();
	}
	
	public int getKills() {
		return kills;
	}

}
