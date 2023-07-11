package de.keksuccino.fancymenu.customization.layout;

import java.io.File;
import java.util.*;

import com.google.common.io.Files;
import de.keksuccino.fancymenu.FancyMenu;
import de.keksuccino.fancymenu.util.audio.SoundRegistry;
import de.keksuccino.fancymenu.customization.ScreenCustomization;
import de.keksuccino.fancymenu.customization.animation.AdvancedAnimation;
import de.keksuccino.fancymenu.customization.animation.AnimationHandler;
import de.keksuccino.fancymenu.customization.layout.editor.LayoutEditorScreen;
import de.keksuccino.fancymenu.util.file.FilenameComparator;
import de.keksuccino.fancymenu.util.Legacy;
import de.keksuccino.fancymenu.util.ListUtils;
import de.keksuccino.konkrete.file.FileUtils;
import de.keksuccino.fancymenu.util.properties.PropertiesSerializer;
import de.keksuccino.fancymenu.util.properties.PropertyContainerSet;
import de.keksuccino.konkrete.rendering.animation.IAnimationRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class LayoutHandler {
	
	private static final List<Layout> LAYOUTS = new ArrayList<>();

	public static void init() {
		convertLegacyDisabledLayouts();
		reloadLayouts();
	}

	public static void reloadLayouts() {
		ScreenCustomization.readCustomizableScreensFromFile();
		LAYOUTS.clear();
		LAYOUTS.addAll(deserializeLayoutFilesInDirectory(FancyMenu.LAYOUT_DIR));
	}

	@NotNull
	public static List<Layout> deserializeLayoutFilesInDirectory(File directory) {
		List<Layout> layouts = new ArrayList<>();
		if (!directory.exists()) {
			directory.mkdirs();
		}
		File[] filesArray = directory.listFiles();
		if (filesArray != null) {
			for (File f : filesArray) {
				if (f.getPath().toLowerCase().endsWith(".txt")) {
					PropertyContainerSet s = PropertiesSerializer.deserializePropertyContainerSet(f.getAbsolutePath().replace("\\", "/"));
					if (s != null) {
						Layout layout = deserializeLayout(s, f);
						if (layout != null) {
							layouts.add(layout);
						}
					}
				}
			}
		}
		return layouts;
	}

	@Nullable
	public static Layout deserializeLayout(@NotNull PropertyContainerSet serialized, @Nullable File layoutFile) {
		return Layout.deserialize(serialized, layoutFile);
	}

	@NotNull
	public static List<Layout> getEnabledLayouts() {
		List<Layout> enabled = new ArrayList<>();
		LAYOUTS.forEach(layout -> { if (layout.isEnabled()) enabled.add(layout); });
		return enabled;
	}

	@NotNull
	public static List<Layout> getDisabledLayouts() {
		List<Layout> disabled = new ArrayList<>();
		LAYOUTS.forEach(layout -> { if (!layout.isEnabled()) disabled.add(layout); });
		return disabled;
	}

	@NotNull
	public static List<Layout> getAllLayouts() {
		return new ArrayList<>(LAYOUTS);
	}

	@NotNull
	public static List<Layout> getEnabledLayoutsForMenuIdentifier(@NotNull String menuIdentifier, boolean includeUniversalLayouts) {
		List<Layout> l = new ArrayList<>();
		for (Layout layout : getEnabledLayouts()) {
			if (layout.menuIdentifier.equals(menuIdentifier)) {
				l.add(layout);
			} else if (layout.isUniversalLayout() && includeUniversalLayouts) {
				if (!layout.universalLayoutMenuWhitelist.isEmpty() || !layout.universalLayoutMenuBlacklist.isEmpty()) {
					if (!layout.universalLayoutMenuWhitelist.isEmpty() && layout.universalLayoutMenuWhitelist.contains(menuIdentifier)) {
						l.add(layout);
					} else if (!layout.universalLayoutMenuBlacklist.isEmpty() && !layout.universalLayoutMenuBlacklist.contains(menuIdentifier)) {
						l.add(layout);
					}
				} else {
					l.add(layout);
				}
			}
		}
		return l;
	}

	@NotNull
	public static List<Layout> getDisabledLayoutsForMenuIdentifier(@NotNull String menuIdentifier) {
		List<Layout> l = new ArrayList<>();
		for (Layout layout : getDisabledLayouts()) {
			if (layout.menuIdentifier.equals(menuIdentifier)) {
				l.add(layout);
			}
		}
		return l;
	}

	@NotNull
	public static List<Layout> getAllLayoutsForMenuIdentifier(@NotNull String menuIdentifier, boolean includeUniversalLayouts) {
		return ListUtils.mergeLists(getEnabledLayoutsForMenuIdentifier(menuIdentifier, includeUniversalLayouts), getDisabledLayoutsForMenuIdentifier(menuIdentifier));
	}

	@Nullable
	public static Layout getLayout(String name) {
		for (Layout l : LAYOUTS) {
			if (l.getLayoutName().equals(name)) return l;
		}
		return null;
	}

	@NotNull
	public static List<Layout> sortLayoutListByLastEdited(@NotNull List<Layout> layouts, boolean removeNeverEdited) {
		layouts.sort(Comparator.comparingLong(value -> value.lastEditedTime));
		Collections.reverse(layouts);
		if (removeNeverEdited) layouts.removeIf(l -> (l.lastEditedTime == -1));
		return layouts;
	}

	@NotNull
	public static List<Layout> sortLayoutListByLastEdited(@NotNull List<Layout> layouts, boolean removeNeverEdited, int maxLayouts) {
		sortLayoutListByLastEdited(layouts, removeNeverEdited);
		if (!layouts.isEmpty()) {
			List<Layout> temp = new ArrayList<>(layouts.subList(0, Math.min(maxLayouts, layouts.size())));
			layouts.clear();
			layouts.addAll(temp);
		}
		return layouts;
	}

	@NotNull
	public static List<Layout> sortLayoutListByName(@NotNull List<Layout> layouts) {
		FilenameComparator comp = new FilenameComparator();
		layouts.sort((o1, o2) -> comp.compare(o1.getLayoutName(), o2.getLayoutName()));
		return layouts;
	}

	@NotNull
	public static List<Layout> sortLayoutListByStatus(@NotNull List<Layout> layouts, boolean disabledFirst) {
		sortLayoutListByName(layouts);
		List<Layout> enabled = new ArrayList<>();
		List<Layout> disabled = new ArrayList<>();
		layouts.forEach(layout -> {
			if (layout.isEnabled()) {
				enabled.add(layout);
			} else {
				disabled.add(layout);
			}
		});
		layouts.clear();
		if (disabledFirst) {
			layouts.addAll(disabled);
			layouts.addAll(enabled);
		} else {
			layouts.addAll(enabled);
			layouts.addAll(disabled);
		}
		return layouts;
	}

	public static void deleteLayout(@NotNull Layout layout, boolean reInitCurrentScreen) {
		try {
			if (layout.layoutFile != null) {
				layout.layoutFile.delete();
			}
			LAYOUTS.remove(layout);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		if (reInitCurrentScreen) ScreenCustomization.reInitCurrentScreen();
	}

	public static void addLayout(@NotNull Layout layout, boolean saveToFile) {
		if (!LAYOUTS.contains(layout)) {
			LAYOUTS.add(layout);
			if (saveToFile) layout.saveToFileIfPossible();
		}
	}

	public static void openLayoutEditor(@NotNull Layout layout, @Nullable Screen layoutTargetScreen) {
		try {
			SoundRegistry.stopSounds();
			SoundRegistry.resetSounds();
			for (IAnimationRenderer r : AnimationHandler.getAnimations()) {
				if (r instanceof AdvancedAnimation) {
					((AdvancedAnimation)r).stopAudio();
					if (((AdvancedAnimation)r).replayIntro()) {
						r.resetAnimation();
					}
				}
			}
			Minecraft.getInstance().setScreen(new LayoutEditorScreen(layoutTargetScreen, layout));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Will save the layout as layout file.
	 *
	 * @param saveTo Full file path with file name + extension.
	 */
	public static boolean saveLayoutToFile(Layout layout, String saveTo) {
		File f = new File(saveTo);
		if (f.isFile()) {
			f.delete();
		}
		PropertyContainerSet set = layout.serialize();
		if (set != null) {
			PropertiesSerializer.serializePropertyContainerSet(set, f.getPath());
			return true;
		}
		return false;
	}

	@Legacy("This basically copies all layouts from the old '.disabled' directory to the main directory and sets them to disabled.")
	private static void convertLegacyDisabledLayouts() {
		File disabledDir = new File(FancyMenu.LAYOUT_DIR.getPath() + "/.disabled");
		if (disabledDir.isDirectory()) {
			List<Layout> legacyDisabled = deserializeLayoutFilesInDirectory(disabledDir);
			for (Layout l : legacyDisabled) {
				try {
					if (l.layoutFile != null) {
						String name = FileUtils.generateAvailableFilename(FancyMenu.LAYOUT_DIR.getPath(), Files.getNameWithoutExtension(l.layoutFile.getPath()), "txt");
						File newFile = new File(FancyMenu.LAYOUT_DIR.getPath() + "/" + name);
						FileUtils.copyFile(l.layoutFile, newFile);
						l.layoutFile.delete();
						l.layoutFile = newFile;
						l.enabled = false;
						l.saveToFileIfPossible();
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}

}