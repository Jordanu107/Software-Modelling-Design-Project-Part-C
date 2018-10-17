package mycontroller.map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import tiles.LavaTrap;
import tiles.MapTile;
import tiles.MapTile.Type;
import tiles.MudTrap;
import tiles.TrapTile;
import utilities.Coordinate;

/**
 * Mapping expresses the current state of the map that has been explored,
 * keeping track of the relevant features that the map has
 * @author jordanung
 *
 */

public class Mapping {
	private HashMap<Coordinate, Integer> keys;
	private HashMap<Coordinate, String> pointsOfInterest;
	private ArrayList<Coordinate> deadEnds;
	
	public Mapping() {
		keys = new HashMap<>();
		pointsOfInterest = new HashMap<>();
		deadEnds = new ArrayList<>();
	}
	
	public HashMap<Coordinate, Integer> getKeysSeen() {
		return keys;
	}
	
	public HashMap<Coordinate, String> getPointsOfInterest() {
		return pointsOfInterest;
	}
	
	public void addPointOfInterest(Coordinate coordinate, String type) {
		pointsOfInterest.put(coordinate, type);
	}
	
	public void articulateViewPoint(HashMap<Coordinate, MapTile> currentView) {
		// Iterate through all the tiles that the car can currently see
        for (Map.Entry<Coordinate, MapTile> mapInfo : currentView.entrySet()) {
        		Coordinate coordinate = mapInfo.getKey();
        		MapTile mapTile = mapInfo.getValue();
        		
        		// Check if the tile being inspected is a trap
        		if (mapTile instanceof TrapTile && !pointsOfInterest.containsKey(coordinate)) {
        			TrapTile trapTile = (TrapTile) mapTile;
        			String type = trapTile.getTrap();
        			switch (type) {
        				case "lava":
        					LavaTrap lavaTrap = (LavaTrap) trapTile;
        					int key = lavaTrap.getKey();
        					
        					// A key exists within the lava
        					if (key > 0 && !keys.containsKey(key)) {
        						keys.put(mapInfo.getKey(), key);
        					}
        					addPointOfInterest(coordinate, type);
        					break;
        				// One of the other traps
        				default:
        					addPointOfInterest(coordinate, type);
        					break;
        			}
        		}
        		
        		// In case the map tile is a dead end
        		if (mapTile.getType().toString().equals("WALL")) {
        			deadEnds.add(coordinate);
        		}
		}
        
//        // Print location and key #
//        for (Map.Entry<Coordinate, Integer> key : keys.entrySet()) {
//        		System.out.println("The key " + key.getValue() + " is at " + key.getKey());
//        }
//        System.out.println("---------------All Keys Processed---------------");
//        
//        // Print location of dead ends
//        for (Map.Entry<Coordinate, String> point : pointsOfInterest.entrySet()) {
//        		System.out.println("The coordinate " + point.getKey() + " is the tile of " + point.getValue() + " !");
//        }
//        System.out.println("---------------All Dead Ends Processed---------------");
	}
}
