package rs2;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.JInternalFrame;

import rs2.cache.Archive;
import rs2.cache.Cache;
import rs2.cache.media.MediaArchive;
import rs2.constants.Constants;
import rs2.graphics.RSDrawingArea;
import rs2.graphics.RSFont;
import rs2.graphics.RSImage;
import rs2.graphics.RSImageProducer;
import rs2.graphics.RealFont;
import rs2.listeners.impl.MyKeyListener;
import rs2.listeners.impl.MyMouseListener;
import rs2.swing.UserInterface;

@SuppressWarnings("serial")
public class Main extends Canvas implements Runnable, FocusListener, WindowListener {

	/**
	 * Is an area being selected or already selected?
	 * @return
	 */
	public boolean isSelected() {
		if (selectionX != -1 && selectionY != -1) {
			return true;
		}
		return false;
	}

	/**
	 * Gets the font metrics.
	 * @param font
	 * @return
	 */
	public FontMetrics getMetrics(Font font) {
		FontMetrics metrics = null;
		if (font != null) {
			metrics = getFontMetrics(font);
		}
		return metrics;
	}

	/**
	 * Sets the next child as the selected interface.
	 */
	public static void tab() {
		RSInterface rsi = getInterface();
		if (rsi.children != null) {
			if (getSelectedIndex() + 1 < rsi.children.length) {
				selectChild(getSelectedIndex() + 1);
			} else {
				selectChild(0);
			}
		}
	}

	/**
	 * Returns the text for the type of interface.
	 * @param id
	 * @param type
	 * @return
	 */
	public static String getType(int id, int type) {
		switch(type) {
			case 0:
				return "parent";
			case 1:
				return "";
			case 2:
				return "item group";
			case 3:
				return "pixels: " + getInterface(id).width + "x" + getInterface(id).height;
			case 4:
				return "text: " + getInterface(id).disabledText;
			case 5:
				return "image";
			case 6:
				return "model";
			case 7:
				return "media";
			case 8:
				return "tooltip";
		}
		return null;
	}

	/**
	 * Gets the current interface.
	 * @return
	 */
	public static RSInterface getInterface() {
		if (currentId == -1) {
			return null;
		}
		return RSInterface.cache[currentId];
	}

	/**
	 * Gets the interface of the selected id.
	 * @return
	 */
	public static RSInterface getSelected() {
		if (selectedId == -1) {
			return null;
		}
		return RSInterface.cache[selectedId];
	}

	/**
	 * Gets the interface for the hovered id.
	 * @return
	 */
	public static RSInterface getHovered() {
		if (hoverId == -1) {
			return null;
		}
		return RSInterface.cache[hoverId];
	}

	/**
	 * Gets the interface for the specified id.
	 * @param id
	 * @return
	 */
	public static RSInterface getInterface(int id) {
		return RSInterface.cache[id];
	}

	/**
	 * Gets the selected child's index in the parent's children.
	 * @return
	 */
	public static int getSelectedIndex() {
		RSInterface parent = getInterface();
		if (parent.children == null) {
			return -1;
		}
		for (int index = 0; index < parent.children.length; index++) {
			if (parent.children[index] == getSelected().id) {
				return index;
			}
		}
		return -1;
	}

	/**
	 * Gets the selected child's x-position.
	 * @return
	 */
	public static int getSelectedX() {
		if (selectedId == -1) {
			return 0;
		}
		return getX(getInterface(), getSelected());
	}

	/**
	 * Gets the selected child's y-position.
	 * @return
	 */
	public static int getSelectedY() {
		if (selectedId == -1) {
			return 0;
		}
		return getY(getInterface(), getSelected());
	}

	/**
	 * Gets the x-position of a child on the parent interface.
	 * @param parent The parent interface.
	 * @param child The child to find the x-position for.
	 * @return
	 */
	public static int getX(RSInterface parent, RSInterface child) {
		if (parent == null || parent.children == null) {
			return -1;
		}
		for (int index = 0; index < parent.children.length; index++) {
			if (parent.children[index] == child.id) {
				return parent.childX[index];
			}
		}
		return -1;
	}

	/**
	 * Gets the y-position of a child on the parent interface.
	 * @param parent The parent interface.
	 * @param child The child to find the y-position for.
	 * @return
	 */
	public static int getY(RSInterface parent, RSInterface child) {
		if (parent == null || parent.children == null) {
			return -1;
		}
		for (int index = 0; index < parent.children.length; index++) {
			if (parent.children[index] == child.id) {
				return parent.childY[index];
			}
		}
		return -1;
	}

	/**
	 * Selects the specified child index.
	 * @param index
	 */
	public static void selectChild(int index) {
		RSInterface rsi = getInterface();
		if (rsi != null) {
			if (rsi.children != null) {
				if (index < rsi.children.length - 1) {
					selectedId = rsi.children[index];
				}
			}
		}
	}

	/**
	 * Selects the specified id as the current interface.
	 * @param id
	 */
	public void selectInterface(int id) {
		selectedId = -1;
		currentId = id;
		verticalPos = getCanvasHeight() / 2;
		horizontalPos = getCanvasWidth() / 2;
		verticalScale = 10;
		horizontalScale = 10;
		UserInterface.ui.buildTreePane();
	}

	/**
	 * Gets the horizontal slider width.
	 * @return
	 */
	public int getSliderWidth() {
		return getCanvasWidth() / 5;
	}

	/**
	 * Gets the vertical slider height.
	 * @return
	 */
	public int getSliderHeight() {
		return getCanvasHeight() / 5;
	}

	/**
	 * Draws the grid sliders.
	 */
	public void drawSliders() {
		if (!Settings.displayGrid) {
			return;
		}
		int background = 0x151515;
		int bar = 0x666666;
		int thickness = 8;
		int start = 150;
		if (mouseInRegion(0, getCanvasWidth() - thickness, getCanvasHeight() - thickness, getCanvasHeight())) {
			if (alpha[2] < 50) {
				alpha[2] += 5;
			}
		} else {
			if (alpha[2] > 0) {
				alpha[2] -= 5;
			}
		}
		if (mouseInRegion(getCanvasWidth() - thickness, getCanvasWidth(), 0, getCanvasHeight() - thickness)) {
			if (alpha[3] < 50) {
				alpha[3] += 5;
			}
		} else {
			if (alpha[3] > 0) {
				alpha[3] -= 5;
			}
		}
		RSDrawingArea.drawRoundedRectangle(0, getCanvasHeight() - thickness, getCanvasWidth() - thickness, thickness, background, start + alpha[2], true, false);
		RSDrawingArea.drawRoundedRectangle(getCanvasWidth() - thickness, 0, thickness, getCanvasHeight() - thickness, background, start + alpha[3], true, false);
		RSDrawingArea.drawRoundedRectangle(horizontalPos - (getSliderWidth() / 2), getCanvasHeight() - thickness, getSliderWidth(), thickness, bar, start + (alpha[2] * 2), true, false);
		RSDrawingArea.drawRoundedRectangle(getCanvasWidth() - thickness, verticalPos - (getSliderHeight() / 2), thickness, getSliderHeight(), bar, start + (alpha[3] * 2), true, false);
	}

	public static int horizontalScale = 10;
	public static int verticalScale = 10;

	/**
	 * Displays the grid.
	 */
	public void drawGrid(Graphics g) {
		if (!Settings.displayGrid) {
			return;
		}
		g.setColor(new Color(255, 255, 255, 15));
		int width = getWidth() + 1;
		int height = getHeight() + 1;
		int horizontalCount = width / (horizontalScale != 0 ? horizontalScale : 1);
		int verticalCount = height / (verticalScale != 0 ? verticalScale : 1);
		for (int index = 0, x = 0; index < horizontalCount + 1; index++, x += horizontalScale) {
			g.drawLine(x, 0, x, height);
		}
		for (int index = 0, y = 0; index < verticalCount + 1; index++, y += verticalScale) {
			g.drawLine(0, y, width, y);
		}
	}

	/**
	 * Draws the data pane.
	 */
	public void drawDataPane() {
		String[] names = { "currentId:", "selectedId:", "selectedX:", "selectedY:", "locked:", "hoverId:" };
		Object[] values = { currentId, selectedId, getSelectedX(), getSelectedY(), getSelected() != null ? getSelected().locked : false, hoverId };
		int width = 80;
		int height = (names.length * 12) + 2;
		int alpha = 100;
		int x = 5;
		int y = getCanvasHeight() - (height + 5);
		for (int index = 0; index < names.length; index++) {
			if (small.getTextWidth(names[index] + " " + values[index]) > width) {
				width = small.getTextWidth(names[index] + " " + values[index]);
			}
		}
		width += 5;
		RSDrawingArea.drawRoundedRectangle(x, y, width, height, 0, alpha, true, true);
		RSDrawingArea.drawRoundedRectangle(x, y, width, height, 0xFFFFFF, alpha + 50, false, true);
		for (int index = 0; index < names.length; index++, y += 11) {
			arial[0].drawString(names[index] + " " + values[index], x + 5, y + 12, arialColor, true);
		}
	}

	/**
	 * Displays the children on the interface that are locked.
	 */
	public void showLockedChildren() {
		if (getInterface() == null || getInterface().children == null) {
			return;
		}
		for (int index = 0; index < getInterface().children.length; index++) {
			RSInterface child = getInterface(getInterface().children[index]);
			if (child.locked) {
				RSDrawingArea.drawFilledAlphaPixels(getX(getInterface(), child), getY(getInterface(), child), child.width, child.height, 0, 150);
				RSDrawingArea.drawUnfilledPixels(getX(getInterface(), child), getY(getInterface(), child), child.width, child.height, 0);
				RSImage lock = new RSImage("lock.png");
				lock.drawCenteredARGBImage(getX(getInterface(), child) + (child.width / 2), getY(getInterface(), child) + (child.height / 2));
			}
		}
	}

	/**
	 * Calculates the x and y distance the mouse has been dragged from the original click.
	 * @return
	 */
	public int[] calculateDragDistance() {
		int[] distances = new int[2];
		int startX = clickX;
		int startY = clickY;
		int currentX = mouseX;
		int currentY = mouseY;
		if (startX < currentX) {
			distances[0] = +(currentX - startX);
		}
		if (startX > currentX) {
			distances[0] = -(startX - currentX);
		}
		if (startY < currentY) {
			distances[1] = +(currentY - startY);
		}
		if (startY > currentY) {
			distances[1] = -(startY - currentY);
		}
		return distances;
	}

	/**
	 * Draws the selection rectangle.
	 */
	public void drawSelection() {
		if (isSelected()) {
			if (selectionWidth < 0) {
				selectionWidth *= -1;
				selectionX -= selectionWidth;
			}
			if (selectionHeight < 0) {
				selectionHeight *= -1;
				selectionY -= selectionHeight;
			}
			RSDrawingArea.drawUnfilledPixels(selectionX, selectionY, selectionWidth, selectionHeight, 0x00FFFF);
			RSDrawingArea.drawFilledAlphaPixels(selectionX, selectionY, selectionWidth, selectionHeight, 0x00FFFF, 125);
		}
	}

	/**
	 * Displays the interface.
	 */
	private void displayInterfacePane() {
		RSDrawingArea.drawFilledAlphaPixels(0, 0, getCanvasWidth(), getCanvasHeight(), Constants.BACKGROUND_COLOR, 256);
		if (currentId != -1 && getInterface() != null) {
			drawInterface(getInterface(), 0, 0, 0);
			if (getHovered() != null && Settings.displayHover) {
				RSDrawingArea.drawFilledAlphaPixels(getX(getInterface(), getHovered()), getY(getInterface(), getHovered()), getHovered().width, getHovered().height, 0xffffff, 50);
				RSDrawingArea.drawUnfilledPixels(getX(getInterface(), getHovered()), getY(getInterface(), getHovered()), getHovered().width, getHovered().height, 0xffffff);
			}
			if (selectedId != -1) {
				childActions = new String[]{ "Remove", "Move down", "Move up", "Move to back", "Move to front", getSelected().locked ? "Unlock" : "Lock", "Edit" };
				int x = -1;
				int y = -1;
				RSInterface child = null;
				if (getInterface().children != null) {
					for (int index = 0; index < getInterface().children.length; index++) {
						if (getInterface().children[index] == selectedId) {
							x = getInterface().childX[index];
							y = getInterface().childY[index];
							child = RSInterface.cache[getInterface().children[index]];
							break;
						} else {
							if (RSInterface.cache[getInterface().children[index]].children != null) {
								child = RSInterface.cache[getInterface().children[index]];
								for (int childIndex = 0; childIndex < child.children.length; childIndex++) {
									if (child.children[childIndex] == selectedId) {
										x = getInterface().childX[index] + child.childX[childIndex];
										y = getInterface().childY[index] + child.childY[childIndex];
										child = RSInterface.cache[child.children[childIndex]];
										break;
									}
								}
							}
						}
					}
				}
				if (child != null) {
					int color = 0xff00ff;
					int alpha = 50;
					RSDrawingArea.drawFilledAlphaPixels(x, y, child.width, child.height, color, alpha);
					RSDrawingArea.drawUnfilledPixels(x, y, child.width, child.height, color);
				}
			}
		}
		showLockedChildren();
		drawSelection();
		if (currentId != -1 && getInterface() != null) {
			drawSliders();
			if (menuOpen) {
				drawMenu(0, 0);
			}
		}
		if (Settings.displayData) {
			drawDataPane();
		}
		Graphics2D graphics = (Graphics2D) strategy.getDrawGraphics();
		imageProducer.drawGraphics(0, 0, graphics);
		drawGrid(graphics);
		debugMouse(graphics);
		graphics.dispose();
		strategy.show();
	}

	/**
	 * Displays the mouse location and mouse information.
	 * @param g
	 */
	public void debugMouse(Graphics g) {
		if (Constants.DEBUG_MOUSE) {
			if (mouseX != -1 && mouseY != -1) {
				g.setColor(new Color(0, 255, 255));
				int x = mouseX;
				int y = mouseY;
				g.drawLine(x, 0, x, getCanvasHeight());
				g.drawLine(0, y, getCanvasWidth(), y);
			}
		}
	}

	/**
	 * Adjusts the horizontal grid scale.
	 * @param newX
	 */
	public void adjustHorizontal(int newX) {
		if (getScale() != 1) {
			return;
		}
		if (newX == horizontalPos) {
			return;
		}
		int startX = horizontalPos;
		if (newX < (getSliderWidth() / 2)) {
			horizontalPos = (getSliderWidth() / 2);
			return;
		}
		if (newX > getCanvasWidth() - (getSliderWidth() / 2) - 8) {
			horizontalPos = getCanvasWidth() - (getSliderWidth() / 2) - 8;
			return;
		}
		horizontalPos = newX;
		int percent = (int) (((double) (horizontalPos - (getSliderWidth() / 2)) / (double) (getCanvasWidth() - (getSliderWidth() / 2) - 8)) * 100D);
		percent = (percent / 10) * 2;
		boolean left = startX - newX > 0 ? true : false;
		if (left) {
			if (horizontalScale > 1) {
				horizontalScale = percent > 0 ? percent : 1;
			}
		} else {
			if (horizontalScale < 20) {
				horizontalScale = percent < 20 ? percent : 20;
			}
		}
		adjustingGrid = true;
	}

	/**
	 * Adjusts the vertical grid scale.
	 * @param newX
	 */
	public void adjustVertical(int newX) {
		if (getScale() != 1) {
			return;
		}
		if (newX == verticalPos) {
			return;
		}
		int startX = verticalPos;
		if (newX < (getSliderHeight() / 2)) {
			verticalPos = (getSliderHeight() / 2);
			return;
		}
		if (newX > getCanvasHeight() - (getSliderHeight() / 2) - 8) {
			verticalPos = getCanvasHeight() - (getSliderHeight() / 2) - 8;
			return;
		}
		verticalPos = newX;
		int percent = (int) (((double) (verticalPos - (getSliderHeight() / 2)) / (double) (getCanvasHeight() - (getSliderHeight() / 2) - 8)) * 100D);
		percent = (percent / 10) * 2;
		boolean left = startX - newX > 0 ? true : false;
		if (left) {
			if (verticalScale > 1) {
				verticalScale = percent > 0 ? percent : 1;
			}
		} else {
			if (verticalScale < 20) {
				verticalScale = percent < 20 ? percent : 20;
			}
		}
		adjustingGrid = true;
	}
	boolean adjustingGrid = false;

	/**
	 * Processes mouse input for the interface editor.
	 */
	public void processInput() {
		if (getInterface() != null) {
			/* Interface children clicking */
			if (!menuOpen) {
				processChildClicking();
			}
			int thickness = 8;
			/* Grid adjustment */
			if (clickInRegion(0, getCanvasWidth() - thickness, getCanvasHeight() - thickness, getCanvasHeight())) {
				if (Settings.displayGrid) {
					if (getClickType() == ClickType.LEFT_CLICK || getClickType() == ClickType.LEFT_DRAG) {
						adjustHorizontal(mouseX);
					}
				}
			} else if (clickInRegion(getCanvasWidth() - thickness, getCanvasWidth(), 0, getCanvasHeight() - thickness)) {
				if (Settings.displayGrid) {
					if (getClickType() == ClickType.LEFT_CLICK || getClickType() == ClickType.LEFT_DRAG) {
						adjustVertical(mouseY);
					}
				}
			} else {
				adjustingGrid = false;
			}
			thickness = 12;
			if (!adjustingGrid) {
				/* Selection rectangle dragging */
				if (getClickType() == ClickType.LEFT_DRAG) {
					selectionX = clickX;
					selectionY = clickY;
					int distX = calculateDragDistance()[0];
					int distY = calculateDragDistance()[1];
					selectionWidth = distX;
					selectionHeight = distY;
				}
			}
			/* Right clicking menus and actions */
			if (getSelected() != null) {
				if (getClickType() == ClickType.RIGHT_CLICK && clickInRegion(getSelectedX(), getSelectedX() + getSelected().width, getSelectedY(), getSelectedY() + getSelected().height)) {
					determineMenuSize();
				}
				if (menuOpen) {
					if (getClickType() == ClickType.LEFT_CLICK) {
						int _clickX = clickX;
						int _clickY = clickY ;
						for(int action = 0; action < childActions.length; action++) {
							int posY = menuOffsetY + 31 + (childActions.length - 1 - action) * 15;
							if(_clickX > menuOffsetX && _clickX < menuOffsetX + menuWidth && _clickY > posY - 13 && _clickY < posY + 3) {
								childAction = action;
								perform(childAction);
							}
						}
					}
				}
			}
			/* Close menu when mouse leaves menu area */
			if (!mouseInRegion(menuOffsetX, menuOffsetX + menuWidth, menuOffsetY, menuOffsetY + menuHeight)) {
				menuOpen = false;
				childAction = -1;
			}
		}
	}

	public int childAction = -1;
	public String[] childActions = { "Remove", "Move down", "Move up", "Move to back", "Move to front", "Lock", "Edit" };

	public void perform(int action) {
		menuOpen = false;
		switch (action) {
			case 0:
				ActionHandler.removeSelected();
				break;
			case 1:
				ActionHandler.setZIndex(getSelectedIndex() - 1);
				break;
			case 2:
				ActionHandler.setZIndex(getSelectedIndex() + 1);
				break;
			case 3:
				ActionHandler.setZIndex(0);
				break;
			case 4:
				ActionHandler.setZIndex(getInterface().children.length - 1);
				break;
			case 5:
				if (getSelected().locked) {
					ActionHandler.unlock();
				} else {
					ActionHandler.lock();
				}
				UserInterface.ui.rebuildTreeList();
				break;
			case 6:
				ActionHandler.edit(getSelected());
				break;
		}
	}

	public void processChildClicking() {
		RSInterface rsi = getInterface();
		int offsetY = 0;
		int _mouseX = mouseX;
		int _mouseY = mouseY;
		int _clickX = clickX;
		int _clickY = clickY;
		if (rsi.type != 0 || rsi.children == null || rsi.showInterface) {
			return;
		}
		if (_mouseX < 0 || _mouseY < 0 || _mouseX > 0 + rsi.width || _mouseY > 0 + rsi.height) {
			return;
		}
		if (_clickX < 0 || _clickY < 0 || _clickX > 0 + rsi.width || _clickY > 0 + rsi.height) {
			return;
		}
		hoverId = -1;
		int childCount = rsi.children.length;
		for(int index = 0; index < childCount; index++) {
			int posX = rsi.childX[index];
			int posY = rsi.childY[index] - offsetY;
			RSInterface child = RSInterface.cache[rsi.children[index]];
			posX += child.drawOffsetX;
			posY += child.drawOffsetY;
			if (mouseInRegion(posX, posX + child.width, posY, posY + child.height)) {
				if (getClickType() == ClickType.MOVED) {
					hoverId = child.id;
				}
				if (getClickType() == ClickType.CTRL_DRAG && selectedId != -1 && selectedId != currentId && getScale() == 1) {
					ActionHandler.setSelectedX(_mouseX - (getSelected().width / 2));
					ActionHandler.setSelectedY(_mouseY - (getSelected().height / 2));
					return;
				}
			}
			if (clickInRegion(posX, posX + child.width, posY, posY + child.height)) {
				if (getClickType() == ClickType.CTRL_RIGHT) {
					selectChild(index);
					determineMenuSize();
					return;
				}
				if (getClickType() == ClickType.CTRL_LEFT || getClickType() == ClickType.DOUBLE) {
					selectChild(index);
					return;
				}
			}
			boolean test = false;
			if (test && getClickType() == ClickType.LEFT_CLICK && clickInRegion(posX, posX + child.width, posY, posY + child.height)) {
				if(child.actionType == 1) {
					boolean flag = false;
					if(child.contentType != 0) {
						//TODO
					}
					if(!flag) {
						menuActionName[menuActionRow] = child.tooltip;
						menuActionID[menuActionRow] = 315;
						menuActionCmd3[menuActionRow] = child.id;
						menuActionRow++;
					}
				}
				if(child.actionType == 2) {
					String name = child.selectedActionName;
					if(name.indexOf(" ") != -1) {
						name = name.substring(0, name.indexOf(" "));
					}
					if (menuActionRow < menuActionName.length) {
						menuActionName[menuActionRow] = name + " @gre@" + child.spellName;
						menuActionID[menuActionRow] = 626;
						menuActionCmd3[menuActionRow] = child.id;
						menuActionRow++;
					}
				}
				if(child.actionType == 3) {
					if (menuActionRow < menuActionName.length) {
						menuActionName[menuActionRow] = "Close";
						menuActionID[menuActionRow] = 200;
						menuActionCmd3[menuActionRow] = child.id;
						menuActionRow++;
					}
				}
				if(child.actionType == 4) {
					if (menuActionRow < menuActionName.length) {
						menuActionName[menuActionRow] = child.tooltip;
						menuActionID[menuActionRow] = 169;
						menuActionCmd3[menuActionRow] = child.id;
						menuActionRow++;
					}
				}
				if(child.actionType == 5) {
					if (menuActionRow < menuActionName.length) {
						menuActionName[menuActionRow] = child.tooltip;
						menuActionID[menuActionRow] = 646;
						menuActionCmd3[menuActionRow] = child.id;
						menuActionRow++;
					}
				}
				if(child.actionType == 6 && !aBoolean1149) {
					if (menuActionRow < menuActionName.length) {
						menuActionName[menuActionRow] = child.tooltip;
						menuActionID[menuActionRow] = 679;
						menuActionCmd3[menuActionRow] = child.id;
						menuActionRow++;
					}
				}
				doAction(menuActionRow - 1);
			}
		}
	}

	/**
	 * Dumps a sprite with the specified name.
	 * @param id
	 * @param image
	 */
	public void dumpImage(RSImage image, String name) {
		File directory = new File(Constants.getCacheDirectory() + "rsimg/dump/");
		if (!directory.exists()) {
			directory.mkdir();
		}
		BufferedImage bi = new BufferedImage(image.myWidth, image.myHeight, BufferedImage.TYPE_INT_RGB);
		bi.setRGB(0, 0, image.myWidth, image.myHeight, image.myPixels, 0, image.myWidth);
		Image img = makeColorTransparent(bi, new Color(0, 0, 0));
		BufferedImage trans = imageToBufferedImage(img);
		try {
			File out = new File(Constants.getCacheDirectory() + "rsimg/dump/" + name + ".png");
			ImageIO.write(trans, "png", out);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Turns an Image into a BufferedImage.
	 * @param image
	 * @return
	 */
    private static BufferedImage imageToBufferedImage(Image image) {
        BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = bufferedImage.createGraphics();
        g2.drawImage(image, 0, 0, null);
        g2.dispose();
        return bufferedImage;
    }

    /**
     * Makes the specified color transparent in a buffered image.
     * @param im
     * @param color
     * @return
     */
    public static Image makeColorTransparent(BufferedImage im, final Color color) {
    	RGBImageFilter filter = new RGBImageFilter() {
    		public int markerRGB = color.getRGB() | 0xFF000000;
    		public final int filterRGB(int x, int y, int rgb) {
    			if ((rgb | 0xFF000000) == markerRGB) {
    				return 0x00FFFFFF & rgb;
    			} else {
    				return rgb;
    			}
    		}
    	};
    	ImageProducer ip = new FilteredImageSource(im.getSource(), filter);
    	return Toolkit.getDefaultToolkit().createImage(ip);
    }

	public void init() {
		main = this;
		//initClientFrame(getCanvasWidth(), getCanvasHeight());
		startRunnable(this, 1);
	}

	private void drawScrollbar(int x, int y, int height, int scrollPosition, int scrollMax, boolean isTransparent) {
		int barHeight = ((height - 32) * height) / scrollMax;
		if(barHeight < 8) {
			barHeight = 8;
		}
		int offsetY = ((height - 32 - barHeight) * scrollPosition) / (scrollMax - height);
		if (isTransparent) {
			int alpha = 40;
			int color = 0xFFFFFF;
			RSDrawingArea.drawFilledAlphaPixels(x + 7, y + 3, 2, 11, color, alpha);
			RSDrawingArea.drawFilledAlphaPixels(x + 6, y + 4, 1, 3, color, alpha);
			RSDrawingArea.drawFilledAlphaPixels(x + 5, y + 5, 1, 3, color, alpha);
			RSDrawingArea.drawFilledAlphaPixels(x + 4, y + 6, 1, 3, color, alpha);
			RSDrawingArea.drawFilledAlphaPixels(x + 3, y + 7, 1, 2, color, alpha);
			RSDrawingArea.drawFilledAlphaPixels(x + 9, y + 4, 1, 3, color, alpha);
			RSDrawingArea.drawFilledAlphaPixels(x + 10, y + 5, 1, 3, color, alpha);
			RSDrawingArea.drawFilledAlphaPixels(x + 11, y + 6, 1, 3, color, alpha);
			RSDrawingArea.drawFilledAlphaPixels(x + 12, y + 7, 1, 2, color, alpha);
			RSDrawingArea.drawVerticalAlphaLine(x, y + 16, height - 32, color, alpha);
			RSDrawingArea.drawVerticalAlphaLine(x + 15, y + 16, height - 32, color, alpha);
			RSDrawingArea.drawHorizontalAlphaLine(x, y + 17 + offsetY, 16, color, alpha);
			RSDrawingArea.drawVerticalAlphaLine(x, y + 18 + offsetY, barHeight - 3, color, alpha);
			RSDrawingArea.drawFilledAlphaPixels(x + 1, y + 17 + offsetY + 1, 1, 1, color, alpha);
			RSDrawingArea.drawFilledAlphaPixels(x + 14, y + 17 + offsetY + 1, 1, 1, color, alpha);
			RSDrawingArea.drawFilledAlphaPixels(x + 1, y + 18 + offsetY, 14, barHeight - 3, color, 15);
			RSDrawingArea.drawFilledAlphaPixels(x + 1, y + 14 + offsetY + barHeight, 1, 1, color, alpha);
			RSDrawingArea.drawFilledAlphaPixels(x + 14, y + 14 + offsetY + barHeight, 1, 1, color, alpha);
			RSDrawingArea.drawVerticalAlphaLine(x + 15, y + 18 + offsetY, barHeight - 3, color, alpha);
			RSDrawingArea.drawHorizontalAlphaLine(x, y + 15 + offsetY + barHeight, 16, color, alpha);
			RSDrawingArea.drawFilledAlphaPixels(x + 7, y + height - 14, 2, 11, color, alpha);
			RSDrawingArea.drawFilledAlphaPixels(x + 6, y + height - 7, 1, 3, color, alpha);
			RSDrawingArea.drawFilledAlphaPixels(x + 5, y + height - 8, 1, 3, color, alpha);
			RSDrawingArea.drawFilledAlphaPixels(x + 4, y + height - 9, 1, 3, color, alpha);
			RSDrawingArea.drawFilledAlphaPixels(x + 3, y + height - 9, 1, 2, color, alpha);
			RSDrawingArea.drawFilledAlphaPixels(x + 9, y + height - 7, 1, 3, color, alpha);
			RSDrawingArea.drawFilledAlphaPixels(x + 10, y + height - 8, 1, 3, color, alpha);
			RSDrawingArea.drawFilledAlphaPixels(x + 11, y + height - 9, 1, 3, color, alpha);
			RSDrawingArea.drawFilledAlphaPixels(x + 12, y + height - 9, 1, 2, color, alpha);
		} else {
			scrollBar1.drawImage(x, y);
			scrollBar2.drawImage(x, (y + height) - 16);
			RSDrawingArea.drawFilledPixels(x, y + 16, 16, height - 32, scrollBackground);
			RSDrawingArea.drawFilledPixels(x, y + 16 + offsetY, 16, barHeight, scrollFill);
			RSDrawingArea.drawVerticalLine(x, y + 16 + offsetY, barHeight, scrollLight);
			RSDrawingArea.drawVerticalLine(x + 1, y + 16 + offsetY, barHeight, scrollLight);
			RSDrawingArea.drawHorizontalLine(x, y + 16 + offsetY, 16, scrollLight);
			RSDrawingArea.drawHorizontalLine(x, y + 17 + offsetY, 16, scrollLight);
			RSDrawingArea.drawVerticalLine(x + 15, y + 16 + offsetY, barHeight, scrollDark);
			RSDrawingArea.drawVerticalLine(x + 14, y + 17 + offsetY, barHeight - 1, scrollDark);
			RSDrawingArea.drawHorizontalLine(x, y + 15 + offsetY + barHeight, 16, scrollDark);
			RSDrawingArea.drawHorizontalLine(x + 1, y + 14 + offsetY + barHeight, 15, scrollDark);
		}
	}

	public int getAmountColor(long amount) {
		int color = 0xFFFFFF;
		if (amount >= 1) {
			color = 0xFFFF00;
		}
		if (amount >= 100000) {
			color = 0xFFFFFF;
		}
		if (amount >= 10000000) {
			color = 0x00FF80;
		}
		if (amount >= 10000000000L) {
			color = 0x00FFFF;
		}
		return color;
	}

	public void process() {
		currentTime++;
		processInput();
		checkSize();
	}

	public void checkSize() {
		boolean resized = false;
		JInternalFrame viewport = UserInterface.viewport;
		if (viewport != null) {
			int width = viewport.getWidth() - (insets.left + insets.right);
			int height = viewport.getHeight() - (insets.top + insets.bottom);
			if (getCanvasWidth() != width) {
				resized = true;
			} else {
				resized = false;
			}
			if (getCanvasHeight() != height) {
				resized = true;
			} else {
				resized = false;
			}
			if (resized) {
				imageProducer = new RSImageProducer(getCanvasWidth(), getCanvasHeight(), this);
			}
		}
	}

	public static Main main;
	public static Main getInstance() {
		return main;
	}

	public static void main(String args[]) {
		Settings.checkDirectory();
		Settings.read();
		main = new Main();
		ActionHandler.main = main;
		new UserInterface();
	}

	public float progress;
	public void updateProgress(String string, int percent) {
        for(float f = progress; f < (float)percent; f = (float)((double)f + 0.29999999999999999D)) {
            displayProgress(string, (int)f);
        }
        progress = percent;
    }

	public void displayProgress(String string, int percent) {
		while (strategy == null)
		{
			createBufferStrategy(2);
			strategy = getBufferStrategy();
		}
		java.awt.Graphics2D graphics = (java.awt.Graphics2D) strategy.getDrawGraphics();
		int centerX = getCanvasWidth() / 2;
		int centerY = getCanvasHeight() / 2;
		int width = 300;
		int height = 30;
		int alpha = 150;
		int x = centerX - (width / 2);
		int y = centerY - (height / 2);
		int loaded = (width * percent) / 100;
		imageProducer.initDrawingArea();
		RSDrawingArea.drawFilledPixels(0, 0, getCanvasWidth(), getCanvasHeight(), Constants.BACKGROUND_COLOR);
		RSDrawingArea.drawRoundedRectangle(x, y, width, height, 0, alpha / 2, true, true);
		RSDrawingArea.drawRoundedRectangle(x, y, loaded, height, 0, alpha / 2, true, true);
		RSDrawingArea.drawRoundedRectangle(x, y, width, height, Constants.TEXT_COLOR, alpha, false, true);
		arial[1].drawStringCenter(string, centerX, centerY + 5, 0xFFFFFF, true);
		imageProducer.drawGraphics(0, 0, graphics);
		graphics.dispose();
		strategy.show();
	}

	/**
	 * Returns the archive for the specified index.
	 * 1 - title
	 * 3 - interfaces
	 * 4 - media
	 * @param index
	 * @return
	 */
	private Archive getArchive(int index) {
		byte data[] = null;
		if(cache.getIndice(0) != null) {
			data = cache.getIndice(0).get(index);
		}
		if(data != null) {
			Archive archive = new Archive(data);
			return archive;
		}
		return null;
	}

	private void doAction(int actionIndex) {
		if(actionIndex < 0) {
			return;
		}
		int id = menuActionCmd3[actionIndex];
		int action = menuActionID[actionIndex];
		if(action >= 2000) {
			action -= 2000;
		}
		if(action == 679 && !aBoolean1149) {
			aBoolean1149 = true;
		}
		if(action == 626) {
			RSInterface rsi = RSInterface.cache[id];
			String prefix = rsi.selectedActionName;
			if(prefix.indexOf(" ") != -1) {
				prefix = prefix.substring(0, prefix.indexOf(" "));
			}
			String suffix = rsi.selectedActionName;
			if(suffix.indexOf(" ") != -1) {
				suffix = suffix.substring(suffix.indexOf(" ") + 1);
			}
			return;
		}
		if(action == 646) {
			RSInterface rsi = RSInterface.cache[id];
			if(rsi.valueIndexArray != null && rsi.valueIndexArray[0][0] == 5) {
				int setting = rsi.valueIndexArray[0][1];
				if(variousSettings[setting] != rsi.requiredValues[0]) {
					variousSettings[setting] = rsi.requiredValues[0];
				}
			}
		}
		if(action == 169) {
			RSInterface rsi = RSInterface.cache[id];
			if(rsi.valueIndexArray != null && rsi.valueIndexArray[0][0] == 5) {
				int setting = rsi.valueIndexArray[0][1];
				variousSettings[setting] = 1 - variousSettings[setting];
			}
		}
	}

	public void cleanUpForQuit() {
		menuActionCmd3 = null;
		menuActionID = null;
		menuActionName = null;
		variousSettings = null;
		imageProducer = null;
		RSInterface.cache = null;
		System.gc();
	}

	void initialize() {
		updateProgress("IntelliRS is starting...", 20);
		try {
			updateProgress("Unpacking archives...", 40);
			titleArchive = getArchive(1);
			small = new RSFont(false, "p11_full", titleArchive);
			regular = new RSFont(false, "p12_full", titleArchive);
			bold = new RSFont(false, "b12_full", titleArchive);
			fancy = new RSFont(true, "q8_full", titleArchive);
			interfaces = getArchive(3);
			media = getArchive(4);
			mediaArchive = new MediaArchive(media);
			updateProgress("Unpacking media...", 65);
			scrollBar1 = new RSImage(media, "scrollbar", 0);
			scrollBar2 = new RSImage(media, "scrollbar", 1);
			updateProgress("Unpacking interfaces...", 95);
			RSFont fonts[] = { small, regular, bold, fancy };
			RSInterface.load(interfaces, media, fonts);
			mediaArchive.updateKnown();
			updateProgress("Complete!", 100);
			return;
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private String interfaceIntToString(int val) {
		if(val < 0x3b9ac9ff) {
			return String.valueOf(val);
		} else {
			return "*";
		}
	}

	private void drawInterface(RSInterface rsi, int pos_x, int pos_y, int offsetY) {
		if(rsi.type != 0 || rsi.children == null) {
			return;
		}
		if(rsi.showInterface && anInt1026 != rsi.id && anInt1048 != rsi.id && anInt1039 != rsi.id) {
			return;
		}
		int startX = RSDrawingArea.startX;
		int startY = RSDrawingArea.startY;
		int endX = RSDrawingArea.endX;
		int endY = RSDrawingArea.endY;
		RSDrawingArea.setBounds(pos_x, pos_x + rsi.width, pos_y, pos_y + rsi.height);
		int children = rsi.children.length;
		for(int index = 0; index < children; index++) {
			int x = rsi.childX[index] + pos_x;
			int y = (rsi.childY[index] + pos_y) - offsetY;
			RSInterface child = RSInterface.cache[rsi.children[index]];
			x += child.drawOffsetX;
			y += child.drawOffsetY;
			if(child.contentType > 0) {
				//drawFriendsListOrWelcomeScreen(child);
			}
			if(child.type == 0) {
				if(child.scrollPosition > child.scrollMax - child.height)
					child.scrollPosition = child.scrollMax - child.height;
				if(child.scrollPosition < 0) {
					child.scrollPosition = 0;
				}
				drawInterface(child, x, y, child.scrollPosition);
				if(child.scrollMax > child.height) {
					drawScrollbar(x + child.width, y, child.height, child.scrollPosition, child.scrollMax, false);
				}
			} else {
				if(child.type != 1)
					if(child.type == 2) {
						int item_index = 0;
						for(int itemY = 0; itemY < child.height; itemY++) {
							for(int itemX = 0; itemX < child.width; itemX++) {
								int item_x = x + itemX * (32 + child.invSpritePadX);
								int item_y = y + itemY * (32 + child.invSpritePadY);
								if(item_index < 20) {
									item_x += child.spritesX[item_index];
									item_y += child.spritesY[item_index];
								}
								if(child.inventory[item_index] > 0) {
									if(item_x > RSDrawingArea.startX - 32 && item_x < RSDrawingArea.endX && item_y > RSDrawingArea.startY - 32 && item_y < RSDrawingArea.endY || activeInterfaceType != 0 && anInt1085 == item_index) {
										/*RSImage image = ItemDefinitions.getImage(itemId, child.inventoryAmount[item_index], color);
										if(image != null) {
											if(activeInterfaceType != 0 && anInt1085 == item_index && anInt1084 == child.id) {
												offset_x = mouseX - anInt1087;
												offset_y = mouseY - anInt1088;
												if(offset_x < 5 && offset_x > -5) {
													offset_x = 0;
												}
												if(offset_y < 5 && offset_y > -5) {
													offset_y = 0;
												}
												if(anInt989 < 5) {
													offset_x = 0;
													offset_y = 0;
												}
												image.drawImage(item_x + offset_x, item_y + offset_y, 128);
												if(item_y + offset_y < RSDrawingArea.startY && rsi.scrollPosition > 0) {
													int scrollPos = (anInt945 * (RSDrawingArea.startY - item_y - offset_y)) / 3;
													if(scrollPos > anInt945 * 10) {
														scrollPos = anInt945 * 10;
													}
													if(scrollPos > rsi.scrollPosition) {
														scrollPos = rsi.scrollPosition;
													}
													rsi.scrollPosition -= scrollPos;
													anInt1088 += scrollPos;
												}
												if(item_y + offset_y + 32 > RSDrawingArea.endY && rsi.scrollPosition < rsi.scrollMax - rsi.height) {
													int scrollPos = (anInt945 * ((item_y + offset_y + 32) - RSDrawingArea.endY)) / 3;
													if(scrollPos > anInt945 * 10) {
														scrollPos = anInt945 * 10;
													}
													if(scrollPos > rsi.scrollMax - rsi.height - rsi.scrollPosition) {
														scrollPos = rsi.scrollMax - rsi.height - rsi.scrollPosition;
													}
													rsi.scrollPosition += scrollPos;
													anInt1088 -= scrollPos;
												}
											} else {
												if(atInventoryInterfaceType != 0 && atInventoryIndex == item_index && atInventoryInterface == child.id) {
													image.drawImage(item_x, item_y, 128);
												} else {
													image.drawImage(item_x, item_y);
												}
											}
											if(image.maxWidth == 33 || child.inventoryAmount[item_index] != 1) {
												int amount = child.inventoryAmount[item_index];
												small.drawShadowedString(getAmountString(amount), item_x + offset_x, item_y + offset_y + 9, getAmountColor(amount), true);
											}
										}*/
									}
								} else
									if(child.sprites != null && item_index < 20) {
										RSImage image = child.sprites[item_index];
										if(image != null) {
											image.drawImage(item_x, item_y);
										}
									}
								item_index++;
							}
						}
					} else if(child.type == 3) {
						boolean hovered = false;
						if(anInt1039 == child.id || anInt1048 == child.id || anInt1026 == child.id) {
							hovered = true;
						}
						int color;
						if(isEnabled(child)) {
							color = child.enabledColor;
							if(hovered && child.enabledHoverColor != 0) {
								color = child.enabledHoverColor;
							}
						} else {
							color = child.disabledColor;
							if(hovered && child.disabledHoverColor != 0) {
								color = child.disabledHoverColor;
							}
						}
						if(child.alpha == 0) {
							if(child.filled) {
								RSDrawingArea.drawFilledPixels(x, y, child.width, child.height, color);
							} else {
								RSDrawingArea.drawUnfilledPixels(x, y, child.width, child.height, color);
							}
						} else {
							if(child.filled) {
								RSDrawingArea.drawFilledAlphaPixels(x, y, child.width, child.height, color, 256 - (child.alpha & 0xff));
							} else {
								RSDrawingArea.method338(y, child.height, 256 - (child.alpha & 0xff), color, child.width, x);
							}
						}
					} else if(child.type == 4) {
						RSFont[] fonts = { small, regular, bold, fancy };
						RSFont font = fonts[child.fontId];
						String text = child.disabledText;
						boolean hovered = hoverId == child.id;
						int color;
						if(isEnabled(child)) {
							color = child.enabledColor;
							if(hovered && child.enabledHoverColor != 0) {
								color = child.enabledHoverColor;
							}
							if(child.enabledText.length() > 0) {
								text = child.enabledText;
							}
						} else {
							color = child.disabledColor;
							if(hovered && child.disabledHoverColor != 0) {
								color = child.disabledHoverColor;
							}
						}
						if(child.actionType == 6 && aBoolean1149) {
							text = "Please wait...";
							color = child.disabledColor;
						}
						for(int textY = y + font.baseHeight; text.length() > 0; textY += font.baseHeight) {
							if(text.indexOf("%") != -1) {
								do {
									int valueIndex = text.indexOf("%1");
									if(valueIndex == -1)
										break;
									text = text.substring(0, valueIndex) + interfaceIntToString(extractValue(child, 0)) + text.substring(valueIndex + 2);
								} while(true);
								do {
									int valueIndex = text.indexOf("%2");
									if(valueIndex == -1)
										break;
									text = text.substring(0, valueIndex) + interfaceIntToString(extractValue(child, 1)) + text.substring(valueIndex + 2);
								} while(true);
								do {
									int valueIndex = text.indexOf("%3");
									if(valueIndex == -1)
										break;
									text = text.substring(0, valueIndex) + interfaceIntToString(extractValue(child, 2)) + text.substring(valueIndex + 2);
								} while(true);
								do {
									int valueIndex = text.indexOf("%4");
									if(valueIndex == -1)
										break;
									text = text.substring(0, valueIndex) + interfaceIntToString(extractValue(child, 3)) + text.substring(valueIndex + 2);
								} while(true);
								do {
									int valueIndex = text.indexOf("%5");
									if(valueIndex == -1)
										break;
									text = text.substring(0, valueIndex) + interfaceIntToString(extractValue(child, 4)) + text.substring(valueIndex + 2);
								} while(true);
							}
							int newLineIndex = text.indexOf("\\n");
							String finalText;
							if(newLineIndex != -1) {
								finalText = text.substring(0, newLineIndex);
								text = text.substring(newLineIndex + 2);
							} else {
								finalText = text;
								text = "";
							}
							if(child.centered) {
								font.drawCenteredString(finalText, x + child.width / 2, textY, color, child.shadowed);
							} else {
								font.drawShadowedString(finalText, x, textY, color, child.shadowed);
							}
						}
				} else if(child.type == 5) {
					RSImage sprite = null;
					if(isEnabled(child)) {
						sprite = child.enabledSprite;
					} else {
						sprite = child.disabledSprite;
					}
					if(sprite != null) {
						child.width = sprite.myWidth;
						child.height = sprite.myHeight;
						sprite.drawImage(x, y);
					}
				} else if(child.type == 6) {
					/*int k3 = Rasterizer.centerX;
					int j4 = Rasterizer.centerY;
					Rasterizer.centerX = x + child.width / 2;
					Rasterizer.centerY = y + child.height / 2;
					boolean enabled = isEnabled(child);
					int anim;
					if(enabled) {
						anim = child.enabledAnimation;
					} else {
						anim = child.disabledAnimation;
					}
					if(anim == -1) {
						//child.getAnimatedModel(-1, -1, enabled);
					}
					Rasterizer.centerX = k3;
					Rasterizer.centerY = j4;*/
				} else if(child.type == 7) {
					RSFont font = child.font;
					int k4 = 0;
					for(int j5 = 0; j5 < child.height; j5++) {
						for(int i6 = 0; i6 < child.width; i6++) {
							if(child.inventory[k4] > 0) {
								//ItemDefinitions itemDef = ItemDefinitions.getDefinition(child.inventory[k4] - 1);
								//String s2 = itemDef.name;
								//if(itemDef.stackable || child.inventoryAmount[k4] != 1)
								//	s2 = s2 + " x" + formatAmount(child.inventoryAmount[k4]);
								int i9 = x + i6 * (115 + child.invSpritePadX);
								int k9 = y + j5 * (12 + child.invSpritePadY);
								if(child.centered)
									font.drawCenteredString("", i9 + child.width / 2, k9, child.disabledColor, child.shadowed);
								else
									font.drawShadowedString("", i9, k9, child.disabledColor, child.shadowed);
							}
							k4++;
						}

					}
				} else if (child.type == 8 && (anInt1500 == child.id || anInt1044 == child.id || anInt1129 == child.id) && anInt1501 == 50) {
                    int boxWidth = 0;
					int boxHeight = 0;
					RSFont font = regular;
					for (String s1 = child.disabledText; s1.length() > 0;) {
						if (s1.indexOf("%") != -1) {
							do {
								int k7 = s1.indexOf("%1");
								if (k7 == -1)
									break;
								s1 = s1.substring(0, k7) + interfaceIntToString(extractValue(child, 0)) + s1.substring(k7 + 2);
							} while (true);
								do {
									int l7 = s1.indexOf("%2");
									if (l7 == -1)
										break;
									s1 = s1.substring(0, l7) + interfaceIntToString(extractValue(child, 1)) + s1.substring(l7 + 2);
								} while (true);
								do {
									int i8 = s1.indexOf("%3");
									if (i8 == -1)
										break;
									s1 = s1.substring(0, i8) + interfaceIntToString(extractValue(child, 2)) + s1.substring(i8 + 2);
								} while (true);
								do {
									int j8 = s1.indexOf("%4");
									if (j8 == -1)
										break;
									s1 = s1.substring(0, j8) + interfaceIntToString(extractValue(child, 3)) + s1.substring(j8 + 2);
								} while (true);
								do {
									int k8 = s1.indexOf("%5");
									if (k8 == -1)
										break;
									s1 = s1.substring(0, k8) + interfaceIntToString(extractValue(child, 4)) + s1.substring(k8 + 2);
								} while (true);
							}
							int l7 = s1.indexOf("\\n");
							String s4;
							if (l7 != -1) {
								s4 = s1.substring(0, l7);
								s1 = s1.substring(l7 + 2);
							} else {
								s4 = s1;
								s1 = "";
							}
							int j10 = font.getTextWidth(s4);
							if (j10 > boxWidth) {
								boxWidth = j10;
							}
							boxHeight += font.baseHeight + 1;
						}
						boxWidth += 6;
						boxHeight += 7;
						int xPos = (x + child.width) - 5 - boxWidth;
						int yPos = y + child.height + 5;
						if (xPos < x + 5)
							xPos = x + 5;
						if (xPos + boxWidth > pos_x + rsi.width)
							xPos = (pos_x + rsi.width) - boxWidth;
						if (yPos + boxHeight > offsetY + rsi.height)
							yPos = (y - boxHeight);
						RSDrawingArea.drawFilledPixels(xPos, yPos, boxWidth, boxHeight, 0xFFFFA0);
						RSDrawingArea.drawUnfilledPixels(xPos, yPos, boxWidth, boxHeight, 0);
						String s2 = child.disabledText;
						for (int j11 = yPos + font.baseHeight + 2; s2.length() > 0; j11 += font.baseHeight + 1) {
							if (s2.indexOf("%") != -1) {
								do {
									int k7 = s2.indexOf("%1");
									if (k7 == -1)
										break;
									s2 = s2.substring(0, k7) + interfaceIntToString(extractValue(child, 0)) + s2.substring(k7 + 2);
								} while (true);
								do {
									int l7 = s2.indexOf("%2");
									if (l7 == -1)
										break;
									s2 = s2.substring(0, l7) + interfaceIntToString(extractValue(child, 1)) + s2.substring(l7 + 2);
								} while (true);
								do {
									int i8 = s2.indexOf("%3");
									if (i8 == -1)
										break;
									s2 = s2.substring(0, i8) + interfaceIntToString(extractValue(child, 2)) + s2.substring(i8 + 2);
								} while (true);
								do {
									int j8 = s2.indexOf("%4");
									if (j8 == -1)
										break;
									s2 = s2.substring(0, j8) + interfaceIntToString(extractValue(child, 3)) + s2.substring(j8 + 2);
								} while (true);
								do {
									int k8 = s2.indexOf("%5");
									if (k8 == -1)
										break;
									s2 = s2.substring(0, k8) + interfaceIntToString(extractValue(child, 4)) + s2.substring(k8 + 2);
								} while (true);
							}
							int l11 = s2.indexOf("\\n");
							String s5;
							if (l11 != -1) {
								s5 = s2.substring(0, l11);
								s2 = s2.substring(l11 + 2);
							} else {
								s5 = s2;
								s2 = "";
							}
							if (child.centered) {
								font.drawCenteredString(s5, xPos + child.width / 2, yPos, 0, false);
							} else {
								if (s5.contains("\\r")) {
									String text = s5.substring(0, s5.indexOf("\\r"));
									String text2 = s5.substring(s5.indexOf("\\r") + 2);
									font.drawBasicString(text, xPos + 3, j11, 0);
									int rightX = boxWidth + xPos - font.getTextWidth(text2) - 2;
									font.drawBasicString(text2, rightX, j11, 0);
									System.out.println("Box: " + boxWidth + "");
								} else
									font.drawBasicString(s5, xPos + 3, j11, 0);
							}
						}
				}
			}
		}
		RSDrawingArea.setBounds(startX, endX, startY, endY);
	}

	public void processDrawing() {
		displayInterfacePane();
	}

	private int menuOffsetX;
	private int menuOffsetY;
	private int menuWidth;
	private int menuHeight;

	private void drawMenu(int offsetX, int offsetY) {
		int x = menuOffsetX - offsetX;
		int y = menuOffsetY - offsetY;
		int width = menuWidth;
		int height = menuHeight;
		RSDrawingArea.drawRoundedRectangle(x, y, width, height, 0, 220, true, false);
		RSDrawingArea.drawFilledAlphaPixels(x + 1, y + 1, width - 2, 16, 0x2C2C2C, 150);
		RSDrawingArea.drawRoundedRectangle(x, y, width, height, 0xffffff, 220, false, true);
		RSDrawingArea.drawHorizontalAlphaLine(x + 1, y + 17, width - 1, 0xFFFFFF, 220);
		arial[1].drawString("Choose Action", x + 3, y + 14, 0xFFFFFF, true);
		int _mouseX = mouseX - offsetX;
		int _mouseY = mouseY - offsetY;
		for(int action = 0; action < childActions.length; action++) {
			int posY = y + 31 + (childActions.length - 1 - action) * 15;
			int color = Constants.TEXT_COLOR;
			if(_mouseX > x && _mouseX < x + width && _mouseY > posY - 13 && _mouseY < posY + 3) {
				color = 0xFFFFFF;
				RSDrawingArea.drawFilledAlphaPixels(x + 2, posY - 13, menuWidth - 4, 16, 0x9F9F9F, 220);
			}
			arial[1].drawString(childActions[action], x + 3, posY, color, true);
		}
	}

	private void determineMenuSize() {
		int width = regular.getEffectTextWidth("Choose Option");
		for(int action = 0; action < childActions.length; action++) {
			int itemWwidth = bold.getEffectTextWidth(childActions[action]);
			if(itemWwidth > width) {
				width = itemWwidth;
			}
		}
		width += 8;
		int height = 15 * childActions.length + 21;
		int startX =  0;
		int endX = getCanvasWidth();
		int startY = 0;
		int endY = getCanvasHeight();
		if(clickX > startX && clickY > startY && clickX < endX && clickY < endY) {
			int x = clickX - startX - width / 2;
			if(x + width > (endX - startX)) {
				x = (endX - startX) - width;
			}
			if(x < 0) {
				x = 0;
			}
			int y = clickY - startY;
			if(y + height > (endY - startY)) {
				y = (endY - startY) - height;
			}
			if(y < 0) {
				y = 0;
			}
			menuOpen = true;
			menuOffsetX = x;
			menuOffsetY = y;
			menuWidth = width;
			menuHeight = height;
		}
	}

	private int extractValue(RSInterface rsi, int valueIndex) {
		if(rsi.valueIndexArray == null || valueIndex >= rsi.valueIndexArray.length) {
			return -2;
		}
		try {
			int opcodes[] = rsi.valueIndexArray[valueIndex];
			int result = 0;
			int counter = 0;
			int type = 0;
			do {
				int opcode = opcodes[counter++];
				int value = 0;
				byte tempType = 0;
				switch (opcode) {
					case 0:
						return result;
					case 1:
						//returned = currentStats[valueArray[valuePointer++]];
						break;
					case 2:
						//returned = maxStats[valueArray[valuePointer++]];
						break;
					case 3:
						//returned = currentExp[valueArray[valuePointer++]];
						break;
					case 4:
					/*int k2 = valueArray[valuePointer++];
						if(k2 >= 0 && k2 < ItemDefinitions.totalItems && (!ItemDefinitions.getDefinition(k2).membersObject || isMembers)) {
							for(int j3 = 0; j3 < child.inventory.length; j3++) {
								if(child.inventory[j3] == k2 + 1) {
									returned += child.inventoryAmount[j3];
								}
							}
						}*/
						break;
					case 5:
						value = variousSettings[opcodes[counter++]];
						break;
					case 6:
						//returned = anIntArray1019[maxStats[valueArray[valuePointer++]] - 1];
						break;
					case 7:
						value = (variousSettings[opcodes[counter++]] * 100) / 46875;
						break;
					case 8:
						break;
					case 9:
						break;
					case 10:
						break;
					case 11:
						//returned = energy;
						break;
					case 12:
						//returned = weight;
						break;
					case 13:
						int i2 = variousSettings[opcodes[counter++]];
						int i3 = opcodes[counter++];
						value = (i2 & 1 << i3) == 0 ? 0 : 1;
						break;
					case 14:
						/*int j2 = valueArray[valuePointer++];
						VarBit varBit = VarBit.cache[j2];
						int l3 = varBit.anInt648;
						int i4 = varBit.anInt649;
						int j4 = varBit.anInt650;
						int k4 = anIntArray1232[j4 - i4];
						returned = variousSettings[l3] >> i4 & k4;*/
						break;
					case 15:
						tempType = 1;
						break;
					case 16:
						tempType = 2;
						break;
					case 17:
						tempType = 3;
						break;
					case 20:
						value = opcodes[counter++];
						break;

					default:
						break;
				}
				if(tempType == 0) {
					if(type == 0)
						result += value;
					if(type == 1)
						result -= value;
					if(type == 2 && value != 0)
						result /= value;
					if(type == 3)
						result *= value;
					type = 0;
				} else {
					type = tempType;
				}
			} while(true);
		} catch(Exception _ex) {
			return -1;
		}
	}


	private boolean isEnabled(RSInterface rsi) {
		if(rsi.valueCompareType == null) {
			return false;
		}
		if (Settings.forceEnabled) {
			return true;
		}
		for(int index = 0; index < rsi.valueCompareType.length; index++) {
			int value = extractValue(rsi, index);
			int required = rsi.requiredValues[index];
			if(rsi.valueCompareType[index] == 2) {
				if(value >= required) {
					return false;
				}
			} else {
				if(rsi.valueCompareType[index] == 3) {
					if(value <= required) {
						return false;
					}
				} else {
					if(rsi.valueCompareType[index] == 4) {
						if(value == required) {
							return false;
						}
					} else {
						if(value != required) {
							return false;
						}
					}
				}
			}
		}
		return true;
	}

	public boolean mouseInRegion(int x1, int x2, int y1, int y2) {
		if (getScale() != 1) {
			x2 = (int) ((x2 - x1) * getScale());
			x1 *= getScale();
			x2 += x1;
			y2 = (int) ((y2 - y1) * getScale());
			y1 *= getScale();
			y2 += y1;
			x1 += Main.scaledX;
			x2 += Main.scaledX;
			y1 += Main.scaledY;
			y2 += Main.scaledY;
		}
		if (mouseX >= x1 && mouseX <= x2 && mouseY >= y1 && mouseY <= y2) {
			return true;
		}
		return false;
	}

	public boolean clickInRegion(int x1, int x2, int y1, int y2) {
		if (getScale() != 1) {
			x2 = (int) ((x2 - x1) * getScale());
			x1 *= getScale();
			x2 += x1;
			y2 = (int) ((y2 - y1) * getScale());
			y1 *= getScale();
			y2 += y1;
			x1 += Main.scaledX;
			x2 += Main.scaledX;
			y1 += Main.scaledY;
			y2 += Main.scaledY;
		}
		if (clickX >= x1 && clickX <= x2 && clickY >= y1 && clickY <= y2) {
			return true;
		}
		return false;
	}

	public double getScale() {
		if (zoom < 100) {
			zoom = 100;
		}
		if (zoom > 250) {
			zoom = 250;
		}
		return (zoom / 100D);
	}

	public int getCanvasWidth() {
		return getWidth();
	}

	public int getCanvasHeight() {
		return getHeight();
	}

	public Main() {
		cache = new Cache();
		imageProducer = new RSImageProducer(765, 503, this);
		RSDrawingArea.setAllPixelsToZero();
		arial = new RealFont[]{ new RealFont(this, "Arial", 0, 10, true), new RealFont(this, "Arial", 0, 12, true), new RealFont(this, "Arial", 0, 14, true) };
		arialColor = 0xD8D8D8;;
		progress = 0.0F;
		menuOpen = false;
		scrollLight = 0x766654;
		scrollDark = 0x332d25;
		variousSettings = new int[2000];
		scrollBackground = 0x23201b;
		scrollFill = 0x4d4233;
		menuActionCmd3 = new int[500];
		menuActionID = new int[500];
		aBoolean1149 = false;
		menuActionName = new String[500];
		zoom = 100;
		scaledX = 0;
		scaledY = 0;
	}

	public static int verticalPos;
	public static int horizontalPos;
	public int selectionX = -1;
	public int selectionY = -1;
	public int selectionWidth = 0;
	public int selectionHeight = 0;
	public MediaArchive mediaArchive;
	public static Cache cache;
	public int zoom;
	public static int scaledX;
	public static int scaledY;
	public static Archive media;
	public static Archive interfaces;
	public RealFont[] arial;
	public int arialColor;
	public static int currentId = -1;
	public static int selectedId = -1;
	public static int hoverId = -1;
	public int[] alpha = { 0, 0, 0, 0 };

	private boolean menuOpen;
	private final int scrollLight;
	private final int scrollDark;
	public int variousSettings[];
	private final int scrollBackground;
	private RSImage scrollBar1;
	private RSImage scrollBar2;
	private int anInt1026;
	private int anInt1039;
	private int anInt1048;
	private Archive titleArchive;
	private final int scrollFill;
	private int anInt1085;
	private int activeInterfaceType;
	static int anInt1089;
	private int[] menuActionCmd3;
	private int[] menuActionID;
	private RSImageProducer imageProducer;
	private int menuActionRow;
	private boolean aBoolean1149;
	public static int currentTime;
	private String[] menuActionName;
	public RSFont small;
	public RSFont regular;
	public RSFont bold;
	public RSFont fancy;
	public static int anInt1290;
    public int anInt1044;
    public int anInt1129;
    public int anInt1315;
    public int anInt1500;
    public int anInt1501;

	public void run() {
		MyMouseListener mouseListener = new MyMouseListener(getInstance());
		addMouseListener(mouseListener);
		addMouseMotionListener(mouseListener);
		addMouseWheelListener(mouseListener);
		addKeyListener(new MyKeyListener());
		addFocusListener(this);
		displayProgress("Loading...", 0);
		initialize();
		int opos = 0;
		int ratio = 256;
		int delay = 1;
		int count = 0;
		int intex = 0;
		for(int index = 0; index < 10; index++) {
			times[index] = System.currentTimeMillis();
		}
		do {
			if(timeRunning < 0) {
				break;
			}
			if(timeRunning > 0) {
				timeRunning--;
				if(timeRunning == 0) {
					exit();
					return;
				}
			}
			int k1 = ratio;
			int i2 = delay;
			ratio = 300;
			delay = 1;
			long systemTime = System.currentTimeMillis();
			if(times[opos] == 0L) {
				ratio = k1;
				delay = i2;
			} else if(systemTime > times[opos]) {
				ratio = (int)((long)(2560 * delayTime) / (systemTime - times[opos]));
			}
			if(ratio < 25) {
				ratio = 25;
			}
			if(ratio > 256) {
				ratio = 256;
				delay = (int)((long)delayTime - (systemTime - times[opos]) / 10L);
			}
			if(delay > delayTime) {
				delay = delayTime;
			}
			times[opos] = systemTime;
			opos = (opos + 1) % 10;
			if(delay > 1) {
				for(int index = 0; index < 10; index++) {
					if(times[index] != 0L) {
						times[index] += delay;
					}
				}
			}
			if(delay < minDelay) {
				delay = minDelay;
			}
			try {
				Thread.sleep(delay);
			} catch(InterruptedException e) {
				intex++;
			}
			for(; count < 256; count += ratio) {
				aLong29 = clickTime;
				process();
			}
			count &= 0xff;
			if(delayTime > 0) {
				fps = (1000 * ratio) / (delayTime * 256);
			}
			processDrawing();
			if(shouldDebug) {
				System.out.println("ntime:" + systemTime);
				for(int index = 0; index < 10; index++) {
					int otim = ((opos - index - 1) + 20) % 10;
					System.out.println("otim" + otim + ":" + times[otim]);
				}
				System.out.println("fps:" + fps + " ratio:" + ratio + " count:" + count);
				System.out.println("del:" + delay + " deltime:" + delayTime + " mindel:" + minDelay);
				System.out.println("intex:" + intex + " opos:" + opos);
				shouldDebug = false;
				intex = 0;
			}
		} while(true);
		if(timeRunning == -1) {
			exit();
		}
	}

	private void exit() {
		timeRunning = -2;
		cleanUpForQuit();
	}

	public void startRunnable(Runnable runnable, int priority) {
		Thread thread = new Thread(runnable);
		thread.start();
		thread.setPriority(priority);
	}

	final void setDelayTime(int time) {
		delayTime = 1000 / time;
	}

	public final void start() {
		if(timeRunning >= 0) {
			timeRunning = 0;
		}
	}

	public final void stop() {
		if(timeRunning >= 0) {
			timeRunning = 4000 / delayTime;
		}
	}

	public final void destroy() {
		timeRunning = -1;
		try {
			Thread.sleep(5000L);
		} catch(Exception e) {
		}
		if(timeRunning == -1) {
			exit();
		}
	}

	public void updateMouse(int mouseX, int mouseY, int clickX, int clickY, int idleTime, long clickTime, ClickType clickType) {
		this.mouseX = mouseX;
		this.mouseY = mouseY;
		this.clickX = clickX;
		this.clickY = clickY;
		this.idleTime = idleTime;
		this.clickTime = clickTime;
		this.clickType = clickType;
	}

	public ClickType clickType;

	public ClickType getClickType() {
		return clickType;
	}

	public int idleTime;
	public int mouseX;
	public int mouseY;
	public int clickX;
	public int clickY;
	public long clickTime;

	public String titleText = "";
	public static int hotKey = 508;
	private int timeRunning;
	private int delayTime;
	int minDelay;
	private final long times[] = new long[10];
	int fps;
	boolean shouldDebug;
	int myWidth;
	int myHeight;
	public Insets insets = new Insets(30, 5, 5, 5);
	public boolean isApplet;
	boolean awtFocus;
	long aLong29;
	protected final int keyArray[] = new int[128];
	protected final int charQueue[] = new int[128];
	protected int writeIndex;
	public static int anInt34;
	private BufferStrategy strategy;

	@Override
	public void windowActivated(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosed(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosing(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeactivated(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeiconified(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowIconified(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowOpened(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void focusGained(FocusEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void focusLost(FocusEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}