/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Output;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author user
 */
public class StreamReader extends Thread {

	private InputStream is;
	private CommandListener listener;

	public StreamReader(CommandListener listener, InputStream is) {
		this.is = is;
		this.listener = listener;
		start();
	}

	@Override
	public void run() {
		try {
			int value = -1;
			while ((value = is.read()) != -1) {
				listener.commandOutput(Character.toString((char) value));
			}
		} catch (IOException exp) {
			exp.printStackTrace();
		}
	}
}
