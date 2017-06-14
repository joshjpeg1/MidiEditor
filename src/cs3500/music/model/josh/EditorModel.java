package cs3500.music.model.josh;

import cs3500.music.model.EditorOperations;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the model for a Midi Editor and an implementation of the EditorOperations interface.
 */
public class EditorModel implements EditorOperations {
  private List<Piece> pieces;
  private Piece opened;

  /**
   * Creates a new {@code EditorModel} with no pieces in memory.
   */
  public EditorModel() {
    this.pieces = new ArrayList<>();
    this.opened = null;
  }

  @Override
  public void create(String title) throws IllegalArgumentException {
    if (this.getPieceFromMemory(title) != null) {
      throw new IllegalArgumentException("Piece already exists with given title.");
    }
    Piece next = new Piece(title);
    this.pieces.add(0, next);
    this.opened = next;
  }

  @Override
  public void open(String title) throws IllegalArgumentException {
    Piece piece = this.getPieceFromMemory(title);
    if (piece == null) {
      throw new IllegalArgumentException("There is no piece that exists with the given title, \""
        + title + "\".");
    }
    this.opened = piece;
  }

  @Override
  public void copy(String toCopy, String newTitle) throws IllegalArgumentException {
    Piece piece = this.getPieceFromMemory(toCopy);
    if (piece == null) {
      throw new IllegalArgumentException("There is no piece to copy that exists with the given "
        + "title, \"" + toCopy + "\".");
    } else if (this.getPieceFromMemory(newTitle) != null) {
      throw new IllegalArgumentException("Piece already exists with given new title.");
    }
    Piece copy = new Piece(newTitle);
    copy.overlay(piece);
    this.pieces.add(0, copy);
    this.opened = copy;
  }

  /**
   * Helper to the create, open, and copy methods. Gets the piece from the list of pieces in the
   * model, or null if no such piece exists.
   *
   * @param title   the title of the desired piece
   * @return the piece with the matching title, or null if no piece has the given title
   * @throws IllegalArgumentException if the given title is uninitialized
   */
  private Piece getPieceFromMemory(String title) throws IllegalArgumentException {
    for (Piece p : this.pieces) {
      if (p.getTitle().equals(title)) {
        return p;
      }
    }
    return null;
  }

  @Override
  public String view() {
    this.openedPieceException();
    return this.opened.toString();
  }

  @Override
  public void close() throws IllegalStateException {
    this.openedPieceException();
    this.opened = null;
  }

  @Override
  public String list() {
    StringBuilder builder = new StringBuilder();
    for (Piece p : this.pieces) {
      if (p.equals(opened)) {
        builder.append(">  ");
      } else {
        builder.append("   ");
      }
      builder.append(p.getTitle() + "\n");
    }
    return builder.toString();
  }

  @Override
  public void addNote(int start, int end, int instrument, int pitch, int volume)
    throws IllegalStateException, IllegalArgumentException {
    this.openedPieceException();
    this.opened.addNote(getOctave(pitch), getPitch(pitch), start, getDuration(start, end),
      instrument, volume);
  }

  @Override
  public void removeNote(int start, int instrument, int pitch)
    throws IllegalStateException, IllegalArgumentException {
    this.openedPieceException();
    this.opened.removeNote(getOctave(pitch), getPitch(pitch), start);
  }

  @Override
  public void editNotePitch(int start, int instrument, int pitch, int editedPitch)
    throws IllegalStateException, IllegalArgumentException {
    this.openedPieceException();
    int octave = getOctave(pitch);
    if (octave != getOctave(editedPitch)) {
      throw new IllegalArgumentException("Cannot edit octave, only pitch.");
    }
    this.opened.editPitch(octave, getPitch(pitch), start, getPitch(editedPitch));
  }

  @Override
  public void editNotePosition(int start, int instrument, int pitch, int editedStart)
    throws IllegalStateException, IllegalArgumentException {
    this.openedPieceException();
    this.opened.editPosition(getOctave(pitch), getPitch(pitch), start, editedStart);
  }

  @Override
  public void editNoteDuration(int start, int instrument, int pitch, int editedEnd)
    throws IllegalStateException, IllegalArgumentException {
    this.openedPieceException();
    this.opened.editDuration(getOctave(pitch), getPitch(pitch), start, editedEnd);
  }

  private Pitch getPitch(int pitch) {
    if (pitch < 0 || pitch > 127) {
      throw new IllegalArgumentException();
    }
    return Pitch.values()[pitch % Pitch.values().length];
  }

  private int getOctave(int pitch) {
    if (pitch < 0 || pitch > 127) {
      throw new IllegalArgumentException();
    }
    return pitch / Pitch.values().length;
  }

  private int getDuration(int start, int end) {
    return (start - end) + 1;
  }

  @Override
  public void setTempo(int tempo) {
    this.openedPieceException();
    this.opened.setTempo(tempo);
  }

  @Override
  public void overlay(String overlayTitle) throws IllegalStateException, IllegalArgumentException {
    this.openedPieceException();
    Piece toOverlay = this.getPieceFromMemory(overlayTitle);
    if (toOverlay == null) {
      throw new IllegalArgumentException("There is no piece with the given title, \""
        + overlayTitle + "\".");
    }
    this.opened.overlay(new Piece(toOverlay));
  }

  @Override
  public void addToEnd(String title) throws IllegalStateException, IllegalArgumentException {
    this.openedPieceException();
    Piece toAdd = this.getPieceFromMemory(title);
    if (toAdd == null) {
      throw new IllegalArgumentException("There is no piece with the given title, \""
        + title + "\".");
    }
    toAdd = new Piece(toAdd);
    toAdd.move(this.opened.length());
    this.opened.overlay(toAdd);
  }

  /**
   * Helper to the print, close, addNote, removeNote, editNotePitch, editNotePosition,
   * editNoteDuration, and overlay methods. Checks if there is currently a piece opened, and if
   * not throws an exception.
   *
   * @throws IllegalStateException if there is currently no piece opened
   */
  private void openedPieceException() throws IllegalStateException {
    if (this.opened == null) {
      throw new IllegalStateException("There is no piece currently open.");
    }
  }
}