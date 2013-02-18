package com.ForgeEssentials.data.typeInfo;

import java.util.HashMap;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.ForgeEssentials.api.data.DataStorageManager;
import com.ForgeEssentials.api.data.IReconstructData;
import com.ForgeEssentials.api.data.ITypeInfo;
import com.ForgeEssentials.api.data.TypeData;
import com.ForgeEssentials.data.SavedField;

public class TypeInfoItemStack implements ITypeInfo<ItemStack>
{
	private static final String	SIZE		= "stackSize";
	private static final String	ITEM		= "itemID";
	private static final String	DAMAGE		= "damage";
	private static final String	COMPOUND	= "compound";
	
	public TypeInfoItemStack() {};
	
	@Override
	public void build(){}

	@Override
	public TypeData getTypeDataFromObject(ItemStack stack)
	{
		TypeData data = DataStorageManager.getDataForType(ItemStack.class);
		
		data.putField(SIZE, stack.stackSize);
		data.putField(ITEM, stack.itemID);
		data.putField(DAMAGE, stack.getItemDamage());
		data.putField(COMPOUND, stack.getTagCompound());

		return data;
	}
	
	@Override
	public Class getTypeOfField(String field)
	{
		if (field == null)
			return null;
		else if (field.equalsIgnoreCase(SIZE) || field.equalsIgnoreCase(DAMAGE) || field.equalsIgnoreCase(ITEM))
			return int.class;
		else if (field.equalsIgnoreCase(COMPOUND))
			return NBTTagCompound.class;
		else
			return null;
	}
	
	@Override
	public String[] getFieldList()
	{
		return new String[] {
			SIZE,
			ITEM,
			DAMAGE,
			COMPOUND
		};
	}

	@Override
	public ItemStack reconstruct(IReconstructData data)
	{
		int size = (Integer) data.getFieldValue(SIZE);
		int item = (Integer) data.getFieldValue(ITEM);
		int damage = (Integer) data.getFieldValue(DAMAGE);

		ItemStack stack = new ItemStack(item, size, damage);

		// TODO: when NBTs are saveable
		NBTTagCompound nbt = (NBTTagCompound) data.getFieldValue(COMPOUND);
		stack.setTagCompound(nbt);

		return stack;
	}

	@Override
	public boolean canSaveInline()
	{
		return true;
	}

	@Override
	public Class<? extends ItemStack> getType()
	{
		return ItemStack.class;
	}

	@Override
	public Class[] getGenericTypes()
	{
		return null;
	}
	
	@Override
	public ITypeInfo getInfoForField(String field)
	{
		return DataStorageManager.getInfoForType(getTypeOfField(field));
	}

}
