/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Formatação;

/**
 *
 * @author user
 */
public class CaixaDeTexto {
	public static String Gerar(String texto) {
		
		char canto = '+';
        char linha = '=';
        char lateral = '|';

        String linha1 = "" + canto;
        for (int i = 0; i < (texto.length() + 2); i++) {
            linha1 += linha;
        }
        linha1 += canto + "\n";
        String linha2 = lateral + " " + texto + " " + lateral + "\n";
        return linha1 + linha2 + linha1;
	}
}
