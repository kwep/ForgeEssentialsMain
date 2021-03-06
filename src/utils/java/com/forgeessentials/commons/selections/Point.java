package com.forgeessentials.commons.selections;

import com.forgeessentials.commons.IReconstructData;
import com.forgeessentials.commons.SaveableObject;
import com.forgeessentials.commons.SaveableObject.Reconstructor;
import com.forgeessentials.commons.SaveableObject.SaveableField;
import com.forgeessentials.commons.SaveableObject.UniqueLoadingKey;
import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SaveableObject(SaveInline = true)
public class Point implements Serializable, Comparable<Point> {
	/**
     *
     */
	private static final long serialVersionUID = 9058731447466825626L;

	@SaveableField
	public int x;

	@SaveableField
	public int y;

	@SaveableField
	public int z;

	public Point(int x, int y, int z)
	{
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Point (double x, double y, double z)
	{
		this.x = ((int) x);
		this.y = ((int) y);
		this.z = ((int) z);
	}

	public Point(Entity player)
	{
		x = (int) Math.floor(player.posX);
		y = (int) Math.floor(player.posY);
		z = (int) Math.floor(player.posZ);
	}

	public Point(Vec3 vector)
	{
		this((int) vector.xCoord, (int) vector.yCoord, (int) vector.zCoord);
	}

	public int getX()
	{
		return x;
	}

	public int getY()
	{
		return y;
	}

	public int getZ()
	{
		return z;
	}

	public void setX(int x)
	{
		this.x = x;
	}

	public void setY(int y)
	{
		this.y = y;
	}

	public void setZ(int z)
	{
		this.z = z;
	}

	/**
	 * This is calculated by the whichever has higher coords.
	 *
	 * @return Posotive number if this Point is larger. 0 if they are equal. Negative if the provided point is larger.
	 */
	@Override
	public int compareTo(Point point)
	{
		if (equals(point))
		{
			return 0;
		}

		int positives = 0;
		int negatives = 0;

		if (x > point.x)
		{
			positives++;
		}
		else
		{
			negatives++;
		}

		if (y > point.y)
		{
			positives++;
		}
		else
		{
			negatives++;
		}

		if (z > point.z)
		{
			positives++;
		}
		else
		{
			negatives++;
		}

		if (positives > negatives)
		{
			return +1;
		}
		else if (negatives > positives)
		{
			return -1;
		}
		else
		{
			return x - point.x + y - point.y + z - point.z;
		}
	}

	@Override
	public boolean equals(Object object)
	{
		if (object instanceof Point)
		{
			Point p = (Point) object;
			if (x == p.x && y == p.y && z == p.z)
			{
				return true;
			}
		}
		return false;
	}

	@Override
    public int hashCode() {
	    int h = 11 + x;
        h = h * 29 + y;
        h = h * 29 + z;
        return h;
	}
	
	/**
	 * @param point
	 * @return The distance to a given Block.
	 */
	public double getDistanceTo(Point point)
	{
		return Math.sqrt((x - point.x) * (x - point.x) + (y - point.y) * (y - point.y) + (z - point.z) * (z - point.z));
	}

	/**
	 * @param p
	 * @return TRUE if the points have the same coordinate on at least one axis.
	 */
	public boolean alignsWith(Point p)
	{
		return x == p.x || y == p.y || z == p.z;
	}

	public boolean isGreaterEqualThan(Point p)
	{
		return x >= p.x && y >= p.y && z >= p.z;
	}

	public boolean isLessEqualThan(Point p)
	{
		return x <= p.x && y <= p.y && z <= p.z;
	}

	/**
	 * gets a new Point with the same data as the provided one.
	 *
	 * @param point
	 * @return
	 */
	public static Point copy(Point point)
	{
		return new Point(point.x, point.y, point.z);
	}

	/**
	 * ensures the Point is valid. Just floors the Y axis to 0. Y can't be negative.
	 */
	public void validate()
	{
		if (y < 0)
		{
			y = 0;
		}
	}

	@Reconstructor()
	public static Point reconstruct(IReconstructData tag)
	{
		int x = (Integer) tag.getFieldValue("x");
		int y = (Integer) tag.getFieldValue("y");
		int z = (Integer) tag.getFieldValue("z");
		return new Point(x, y, z);
	}

	@UniqueLoadingKey()
	private String getLoadingField()
	{
		return toString();
	}

	@Override
	public String toString()
	{
		return "[" + x + ", " + y + ", " + z + "]";
	}

    private static final Pattern pattern = Pattern.compile("\\s*\\[\\s*(-?\\d+)\\s*,\\s*(-?\\d+)\\s*,\\s*(-?\\d+)\\s*\\]\\s*");
    
	public static Point fromString(String value)
    {
        Matcher match = pattern.matcher(value);
        if (!match.matches())
            return null;
        return new Point(Integer.parseInt(match.group(1)), Integer.parseInt(match.group(2)), Integer.parseInt(match.group(3)));
    }

	public Vec3 toVec3()
	{
		return Vec3.createVectorHelper(x, y, z);
	}

	
}
