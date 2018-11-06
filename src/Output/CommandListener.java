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
public interface CommandListener {
	
        public void commandOutput(String text);

        public void commandCompleted(int result);

        public void commandFailed(Exception exp);
}
