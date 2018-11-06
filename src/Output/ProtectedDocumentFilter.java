/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Output;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

/**
 *
 * @author user
 */
public class ProtectedDocumentFilter extends DocumentFilter {

	private UserInput userInput;

	public ProtectedDocumentFilter(UserInput userInput) {
		this.userInput = userInput;
	}

	public UserInput getUserInput() {
		return userInput;
	}

	@Override
	public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
		if (offset >= getUserInput().getUserInputStart()) {
			super.insertString(fb, offset, string, attr);
		}
	}

	@Override
	public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
		if (offset >= getUserInput().getUserInputStart()) {
			super.remove(fb, offset, length); //To change body of generated methods, choose Tools | Templates.
		}
	}

	@Override
	public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
		if (offset >= getUserInput().getUserInputStart()) {
			super.replace(fb, offset, length, text, attrs); //To change body of generated methods, choose Tools | Templates.
		}
	}
}
