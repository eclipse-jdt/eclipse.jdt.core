package test0580;

import ecosim.model.DefaultLocationModel;
import ecosim.model.LocationModel;
import ecosim.model.MapModel;


/**
 * @author Ben Hutchison
 *
 */
public class DiamondSquareTerrainGenerator implements MapGenerator 
{
	//number of halving recursive subdivisons to cover map 
	int _subdivisions;

	//both width and height = 2^_subdivisions + 1 (ie square map)
	int _width, _height;
	
	d
	
	LocationModel[][] _map;
	
	/* (non-Javadoc)
	 * @see ecosim.model.map.MapGenerator#generateLocation(int, int, ecosim.model.MapModel)
	 */
	public LocationModel generateLocation(int i, int j, MapModel mapModel)
	{

		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see ecosim.model.map.MapGenerator#setSize(int, int)
	 */
	public void setSize(int width, int height)
	{
		_subdivisions = Math.max(ceilLogBase2(width), ceilLogBase2(height));
		
		//diamond-square alg needs map size == power of 2 plus one; select next suitable size
		_width = powerOf2(_subdivisions) + 1;
		_height = powerOf2(_subdivisions) + 1;
		
		_map = new LocationModel[_width][_height];
		
		generateMap();
	}
	
	void generateMap() {
		int lod = _subdivisions;
		for (int i = 0; i < lod; ++ i) {
		      int q = 1 << i, r = 1 << (lod - i), s = r >> 1;
		      for (int j = 0; j < divisions; j += r)
		        for (int k = 0; k < divisions; k += r)
		          diamond (j, k, r, rough);
		      if (s > 0)
		        for (int j = 0; j <= divisions; j += s)
		          for (int k = (j + s) % r; k <= divisions; k += r)
		            square (j - s, k - s, r, rough);
		      rough *= roughness;
		    }
	}
	
	void diamond(int x, int y, int side, double scale) {
		if (side > 1) {
		      int half = side / 2;
		      double avg = (terrain[x][y] + terrain[x + side][y] +
		        terrain[x + side][y + side] + terrain[x][y + side]) * 0.25;
		      terrain[x + half][y + half] = avg + rnd () * scale;
		    }

	}
	
	void square (int x, int y, int side, double scale) {
	    int half = side / 2;
	    double avg = 0.0, sum = 0.0;
	    if (x >= 0)
	    { avg += terrain[x][y + half]; sum += 1.0; }
	    if (y >= 0)
	    { avg += terrain[x + half][y]; sum += 1.0; }
	    if (x + side <= divisions)
	    { avg += terrain[x + side][y + half]; sum += 1.0; }
	    if (y + side <= divisions)
	    { avg += terrain[x + half][y + side]; sum += 1.0; }
	    terrain[x + half][y + half] = avg / sum + rnd () * scale;
	  }
	
	
	public static int ceilLogBase2(int value) {
		if (value <= 1)
			return 0;
		int exponent = 1;
		value--;
		while ((value = value >> 1) != 0)
			exponent++;
		return exponent;
	}
	public static int powerOf2(int exponent) {
		int value = 1;
		while (exponent-- > 0)
			value = value << 1;
		return value;
	}
	
	
	public static void main(String[] args)
	{
		System.out.println("1: "+ceilLogBase2(1));
		System.out.println("2: "+ceilLogBase2(2));
		System.out.println("3: "+ceilLogBase2(3));
		System.out.println("4: "+ceilLogBase2(4));
		System.out.println("5: "+ceilLogBase2(5));
		System.out.println("-3: "+ceilLogBase2(3));
		System.out.println("1024: "+ceilLogBase2(1024));
		System.out.println("1023: "+ceilLogBase2(1023));
		System.out.println("1025: "+ceilLogBase2(1025));
		
		System.out.println("1 "+powerOf2(1));
		System.out.println("2 "+powerOf2(2));
		System.out.println("3 "+powerOf2(3));
	}
	
}
