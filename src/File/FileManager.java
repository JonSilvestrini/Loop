/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package File;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JONATASWILLIAMSILVES
 */
public class FileManager {
    
    private File path;
    private List<File> arquivos = new ArrayList<File>();
    
    public FileManager(File $path){
        this.path = $path;
    }
    
    public List<File> ScanFiles(){
        try {
            for (final File arquivo : this.path.listFiles()){
                if (arquivo.getName().endsWith(".classe")){
                    arquivos.add(arquivo);
                }
            }
        } catch (Exception e) {
            System.out.println("Erro: " + e);
        }
        return arquivos;
    }
    
}
