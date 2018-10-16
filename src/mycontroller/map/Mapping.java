package mycontroller.map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import tiles.LavaTrap;
import tiles.MapTile;
import tiles.MudTrap;
import utilities.Coordinate;

/**
 * Mapping expresses the current state of the map that has been explored,
 * keeping track of the relevant features that the map has
 * @author jordanung
 *
 */

public class Mapping {
	private HashMap<Coordinate, Integer> keys;
	private ArrayList<Coordinate> deadEnds;
	
	public Mapping() {
		keys = new HashMap<>();
		deadEnds = new ArrayList<>();
	}
	
	public HashMap<Coordinate, Integer> getKeysSeen() {
		return keys;
	}
	
	public ArrayList<Coordinate> getDeadEnds() {
		return deadEnds;
	}
	
	public void addFoundKey(Coordinate coordinate, Integer value) {
		keys.put(coordinate, value);
	}
	
	public void addDeadEnd(Coordinate coordinate) {
		deadEnds.add(coordinate);
	}
	
	public void articulateViewPoint(HashMap<Coordinate, MapTile> currentView) {
		// Iterate through all the tiles that the car can currently see
        for (Map.Entry<Coordinate, MapTile> mapInfo : currentView.entrySet()) {
			
        		// Check every lava tile for any keys within the tile
        		if (mapInfo.getValue() instanceof LavaTrap) {
				LavaTrap lavaTrap = (LavaTrap) mapInfo.getValue();
				
				// A key exists within the lava
				if (lavaTrap.getKey() > 0 && !keys.containsKey(mapInfo.getKey())) {
					keys.put(mapInfo.getKey(), lavaTrap.getKey());
				}
			}
        		
        		// Found a Mud Trap i.e. game over when traversed over
        		if (mapInfo.getValue() instanceof MudTrap && !deadEnds.contains(mapInfo.getKey())) {
        			deadEnds.add(mapInfo.getKey());
        		}
		}
        
        // Print location and key #
        for (Map.Entry<Coordinate, Integer> key : keys.entrySet()) {
        		System.out.println("The key " + key.getValue() + " is at " + key.getKey());
        }
        System.out.println("---------------All Keys Processed---------------");
        
        // Print location of dead ends
        for (Coordinate coordinate : deadEnds) {
        		System.out.println("The coordinate " + coordinate + " is a dead end!");
        }
        System.out.println("---------------All Dead Ends Processed---------------");
	}
}
