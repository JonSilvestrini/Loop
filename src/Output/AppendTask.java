/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Output;

/**
 *
 * @author user
 */
public class AppendTask implements Runnable {

	private Terminal terminal;
	private String text;

	public AppendTask(Terminal textArea, String text) {
		this.terminal = textArea;
		this.text = text;
	}

	@Override
	public void run() {
		terminal.appendText(text);
	}
}
