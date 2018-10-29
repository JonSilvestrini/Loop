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

/**
 *
 * @author potato
 */
public class PainelEdicao {
	
	private JTextArea jTextArea1 = new JTextArea();
	private JScrollPane jScrollPane1 = new JScrollPane();
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
		jTextArea1.setBackground(new java.awt.Color(18, 31, 53));
		jTextArea1.setColumns(20);
		jTextArea1.setFont(new java.awt.Font("Arial", 0, 15)); // NOI18N
		jTextArea1.setForeground(new java.awt.Color(255, 255, 255));
		jTextArea1.setRows(5);
		jTextArea1.setAlignmentX(1.0F);
		jTextArea1.setAlignmentY(1.0F);
		jTextArea1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
		jTextArea1.setInheritsPopupMenu(true);
		jTextArea1.setName(""); // NOI18N
		jTextArea1.setLineWrap(true);
		jScrollPane1.setViewportView(jTextArea1);
                if (this.arquivo!=null)
                    loadArquivosPainel();
		return jScrollPane1;
	}
	
	private void loadArquivosPainel() throws FileNotFoundException, IOException {
                this.jTextArea1.setText("");
		this.arq = this.arquivo.getAbsolutePath();
                System.out.println(arq);
		Scanner scanner = new Scanner(arquivo);		
		while (scanner.hasNext()) {
			this.jTextArea1.append(scanner.nextLine() + "\n");
		}
		scanner.close();		
	}
}
