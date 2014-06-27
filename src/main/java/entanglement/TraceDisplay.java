package entanglement;

import javax.swing.JEditorPane;

public interface TraceDisplay<IdType, ValType, TraceType> {
	void setUpperPane(JEditorPane pane, TraceSet<IdType, ValType, TraceType> traces, TraceType selected);
	void setLowerPane(JEditorPane pane, TraceSet<IdType, ValType, TraceType> traces, TraceType selected);
}
