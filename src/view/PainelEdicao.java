/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package view;

import java.io.BufferedReader;
import java.io.File;
import static java.io.FileDescriptor.in;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;

/**
 *
 * @author potato
 */
public class PainelEdicao {
	
	private JTextArea areaTexto = new JTextArea();
	private JScrollPane painelScroll = new JScrollPane();
	private File arquivo;
	private String arq;
	
	public PainelEdicao() {
	}
	
	public PainelEdicao(File $arq) {
		this.arquivo = $arq;
	}
	
	public File getArquivo() {
		return this.arquivo;
	}

    public String getArq() {
        return arq;
    }
        
        
	
	public JScrollPane montarPainel() throws IOException {
		areaTexto.setBackground(new java.awt.Color(18, 31, 53));
		areaTexto.setColumns(20);
		areaTexto.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
		areaTexto.setForeground(new java.awt.Color(255, 255, 255));
		areaTexto.setRows(5);
		areaTexto.setAlignmentX(1.0F);
		areaTexto.setAlignmentY(1.0F);
		areaTexto.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
		areaTexto.setInheritsPopupMenu(true);
		areaTexto.setName(""); // NOI18N
		areaTexto.setLineWrap(true);
		painelScroll.setViewportView(areaTexto);
                if (this.arquivo!=null)
                    loadArquivosPainel();
		return painelScroll;
	}
	
	private void loadArquivosPainel() throws FileNotFoundException, IOException {
		this.areaTexto.setText("");
		this.arq = this.arquivo.getAbsolutePath();
                System.out.println(arq);
		Scanner scanner = new Scanner(arquivo);		
		while (scanner.hasNext()) {
			this.areaTexto.append(scanner.nextLine() + "\n");
		}
		scanner.close();		
	}
}
