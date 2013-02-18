package com.ForgeEssentials.data;

import java.io.File;
import java.util.ArrayList;
import java.util.Map.Entry;

import net.minecraftforge.common.ConfigCategory;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;

import com.ForgeEssentials.api.data.DataStorageManager;
import com.ForgeEssentials.api.data.TypeData;
import com.ForgeEssentials.api.data.ITypeInfo;

/**
 * Storage driver for filesystem (flat-file) persistence.
 * 
 * @author AbrarSyed
 * 
 */
public class ForgeConfigDataDriver extends TextDataDriver
{

	@Override
	protected String getExtension()
	{
		return "cfg";
	}

	@Override
	protected boolean saveData(Class type, TypeData objectData)
	{
		boolean wasSuccessful = false;

		File file = getFilePath(type, objectData.getUniqueKey());

		// Wipe existing Forge Configuration file - they don't take new data.
		if (file.exists())
		{
			file.delete();
		}

		Configuration cfg = new Configuration(file, true);

		// write each and every field to the config file.
		for (Entry<String, Object> entry : objectData.getAllFields())
			writeFieldToProperty(cfg, type.getSimpleName(), entry.getKey(), entry.getValue());

		cfg.save();

		return wasSuccessful;
	}

	@Override
	protected TypeData loadData(Class type, String uniqueKey)
	{
		Configuration cfg = new Configuration(getFilePath(type, uniqueKey), true);
		cfg.load();
		ITypeInfo info = DataStorageManager.getInfoForType(type);
		TypeData data = DataStorageManager.getDataForType(type);
		readClassFromProperty(cfg, cfg.categories.get(type.getSimpleName()), data, info);
		data.setUniqueKey(uniqueKey);

		return data;
	}

	@Override
	protected TypeData[] loadAll(Class type)
	{
		File[] files = getTypePath(type).listFiles();
		ArrayList<TypeData> data = new ArrayList<TypeData>();

		if (files != null)
		{
			for (File file : files)
			{
				if (!file.isDirectory() && file.getName().endsWith(".cfg"))
				{
					data.add(loadData(type, file.getName().replace(".cfg", "")));
				}
			}
		}

		return data.toArray(new TypeData[] {});
	}

	private void writeFieldToProperty(Configuration cfg, String category, String name, Object obj)
	{
		if (name == null || obj == null)
		{
			// ignore...
			return;
		}
		
		Class type = obj.getClass();
		
		if (type.equals(Integer.class))
		{
			cfg.get(category, name, ((Integer) obj).intValue());
		}
		else if (type.equals(int[].class))
		{
			cfg.get(category, name, (int[]) obj);
		}
		else if (type.equals(Float.class))
		{
			cfg.get(category, name, ((Float) obj).floatValue());
		}
		else if (type.equals(Double.class))
		{
			cfg.get(category, name, ((Double) obj).doubleValue());
		}
		else if (type.equals(double[].class))
		{
			cfg.get(category, name, (double[]) obj);
		}
		else if (type.equals(Boolean.class))
		{
			cfg.get(category, name, ((Boolean) obj).booleanValue());
		}
		else if (type.equals(boolean[].class))
		{
			cfg.get(category, name, (boolean[]) obj);
		}
		else if (type.equals(String.class))
		{
			cfg.get(category, name, (String) obj);
		}
		else if (type.equals(String[].class))
		{
			cfg.get(category, name, (String[]) obj);
		}
		else if (type.equals(TypeData.class))
		{
			TypeData data = (TypeData) obj;
			String newcat = category + "." + name;
			
			for (Entry<String, Object> entry : data.getAllFields())
				writeFieldToProperty(cfg, newcat, entry.getKey(), entry.getValue());
		}
		else
		{
			throw new IllegalArgumentException("Cannot save object type.");
		}
	}

	private Object readFieldFromProperty(Configuration cfg, String category, String name, Class type)
	{
		if (type.equals(int.class))
		{
			return cfg.get(category, name, 0).getInt();
		}
		else if (type.equals(int[].class))
		{
			return cfg.get(category, name, new int[] {}).getIntList();
		}
		else if (type.equals(float.class))
		{
			return (float) cfg.get(category, name, 0d).getDouble(0);
		}
		else if (type.equals(double.class))
		{
			return cfg.get(category, name, 0d).getDouble(0);
		}
		else if (type.equals(double[].class))
		{
			return cfg.get(category, name, new double[] {}).getDoubleList();
		}
		else if (type.equals(boolean.class))
		{
			return cfg.get(category, name, false).getBoolean(false);
		}
		else if (type.equals(boolean[].class))
		{
			return cfg.get(category, name, new boolean[] {}).getBooleanList();
		}
		else if (type.equals(String.class))
		{
			return cfg.get(category, name, "").value;
		}
		else if (type.equals(String[].class))
		{
			return cfg.get(category, name, new String[] {}).valueList;
		}
		else
		{
			// this should never happen...
			return null;
		}
	}

	private void readClassFromProperty(Configuration cfg, ConfigCategory cat, TypeData data, ITypeInfo info)
	{

		if (cat != null)
		{
			String name;
			Class newType;
			ITypeInfo newInfo;
			TypeData newData;
			Object value;
			for (Property prop : cat.getValues().values())
			{
				name = prop.getName();
				newType = info.getTypeOfField(name);
				value = readFieldFromProperty(cfg, cat.getQualifiedName(), name, newType);
				data.putField(name, value);
			}

			for (ConfigCategory child : cfg.categories.values())
			{
				if (child.isChild() && child.parent == cat) // intentional use
															// of ==
				{
					name = child.getQualifiedName().replace(cat.getQualifiedName() + ".", "");
					newInfo = info.getInfoForField(name);
					newData = DataStorageManager.getDataForType(info.getType());

					if (newData == null || newInfo == null)
					{
						continue;
					}
					readClassFromProperty(cfg, child, newData, newInfo);
					value = newData;
					data.putField(name, value);
				}
			}
		}
	}
}
