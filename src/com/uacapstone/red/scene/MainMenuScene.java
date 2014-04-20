package com.uacapstone.red.scene;

import org.andengine.engine.camera.Camera;
import org.andengine.entity.scene.menu.MenuScene;
import org.andengine.entity.scene.menu.MenuScene.IOnMenuItemClickListener;
import org.andengine.entity.scene.menu.item.IMenuItem;
import org.andengine.entity.scene.menu.item.SpriteMenuItem;
import org.andengine.entity.scene.menu.item.decorator.ScaleMenuItemDecorator;
import org.andengine.entity.sprite.Sprite;
import org.andengine.opengl.util.GLState;

import com.uacapstone.red.base.BaseScene;
import com.uacapstone.red.manager.SceneManager;
import com.uacapstone.red.manager.SceneManager.SceneType;

public class MainMenuScene extends BaseScene implements IOnMenuItemClickListener {

	@Override
	public void createScene() {
		createBackground();
		createMenuChildScene();
	}

	@Override
	public void onBackKeyPressed() {
		System.exit(0);
	}

	@Override
	public SceneType getSceneType() {
		return SceneType.SCENE_MENU;
	}

	@Override
	public void disposeScene() {
		// TODO Auto-generated method stub

	}
	
	private void createBackground()
	{
	    background = new Sprite(0, 0,
	    		resourcesManager.menu_background_region, vbom)
	    {
	        @Override
	        protected void preDraw(GLState pGLState, Camera pCamera) 
	        {
	            super.preDraw(pGLState, pCamera);
	            pGLState.enableDither();
	        }
	    };
	    background.setScale(activity.getScreenWidth()/background.getWidth(), activity.getScreenHeight()/background.getHeight());
	    attachChild(background);
	}
	
	private Sprite background;
	private MenuScene menuChildScene;
	private final int MENU_SOLO = 0;
	private final int MENU_QUICK = 1;
	private final int MENU_FRIENDS = 2;
	private final int MENU_INVITES = 3;

	private void createMenuChildScene()
	{
	    menuChildScene = new MenuScene(camera);
	    
	    final IMenuItem soloMenuItem = new ScaleMenuItemDecorator(new SpriteMenuItem(MENU_SOLO, resourcesManager.solo_region, vbom), .75f, .7f);
	    
	    final IMenuItem quickMenuItem = new ScaleMenuItemDecorator(new SpriteMenuItem(MENU_QUICK, resourcesManager.quick_region, vbom), .75f, .7f);
	    
	    final IMenuItem inviteFriendsMenuItem = new ScaleMenuItemDecorator(new SpriteMenuItem(MENU_FRIENDS, resourcesManager.friends_region, vbom), .75f, .7f);
	    
	    final IMenuItem invitesMenuItem = new ScaleMenuItemDecorator(new SpriteMenuItem(MENU_INVITES, resourcesManager.invites_region, vbom), .75f, .7f);
	    
	    menuChildScene.addMenuItem(soloMenuItem);
	    
	    menuChildScene.addMenuItem(quickMenuItem);
	    
	    menuChildScene.addMenuItem(inviteFriendsMenuItem);
	    
	    menuChildScene.addMenuItem(invitesMenuItem);
	    
	    menuChildScene.buildAnimations();
	    menuChildScene.setBackgroundEnabled(false);

	    menuChildScene.setPosition(activity.getScreenHalfWidth()*2/3, 50);
	    
	    menuChildScene.setOnMenuItemClickListener(this);
	    
	    setChildScene(menuChildScene);
	}

	@Override
	public boolean onMenuItemClicked(MenuScene pMenuScene, IMenuItem pMenuItem, float pMenuItemLocalX, float pMenuItemLocalY)
	{
        switch(pMenuItem.getID())
        {
        case MENU_SOLO:
            //Load Game Scene!
            SceneManager.getInstance().loadGameScene(engine);
            return true;
        case MENU_QUICK:
            //Load Game Scene!
            activity.StartQuickGame();
            return true;
        case MENU_FRIENDS:
            //Load Game Scene!
            activity.InviteFriends();
            return true;
        case MENU_INVITES:
            //Load Game Scene!
            activity.SeeInvitations();
            return true;
        default:
            return false;
	    }
	}

}
