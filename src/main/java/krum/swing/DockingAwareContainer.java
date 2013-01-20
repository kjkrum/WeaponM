package krum.swing;

import java.awt.Dimension;
import java.awt.Point;

/**
 * If a container implementing this interface is used as the content pane of a
 * <tt>JDockingPanel</tt>, it will be notified when the panel is docked or
 * floated.  The container can then change its content or layout depending on
 * whether it is docked or floating.
 *
 * @author Kevin Krumwiede (kjkrum@gmail.com)
 */
public interface DockingAwareContainer {
	/**
	 * Called after this container has been removed from its
	 * <tt>DockingPanel</tt> and before it has been placed in its
	 * <tt>JFrame</tt>.
	 */
	public void floating();
	
	/**
	 * Called after this container has been removed from its <tt>JFrame</tt>
	 * and before it has been placed in its <tt>JDockingPanel</tt>.
	 */
	public void docking();
	
	/**
	 * Allows this container to determine where the float button is overlaid
	 * when the container is docked.  Called after <tt>docking</tt>.  If this
	 * method returns <tt>null</tt>, the docking panel will use the default
	 * button position.
	 * 
	 * @param buttonSize
	 * @return
	 */
	public Point getPreferredButtonPosition(Dimension buttonSize);
}
