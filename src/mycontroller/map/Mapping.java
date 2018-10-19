package mycontroller.map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import tiles.LavaTrap;
import tiles.MapTile;
import tiles.TrapTile;
import utilities.Coordinate;

/**
 * Mapping expresses the current state of the map that has been explored,
 * keeping track of the relevant features that the map has
 * @author jordanung
 *
 */

public class Mapping {
	private static Mapping instance;
	public static Mapping getMap() {
		if (Mapping.instance == null) {
			Mapping.instance = new Mapping();
		}
		
		return Mapping.instance;
	}
	
	private HashMap<Coordinate, Integer> keys;
	private HashMap<Coordinate, MapTile> pointsOfInterest;
	private ArrayList<Coordinate> deadEnds;
	
	public Mapping() {
		keys = new HashMap<>();
		pointsOfInterest = new HashMap<>();
		deadEnds = new ArrayList<>();
		
		Mapping.map = this;
	}
	
	// Returns keys that have been seen by player
	public HashMap<Coordinate, Integer> getKeysSeen() {
		return keys;
	}
	
	public HashMap<Coordinate, MapTile> getPointsOfInterest() {
		return pointsOfInterest;
	}
	
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
	}
}
