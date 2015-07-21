/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package icons;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class TrayUI extends TrayIcon implements PropertyChangeListener {

  private JPopupMenu menu;
  private LaunchLogDialog logDialog;

  private static javax.swing.JFrame frame;
  private static final JDialog dialog;
  private static Image image;
  int x;
  int y;
  private final int port;
  private static boolean showPopup = false;

  static {
    dialog = new JDialog((Frame) null);
    dialog.setUndecorated(true);
    dialog.setAlwaysOnTop(true);
  }
  private static PopupMenuListener popupListener = new PopupMenuListener() {

    @Override
    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
      showPopup = true;
    }

    @Override
    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
      dialog.setVisible(false);
      showPopup = false;
    }

    @Override
    public void popupMenuCanceled(PopupMenuEvent e) {
      dialog.setVisible(false);
      showPopup = false;
    }
  };

  public TrayUI(Image image, int port) {
    super(image);
    TrayUI.image = image;
    this.port = port;
    frame = new javax.swing.JFrame("Karamel");
    logDialog = new LaunchLogDialog(frame, port, image);
    frame.setIconImage(new ImageIcon(TrayUI.image).getImage());
    logDialog.setResizable(true);
    EventQueue.invokeLater(new Runnable() {
      @Override
      public void run() {
        createGui();
      }
    });

    addMouseListener(new MouseAdapter() {

      @Override
      public void mouseClicked(MouseEvent e) {

        if (SwingUtilities.isRightMouseButton(e)) {
          if (showPopup == false) {
            showJPopupMenu(e);
          } else {
            dialog.setVisible(false);
          }
        } else {
          logDialog.setLocation(x + 200, y + 200);
          logDialog.pack();
          logDialog.setVisible(true);
          int state = frame.getExtendedState();
          state &= ~JFrame.ICONIFIED;
          frame.setExtendedState(state);
          logDialog.setAlwaysOnTop(false);
          logDialog.toFront();
          logDialog.requestFocus();

        }

      }

      @Override
      public void mousePressed(MouseEvent e) {
//                showJPopupMenu(e);
      }

      @Override
      public void mouseReleased(MouseEvent e) {
//                showJPopupMenu(e);
      }

      @Override
      public void mouseExited(MouseEvent e) {
//                showJPopupMenu(e);
      }

      @Override
      public void mouseEntered(MouseEvent e) {
//                super.mouseEntered(e);
      }
    });

  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {

  }

  protected void showJPopupMenu(MouseEvent e) {
    if (menu != null) {
      showJPopupMenu(e.getXOnScreen(), e.getYOnScreen());
    }
  }

  protected void showJPopupMenu(int x, int y) {
    Dimension size = menu.getPreferredSize();
    dialog.setLocation(x, y - size.height);
    dialog.setVisible(true);
    menu.show(dialog.getContentPane(), 0, 0);
    // popup works only for focused windows
    dialog.toFront();
  }

  public JPopupMenu getJPopupMenu() {
    return menu;
  }

  public void setJPopupMenu(JPopupMenu menu) {
    if (this.menu != null) {
      this.menu.removePopupMenuListener(popupListener);
    }
    this.menu = menu;
    menu.addPopupMenuListener(popupListener);
  }

  private void createGui() {

    setJPopupMenu(createJPopupMenu());
    try {
            // TODO: Bug in setting transparency of SystemTray Icon in 
      // Linux - http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6453521
      SystemTray.getSystemTray().add(this);
    } catch (AWTException e) {
      e.printStackTrace();
    }
  }

  JPopupMenu createJPopupMenu() {
    final JPopupMenu m = new JPopupMenu();
    final int xCoords = m.getX();
    final int yCoords = m.getY();

    x = xCoords;
    y = yCoords;

    JMenuItem exitItem = new JMenuItem("Exit");
    exitItem.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        System.out.println("Shutdown clicked. Exiting Karamel...");
        System.exit(0);
      }
    });
    m.add(exitItem);
    return m;
  }
}
