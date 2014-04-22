package com.uacapstone.red.object;

import org.andengine.entity.sprite.Sprite;

public class PlayerData {
	public PlayerData(int id, String description)
	{
		mId = Integer.toString(id);
		mDescription = description;
		mSprite = null;
	}
	public PlayerData(int id, String description, Sprite sprite)
	{
		mId = Integer.toString(id);
		mDescription = description;
		mSprite = sprite;
	}
	public String mId;
	public String mDescription;
	public Sprite mSprite;
}
