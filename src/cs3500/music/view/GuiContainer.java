package cs3500.music.view;

import cs3500.music.model.MusicEditorOperations;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseEvent;

/**
 * Represents the main container panel in the {@link GuiView}. This panel contains the
 * editor panel (encapsulated in a scroll pane), as well as the piano panel.
 */
public class GuiContainer extends JPanel {
  private final StringBuilder log;
  private final PianoPanel pianoPanel;
  private final EditorPanel editorPanel;
  private final MusicEditorOperations model;
  private final JScrollPane editorContainer;

  /**
   * Constructs a new {@code GuiContainer} using the given model. Sets the width of the container
   * to the given width.
   *
   * @param model   the model be represented in the panels held in the container
   * @param width   the preferred width of the container
   * @throws IllegalArgumentException if the given model is uninitialized, or the given width is
   *                                  negative or zero
   */
  protected GuiContainer(MusicEditorOperations model, int width) throws IllegalArgumentException {
    if (model == null) {
      throw new IllegalArgumentException("Given model is uninitialized.");
    } else if (width <= 0) {
      throw new IllegalArgumentException("Width cannot be negative or zero.");
    }
    this.log = new StringBuilder();
    this.model = model;
    this.setLayout(new BorderLayout(0, 0));
    int contHeight = 500;
    // Adds the editor panel to a scroll pane, then adds the scroll pane
    this.editorPanel = new EditorPanel(this.model, width, contHeight);
    this.editorContainer = new JScrollPane(this.editorPanel,
        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    this.editorContainer.setPreferredSize(new Dimension(width, contHeight));
    this.add(this.editorContainer, BorderLayout.NORTH);
    // Adds the piano panel and starts piano at note 0
    this.pianoPanel = new PianoPanel(this.model.getNotesAtBeat(0), width);
    this.add(this.pianoPanel, BorderLayout.SOUTH);
  }

  /**
   * Updates the position of the cursor in the editor view, as well as the keys highlighted in
   * the piano view. Won't move the cursor past the limitations of the editor (before 0 or after
   * the last beat displayed).
   *
   * @param forward   moves cursor forward if true, backward if false
   */
  protected void updatePosition(boolean forward) {
    int beat = this.editorPanel.updateCursor(forward);
    this.pianoPanel.updateHighlights(this.model.getNotesAtBeat(beat));
    repaint();
  }

  /**
   * Moves the cursor to the very beginning of the piece (position 0). Updates the piano panel as
   * well to reflect this change.
   */
  protected void jumpToBeginning() {
    while (this.editorPanel.getCursorPosition() > 0) {
      this.editorPanel.updateCursor(false);
    }
    this.pianoPanel.updateHighlights(this.model.getNotesAtBeat(0));
    repaint();
  }

  /**
   * Moves the cursor to the very end of the piece (length of the piece). Updates the piano panel as
   * well to reflect this change.
   */
  protected void jumpToEnd() {
    while (this.editorPanel.getCursorPosition() < this.model.getLength()) {
      this.editorPanel.updateCursor(true);
    }
    this.pianoPanel.updateHighlights(this.model.getNotesAtBeat(this.model.getLength()));
    repaint();
  }

  /**
   * Gets the current position of the cursor in the editor view.
   *
   * @return the current position of the cursor
   */
  protected int getCursorPosition() {
    return this.editorPanel.getCursorPosition();
  }

  /**
   * Gets the log of all drawing operations that have occurred in this container at the point of
   * this method being called.
   *
   * @return the log of operations as a String
   */
  protected String getLog() {
    return this.log.append(this.editorPanel.getLog()).append(this.pianoPanel.getLog()).toString();
  }

  /**
   * Gets the note data for the note created when a key is pressed on the piano view, with the
   * pitch of the note being that of the key pressed. The note will start at the current cursor's
   * position and be one beat long. It will be played on the piano instrument at volume 64.
   * If the mouse did not press on a key, null will be returned instead.
   *
   * @param e   the event of a mouse press
   * @return the note data created from the press of a key on the piano
   * @throws IllegalArgumentException if the given mouse event is uninitialized
   */
  protected Integer[] getNote(MouseEvent e) throws IllegalArgumentException {
    if (e == null) {
      throw new IllegalArgumentException("Cannot pass uninitialized mouse event.");
    }
    int x = e.getX();
    int y = e.getY() - this.editorContainer.getHeight();
    int pitch = pianoPanel.getPitch(x, y);
    if (pitch > 0) {
      int start = editorPanel.getCursorPosition();
      return new Integer[] {start, start + 1, 1, pitch, 64};
    } else {
      return null;
    }
  }

  /**
   * Updates both of the panels with the most up to date information from the model.
   */
  protected void updatePanels() {
    this.editorPanel.update(this.model, this.getWidth(), this.getHeight());
    this.pianoPanel.updateHighlights(this.model.getNotesAtBeat(this.getCursorPosition()));
  }

  /**
   * Toggles scrolling on and off, for when adding new notes to the end of a piece. This allows
   * it to momentarily stop scrolling and then resume to push the cursor forward.
   *
   * @param on   true to turn scrolling on, false otherwise
   */
  protected void scrollToggle(boolean on) {
    this.editorPanel.scrollToggle(on);
  }
}
