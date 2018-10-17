package mycontroller.map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import tiles.LavaTrap;
import tiles.MapTile;
import tiles.MapTile.Type;
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
<<<<<<< HEAD
	private HashMap<Coordinate, String> pointsOfInterest;
	public static Mapping map;
=======
	private HashMap<Coordinate, MapTile> pointsOfInterest;
	private ArrayList<Coordinate> deadEnds;
>>>>>>> mapping
	
	public Mapping() {
		keys = new HashMap<>();
		pointsOfInterest = new HashMap<>();
	}
	
	// Returns keys that have been seen by player
	public HashMap<Coordinate, Integer> getKeysSeen() {
		return keys;
	}
	
	public HashMap<Coordinate, MapTile> getPointsOfInterest() {
		return pointsOfInterest;
	}
	
<<<<<<< HEAD
	public void addFoundKey(Coordinate coordinate, Integer value) {
		keys.put(coordinate, value);
	}
	
	public void addPointOfInterest(Coordinate coordinate, String type) {
		pointsOfInterest.put(coordinate, type);
=======
	public ArrayList<Coordinate> getDeadEnds() {
		return deadEnds;
	}
	
	public MapTile getTypeByCoordinate(Coordinate coordinate) {
		// Check if there is a trap and which type of trap it is
		for (Map.Entry<Coordinate, MapTile> mapInfo : pointsOfInterest.entrySet()) {
			if (mapInfo.getKey().equals(coordinate)) {
				return mapInfo.getValue();
			}
		}
		
		// Check whether the type of mapTile at the point is a wall
		for (Coordinate deadEnd : deadEnds) {
			if (coordinate.equals(deadEnd)) {
				return new MapTile(MapTile.Type.WALL);
			}
		}
		return null;
	}
	
	public void addPointOfInterest(Coordinate coordinate, MapTile mapTile) {
		pointsOfInterest.put(coordinate, mapTile);
>>>>>>> mapping
	}
	
	public void articulateViewPoint(HashMap<Coordinate, MapTile> currentView) {
		
		// Iterate through all the tiles that the car can currently see
        for (Map.Entry<Coordinate, MapTile> mapInfo : currentView.entrySet()) {
        		Coordinate coordinate = mapInfo.getKey();
        		String type = mapInfo.getValue().getType().toString();
			
        		// Check every lava tile for any keys within the tile
        		if (mapInfo.getValue() instanceof LavaTrap) {
				LavaTrap lavaTrap = (LavaTrap) mapInfo.getValue();
				
				// A key exists within the lava
				if (lavaTrap.getKey() > 0 && !keys.containsKey(mapInfo.getKey())) {
					keys.put(mapInfo.getKey(), lavaTrap.getKey());
				}
			}
        		
<<<<<<< HEAD
        		// Found a Mud Trap i.e. game over when traversed over
        		if (mapInfo.getValue() instanceof MudTrap && !pointsOfInterest.containsKey(coordinate)) {
        			addPointOfInterest(coordinate, type);
        		}
		}
        
        // Print location and key #
        for (Map.Entry<Coordinate, Integer> key : keys.entrySet()) {
        		System.out.println("The key " + key.getValue() + " is at " + key.getKey());
        }
        System.out.println("---------------All Keys Processed---------------");
        
        // Print location of dead ends
        for (Map.Entry<Coordinate, String> point : pointsOfInterest.entrySet()) {
        		System.out.println("The coordinate " + point.getKey() + " is the tile of " + point.getValue() + " !");
        }
        System.out.println("---------------All Dead Ends Processed---------------");
=======
        		// Check if the tile being inspected is a trap
        		if (mapTile instanceof TrapTile && !pointsOfInterest.containsKey(coordinate)) {
        			TrapTile trapTile = (TrapTile) mapTile;
        			String type = trapTile.getTrap();
        			switch (type) {
        				case "lava":
        					LavaTrap lavaTrap = (LavaTrap) trapTile;
        					int key = lavaTrap.getKey();
        					
        					// A key exists within the lava
        					if (key > 0 && !keys.containsValue(key)) {
        						keys.put(mapInfo.getKey(), key);
        					}
        					addPointOfInterest(coordinate, mapTile);
        					break;
        					
        				// One of the other traps
        				default:
        					addPointOfInterest(coordinate, mapTile);
        					break;
        			}
        		}
        		
        		// In case the map tile is a dead end
        		if (mapTile.getType().toString().equals("WALL")) {
        			deadEnds.add(coordinate);
        		}
		}
>>>>>>> mapping
	}
}
