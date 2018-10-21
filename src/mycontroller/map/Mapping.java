package mycontroller.map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import tiles.LavaTrap;
import tiles.MapTile;
import tiles.TrapTile;
import tiles.MapTile.Type;
import utilities.Coordinate;
import world.World;

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
	private HashMap<Coordinate, MapTile> mapTiles;
	private ArrayList<Coordinate> deadEnds;
	private Map<Coordinate, Boolean> isRoadExplored;
	
	public Map<Coordinate, Boolean> getIsRoadExplored() {
		return isRoadExplored;
	}

	public Mapping() {
		keys = new HashMap<>();
		mapTiles = new HashMap<>();
		deadEnds = new ArrayList<>();
		isRoadExplored = new HashMap<>();
	}
	
	public void initialize(HashMap<Coordinate, MapTile> map) {
		this.mapTiles = map;
		for (Entry<Coordinate, MapTile> entry : map.entrySet()) {
			if (!entry.getValue().isType(Type.WALL) && !entry.getValue().isType(Type.EMPTY))
				isRoadExplored.put(entry.getKey(), false);
		}
	}
	
	public boolean containsCoordinate(Coordinate coordinate) {
		return mapTiles.containsKey(coordinate);
	}
	
	// Returns keys that have been seen by player
	public HashMap<Coordinate, Integer> getKeysSeen() {
		return keys;
	}
	
	public HashMap<Coordinate, MapTile> getMapTiles() {
		return mapTiles;
	}
	
	public ArrayList<Coordinate> getDeadEnds() {
		return deadEnds;
	}
	
	public MapTile getTile(Coordinate coordinate) {
		// Check if there is a trap and which type of trap it is
		for (Map.Entry<Coordinate, MapTile> mapInfo : mapTiles.entrySet()) {
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
	
	public void addMapTile(Coordinate coordinate, MapTile mapTile) {
		mapTiles.put(coordinate, mapTile);
	}
	
	public boolean isExplored(Coordinate coordinate) {
		return isRoadExplored.get(coordinate);
	}
	
	public void articulateViewPoint(Map<Coordinate, MapTile> currentView) {
		
		// Iterate through all the tiles that the car can currently see
        for (Map.Entry<Coordinate, MapTile> mapInfo : currentView.entrySet()) {
        	Coordinate coordinate = mapInfo.getKey();
        	MapTile mapTile = mapInfo.getValue();
        	
        	// Only record the road and traps and avoid duplicate recording
        	if (mapTile.isType(Type.EMPTY) || mapTile.isType(Type.WALL) || isRoadExplored.get(coordinate))
        		continue;
        	// Check if the tile being inspected is a lava trap
        	if (mapTile instanceof LavaTrap) {
        		LavaTrap lavaTrap = (LavaTrap) mapTile;
        		int key = lavaTrap.getKey();
        		
        		// A key exists within the lava
        		if (key > 0) {	// can have duplicated keys
        			keys.put(coordinate, key);
        		}
        	}
        	addMapTile(coordinate, mapTile);
        	isRoadExplored.put(coordinate, true);
        	
        	// In case the map tile is a dead end
        	if (mapTile.getType().toString().equals("WALL")) {
        		deadEnds.add(coordinate);
        	}
		}
	}
}
