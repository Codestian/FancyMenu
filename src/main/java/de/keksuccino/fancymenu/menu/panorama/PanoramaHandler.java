package de.keksuccino.fancymenu.menu.panorama;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.keksuccino.fancymenu.menu.fancy.helper.MenuReloadedEvent;
import de.keksuccino.konkrete.Konkrete;
import de.keksuccino.konkrete.events.SubscribeEvent;

public class PanoramaHandler {
	
	private static Map<String, ExternalTexturePanoramaRenderer> panoramas = new HashMap<String, ExternalTexturePanoramaRenderer>();
	
	public static void init() {
		updatePanoramas();
		
		Konkrete.getEventHandler().registerEventsFrom(new PanoramaHandler());
	}
	
	public static void updatePanoramas() {
		File f = new File("config/fancymenu/panoramas/");
		if (!f.exists()) {
			f.mkdirs();
		}
		
		panoramas.clear();
		for (File f2 : f.listFiles()) {
			if (f2.isDirectory()) {
				File f3 = new File(f2.getPath() + "/properties.txt");
				File f4 = new File(f2.getPath() + "/panorama");
				if (f3.exists() && f4.exists()) {
					ExternalTexturePanoramaRenderer render = new ExternalTexturePanoramaRenderer(f2.getPath());
					String name = render.getName();
					if (name != null) {
						render.preparePanorama();
						panoramas.put(name, render);
					} else {
						System.out.println("############## ERROR [FANCYMENU] ##############");
						System.out.println("Invalid panorama found: " + f2.getPath());
						System.out.println("###############################################");
					}
				}
			}
		}
	}
	
	public static ExternalTexturePanoramaRenderer getPanorama(String name) {
		return panoramas.get(name);
	}
	
	public static List<ExternalTexturePanoramaRenderer> getPanoramas() {
		List<ExternalTexturePanoramaRenderer> l = new ArrayList<ExternalTexturePanoramaRenderer>();
		l.addAll(panoramas.values());
		return l;
	}
	
	public static List<String> getPanoramaNames() {
		List<String> l = new ArrayList<String>();
		l.addAll(panoramas.keySet());
		return l;
	}
	
	public static boolean panoramaExists(String name) {
		return panoramas.containsKey(name);
	}
	
	@SubscribeEvent
	public void onMenuReload(MenuReloadedEvent e) {
		updatePanoramas();
	}

}