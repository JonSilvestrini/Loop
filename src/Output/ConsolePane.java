/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Output;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;

/**
 *
 * @author user
 */
public class ConsolePane extends JPanel implements CommandListener, Terminal {

	private JTextArea textArea;
	private int userInputStart = 0;
	private Command cmd;
	

	public ConsolePane() {

		cmd = new Command(this);

		setLayout(new BorderLayout());
		textArea = new JTextArea(20, 30);
		textArea.setFont(new java.awt.Font("Monospaced", 0, 11));
		((AbstractDocument) textArea.getDocument()).setDocumentFilter(new ProtectedDocumentFilter(this));
		add(new JScrollPane(textArea));
	}
 	public void executar(String comando) {


		InputMap im = textArea.getInputMap(WHEN_FOCUSED);
		ActionMap am = textArea.getActionMap();

		Action oldAction = am.get("insert-break");
		cmd.execute(comando);
		am.put("insert-break", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int range = textArea.getCaretPosition() - userInputStart;
				try {
					String text = textArea.getText(userInputStart, range).trim();
					System.out.println("[" + text + "]");
					userInputStart += range;
					if (cmd.isRunning()) {
						try {
							cmd.send(text + "\n");
						} catch (IOException ex) {
							appendText("!! Failed to send command to process: " + ex.getMessage() + "\n");
						}
					}
				} catch (BadLocationException ex) {

				}
				oldAction.actionPerformed(e);
			}
		});
	}

	@Override
	public void commandOutput(String text) {
		SwingUtilities.invokeLater(new AppendTask(this, text));
	}

	@Override
	public void commandFailed(Exception exp) {
		SwingUtilities.invokeLater(new AppendTask(this, "Command failed - " + exp.getMessage()));
	}

	@Override
	public void commandCompleted(int result) {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException ex) {
		}
		if (result == 0) {
			appendText("\nTermino bem sucedido!\n");
		}else {
			appendText("\nPrograma terminou com erros\n");
		}
	}

	protected void updateUserInputPos() {
		int pos = textArea.getCaretPosition();
		textArea.setCaretPosition(textArea.getText().length());
		userInputStart = pos;

	}

	@Override
	public int getUserInputStart() {
		return userInputStart;
	}

	@Override
	public void appendText(String text) {
		textArea.append(text);
		updateUserInputPos();
	}

	public void clearText(){
		textArea.setText("");
	}
}
