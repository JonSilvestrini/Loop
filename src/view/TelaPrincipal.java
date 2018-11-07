/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package view;

import File.FileManager;
import Formatação.CaixaDeTexto;
import java.awt.Color;
import java.awt.Component;
import java.awt.Image;
import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javabeans.Arquivo;
import javabeans.Token;
import javabeans.TokensGenBasico;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import portugoloo.input.ScanFiles;
import portugoloo.interpretador.*;
import portugoloo.lexico.Converte;
import Output.ConsolePane;
import java.awt.Dimension;
import java.awt.LayoutManager;

/**
 *
 * @author Usuário
 */
public class TelaPrincipal extends javax.swing.JFrame {

    /**
     * Creates new form TelaPrincipal
     */
    private File pastaProjeto;
    private FileManager fm;
    private List<PainelEdicao> conteudoAbas = new ArrayList<>();
    private PainelEdicao painelGenerico;
    private JTextArea funcoesTexto = new JTextArea();
	private ConsolePane consolePane = new ConsolePane();
	private Arquivo arquivoMain;

    private Compilador comp;

    public TelaPrincipal() throws IOException {
        URL url = this.getClass().getResource("/img/IconeLoop.png");
        Image iconeTitulo = Toolkit.getDefaultToolkit().getImage(url);
        this.setIconImage(iconeTitulo);
        selecionarPastaProjeto();
        this.fm = new FileManager(this.pastaProjeto);
        initComponents();
		CarregarOutput();
		this.OutputInternalFrame.getContentPane().add(consolePane);
		this.OutputInternalFrame.setPreferredSize(new Dimension(200,200));
		this.OutputInternalFrame.setSize(this.OutputInternalFrame.getPreferredSize());
		this.OutputInternalFrame.setVisible(true);
        carregarArquivos();
        comp = new Compilador();

        comp.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent pce) {
                OutputTextUpdate((String) pce.getNewValue());
            }
        });

		


    }
   	public void CarregarOutput() {
		javax.swing.GroupLayout OutputInternalFrameLayout = new javax.swing.GroupLayout(OutputInternalFrame.getContentPane());
				OutputInternalFrame.getContentPane().setLayout(OutputInternalFrameLayout);
        OutputInternalFrameLayout.setHorizontalGroup(
            OutputInternalFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(consolePane)
        );
        OutputInternalFrameLayout.setVerticalGroup(
            OutputInternalFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(consolePane, javax.swing.GroupLayout.DEFAULT_SIZE, 197, Short.MAX_VALUE)
        );
			
	}
    public void selecionarPastaProjeto() {
        JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(new java.io.File("."));
        fc.setDialogTitle("Selecione a pasta do Projeto");
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setAcceptAllFileFilterUsed(false);
        fc.showDialog(fc, "Abrir Pasta");
        this.pastaProjeto = fc.getSelectedFile();
    }

    public void Compilar() {

        for (int i = 0; i < abasTexto.getComponentCount(); i++) {
            try {
                salvar(i);
            } catch (IOException ex) {
                Logger.getLogger(TelaPrincipal.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        ScanFiles scanner = new ScanFiles();

        String path = pastaProjeto.getAbsolutePath().toString();

        OutputTextUpdate(CaixaDeTexto.Gerar("Procurando em pasta: " + path));

        scanner.setPath(path);
        try {
            scanner.ScanClasses();
        } catch (IOException ex) {
            OutputTextUpdate(CaixaDeTexto.Gerar("Erro ao encontrar os arquivos .classe"));
            JOptionPane.showMessageDialog(rootPane, "Erro ao encontrar os arquivos .classe\nErro:" + ex.getMessage(), "Deu ruim", 0);
            return;
        }

        if (scanner.getListaArquivos().isEmpty()) {
            OutputTextUpdate(CaixaDeTexto.Gerar("Erro ao encontrar os arquivos .classe"));
            return;
        }

        List<Arquivo> arquivos = scanner.getListaArquivos();

        List<Token> tokens = new TokensGenBasico().getTokens();




        Converte conversor = new Converte(tokens);
        conversor.converter(arquivos);

        Interprete inter = new Interprete(tokens);
        inter.interpretar(conversor.getListaInter());

		for (final Arquivo arq : inter.getListaJava()) {
			if (arq.isMain()) {
				arquivoMain = arq;
			}
		}

		
        comp.setArquivos(inter.getListaJava());
        comp.setPath(path);
        try {
            OutputTextUpdate(CaixaDeTexto.Gerar("Compilando os arquivos..."));
            if (comp.CleanCompile()) {
                OutputTextUpdate(CaixaDeTexto.Gerar("Compilado com sucesso!"));
				consolePane.executar("java -classpath " + path+"/output/"+" " + arquivoMain.getNomeArq() );
            } else {
                OutputTextUpdate(CaixaDeTexto.Gerar("Erro ao Compilar"));
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(rootPane, "Erro ao compilar o projeto\nerro:" + ex.getMessage(), "deu ruim", 0);
            OutputTextUpdate(CaixaDeTexto.Gerar("Erro ao compilar!"));
        }
    }

    public void carregarArquivos() throws IOException {
        System.out.println(abasTexto.getComponentCount());
        if (abasTexto.getComponentCount() != 0) {
            for (int i = 1; i <= abasTexto.getComponentCount(); i++) {
                abasTexto.removeTabAt(i);
            }
        }
        conteudoAbas.clear();
        List<File> files = fm.ScanFiles();
        if (files.isEmpty()) {
            painelGenerico = new PainelEdicao();
            abasTexto.addTab("Novo Arquivo", painelGenerico.montarPainel());
            conteudoAbas.add(painelGenerico);
        } else {
            for (final File arq : files) {
                painelGenerico = new PainelEdicao(arq);
                abasTexto.addTab(arq.getName(), painelGenerico.montarPainel());
                conteudoAbas.add(painelGenerico);
            }
        }
    }

    public void OutputTextUpdate(String texto) {

//        txtOutput.setCaretPosition(txtOutput.getDocument().getLength());
		consolePane.appendText(texto+"\n");
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenu2 = new javax.swing.JMenu();
        jPopupMenuOutput = new javax.swing.JPopupMenu();
        jMenuItemLimparOutput = new javax.swing.JMenuItem();
        jPanel4 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jButton7 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        btnNovoArquivo = new javax.swing.JButton();
        btnAbrir = new javax.swing.JButton();
        btnSalvar = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jButton11 = new javax.swing.JButton();
        bntExecutar = new javax.swing.JButton();
        jButton13 = new javax.swing.JButton();
        jButton14 = new javax.swing.JButton();
        jButton15 = new javax.swing.JButton();
        jButton16 = new javax.swing.JButton();
        jSeparator6 = new javax.swing.JSeparator();
        jSeparator7 = new javax.swing.JSeparator();
        jSeparator8 = new javax.swing.JSeparator();
        jSeparator9 = new javax.swing.JSeparator();
        jSeparator5 = new javax.swing.JSeparator();
        OutputInternalFrame = new javax.swing.JInternalFrame();
        abasTexto = new javax.swing.JTabbedPane();
        jMenuBar2 = new javax.swing.JMenuBar();
        menuArquivo = new javax.swing.JMenu();
        itmNovoArquivo = new javax.swing.JMenuItem();
        MenuAbrir = new javax.swing.JMenuItem();
        jMenuSalvar = new javax.swing.JMenuItem();
        itmSalvarTudo = new javax.swing.JMenuItem();
        itmRecarregarProj = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        jMenuItem4 = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JPopupMenu.Separator();
        jMenuSair = new javax.swing.JMenuItem();
        menuEditar = new javax.swing.JMenu();
        itmDesfazer = new javax.swing.JMenuItem();
        jMenuItem7 = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        itmCortar = new javax.swing.JMenuItem();
        itmCopiar = new javax.swing.JMenuItem();
        itmColar = new javax.swing.JMenuItem();
        jMenuItem11 = new javax.swing.JMenuItem();
        jMenuItem12 = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        jMenuItem13 = new javax.swing.JMenuItem();
        jMenu5 = new javax.swing.JMenu();
        jMenuItemIniciar = new javax.swing.JMenuItem();
        jMenuItem15 = new javax.swing.JMenuItem();
        jMenuItem16 = new javax.swing.JMenuItem();
        jMenuItem17 = new javax.swing.JMenuItem();
        jMenu6 = new javax.swing.JMenu();
        jMenuItem18 = new javax.swing.JMenuItem();

        jMenu1.setText("File");
        jMenuBar1.add(jMenu1);

        jMenu2.setText("Edit");
        jMenuBar1.add(jMenu2);

        jMenuItemLimparOutput.setText("Limpar");
        jMenuItemLimparOutput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemLimparOutputActionPerformed(evt);
            }
        });
        jPopupMenuOutput.add(jMenuItemLimparOutput);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("LOOP");
        setBackground(new java.awt.Color(102, 102, 102));
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        jPanel4.setBackground(new java.awt.Color(102, 102, 102));

        jPanel2.setBackground(new java.awt.Color(51, 51, 51));
        jPanel2.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanel2.setAlignmentX(0.0F);
        jPanel2.setAlignmentY(0.0F);

        jButton7.setBackground(new java.awt.Color(51, 51, 51));
        jButton7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/Armazenamento_3.png"))); // NOI18N

        jButton8.setBackground(new java.awt.Color(51, 51, 51));
        jButton8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/banco_3.png"))); // NOI18N

        jButton9.setBackground(new java.awt.Color(51, 51, 51));
        jButton9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/Pesquisa_2.png"))); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jButton9, javax.swing.GroupLayout.PREFERRED_SIZE, 55, Short.MAX_VALUE)
            .addComponent(jButton8, javax.swing.GroupLayout.PREFERRED_SIZE, 55, Short.MAX_VALUE)
            .addComponent(jButton7, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 55, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton7, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton8, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton9, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel1.setBackground(new java.awt.Color(51, 51, 51));
        jPanel1.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));

        btnNovoArquivo.setBackground(new java.awt.Color(51, 51, 51));
        btnNovoArquivo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/novo_1.png"))); // NOI18N
        btnNovoArquivo.setBorder(null);
        btnNovoArquivo.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        btnNovoArquivo.setOpaque(false);
        btnNovoArquivo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNovoArquivoActionPerformed(evt);
            }
        });

        btnAbrir.setBackground(new java.awt.Color(51, 51, 51));
        btnAbrir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/abrir_1.png"))); // NOI18N
        btnAbrir.setBorder(null);
        btnAbrir.setOpaque(false);
        btnAbrir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAbrirActionPerformed(evt);
            }
        });

        btnSalvar.setBackground(new java.awt.Color(51, 51, 51));
        btnSalvar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/salvar_1.png"))); // NOI18N
        btnSalvar.setBorder(null);
        btnSalvar.setOpaque(false);
        btnSalvar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSalvarActionPerformed(evt);
            }
        });

        jButton4.setBackground(new java.awt.Color(51, 51, 51));
        jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/imprimir_1.png"))); // NOI18N
        jButton4.setBorder(null);
        jButton4.setOpaque(false);

        jButton5.setBackground(new java.awt.Color(51, 51, 51));
        jButton5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/desfazer1.png"))); // NOI18N
        jButton5.setBorder(null);
        jButton5.setOpaque(false);

        jButton6.setBackground(new java.awt.Color(51, 51, 51));
        jButton6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/refazer1.png"))); // NOI18N
        jButton6.setBorder(null);
        jButton6.setBorderPainted(false);
        jButton6.setOpaque(false);

        jButton11.setBackground(new java.awt.Color(51, 51, 51));
        jButton11.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/pesquisar.png"))); // NOI18N
        jButton11.setBorder(null);
        jButton11.setOpaque(false);

        bntExecutar.setBackground(new java.awt.Color(51, 51, 51));
        bntExecutar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/play.png"))); // NOI18N
        bntExecutar.setBorder(null);
        bntExecutar.setBorderPainted(false);
        bntExecutar.setOpaque(false);
        bntExecutar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                Executar(evt);
            }
        });

        jButton13.setBackground(new java.awt.Color(51, 51, 51));
        jButton13.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/playepause.png"))); // NOI18N
        jButton13.setBorder(null);
        jButton13.setBorderPainted(false);
        jButton13.setOpaque(false);

        jButton14.setBackground(new java.awt.Color(51, 51, 51));
        jButton14.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/stop.png"))); // NOI18N
        jButton14.setBorder(null);
        jButton14.setBorderPainted(false);
        jButton14.setOpaque(false);
        jButton14.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton14ActionPerformed(evt);
            }
        });

        jButton15.setBackground(new java.awt.Color(51, 51, 51));
        jButton15.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/recomeçar_1.png"))); // NOI18N
        jButton15.setBorder(null);
        jButton15.setBorderPainted(false);
        jButton15.setOpaque(false);

        jButton16.setBackground(new java.awt.Color(51, 51, 51));
        jButton16.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/ajuda_1.png"))); // NOI18N
        jButton16.setBorder(null);
        jButton16.setBorderPainted(false);
        jButton16.setOpaque(false);

        jSeparator6.setOrientation(javax.swing.SwingConstants.VERTICAL);

        jSeparator7.setOrientation(javax.swing.SwingConstants.VERTICAL);

        jSeparator8.setOrientation(javax.swing.SwingConstants.VERTICAL);

        jSeparator9.setOrientation(javax.swing.SwingConstants.VERTICAL);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(btnNovoArquivo, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnAbrir, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnSalvar, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator6, javax.swing.GroupLayout.PREFERRED_SIZE, 3, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator7, javax.swing.GroupLayout.PREFERRED_SIZE, 3, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton11, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator8, javax.swing.GroupLayout.PREFERRED_SIZE, 3, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(bntExecutar, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton13, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton14, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton15, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton16, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator9, javax.swing.GroupLayout.PREFERRED_SIZE, 3, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 278, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSeparator6)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                .addComponent(btnNovoArquivo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnAbrir, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnSalvar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(bntExecutar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton13, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jSeparator7)
                .addComponent(jSeparator8)
                .addComponent(jSeparator9))
        );

        jSeparator5.setCursor(new java.awt.Cursor(java.awt.Cursor.N_RESIZE_CURSOR));

        OutputInternalFrame.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        OutputInternalFrame.setTitle("Output");
        OutputInternalFrame.setVisible(true);

        javax.swing.GroupLayout OutputInternalFrameLayout = new javax.swing.GroupLayout(OutputInternalFrame.getContentPane());
        OutputInternalFrame.getContentPane().setLayout(OutputInternalFrameLayout);
        OutputInternalFrameLayout.setHorizontalGroup(
            OutputInternalFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 905, Short.MAX_VALUE)
        );
        OutputInternalFrameLayout.setVerticalGroup(
            OutputInternalFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 197, Short.MAX_VALUE)
        );

        abasTexto.setMinimumSize(new java.awt.Dimension(200, 200));
        abasTexto.setName("Novo Arquivo"); // NOI18N

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(OutputInternalFrame)
                    .addComponent(abasTexto, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jSeparator5, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(abasTexto, javax.swing.GroupLayout.DEFAULT_SIZE, 450, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSeparator5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(OutputInternalFrame, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        abasTexto.getAccessibleContext().setAccessibleName("Novo Arquivo");
        abasTexto.getAccessibleContext().setAccessibleDescription("");

        jMenuBar2.setBackground(new java.awt.Color(51, 51, 51));
        jMenuBar2.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jMenuBar2.setForeground(new java.awt.Color(255, 255, 255));
        jMenuBar2.setMinimumSize(new java.awt.Dimension(200, 200));

        menuArquivo.setText("Arquivo");
        menuArquivo.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        itmNovoArquivo.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        itmNovoArquivo.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        itmNovoArquivo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/Novo.png"))); // NOI18N
        itmNovoArquivo.setLabel("Novo");
        itmNovoArquivo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itmNovoArquivoActionPerformed(evt);
            }
        });
        menuArquivo.add(itmNovoArquivo);

        MenuAbrir.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        MenuAbrir.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        MenuAbrir.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/Abrir.png"))); // NOI18N
        MenuAbrir.setLabel("Abrir");
        MenuAbrir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MenuAbrirActionPerformed(evt);
            }
        });
        menuArquivo.add(MenuAbrir);

        jMenuSalvar.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        jMenuSalvar.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        jMenuSalvar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/Salvar.png"))); // NOI18N
        jMenuSalvar.setText("Salvar");
        jMenuSalvar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuSalvarActionPerformed(evt);
            }
        });
        menuArquivo.add(jMenuSalvar);

        itmSalvarTudo.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        itmSalvarTudo.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        itmSalvarTudo.setText("Salvar Tudo");
        itmSalvarTudo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itmSalvarTudoActionPerformed(evt);
            }
        });
        menuArquivo.add(itmSalvarTudo);

        itmRecarregarProj.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.ALT_MASK | java.awt.event.InputEvent.SHIFT_MASK));
        itmRecarregarProj.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        itmRecarregarProj.setText("Recarregar Projeto");
        itmRecarregarProj.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itmRecarregarProjActionPerformed(evt);
            }
        });
        menuArquivo.add(itmRecarregarProj);
        menuArquivo.add(jSeparator3);

        jMenuItem4.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_I, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem4.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        jMenuItem4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/Imprimir.png"))); // NOI18N
        jMenuItem4.setText("Imprimir");
        menuArquivo.add(jMenuItem4);
        menuArquivo.add(jSeparator4);

        jMenuSair.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F4, java.awt.event.InputEvent.ALT_MASK));
        jMenuSair.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        jMenuSair.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/Sair.png"))); // NOI18N
        jMenuSair.setText("Sair");
        jMenuSair.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuSairActionPerformed(evt);
            }
        });
        menuArquivo.add(jMenuSair);

        jMenuBar2.add(menuArquivo);

        menuEditar.setText("Editar");
        menuEditar.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        itmDesfazer.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z, java.awt.event.InputEvent.CTRL_MASK));
        itmDesfazer.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        itmDesfazer.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/Desfazer.png"))); // NOI18N
        itmDesfazer.setText("Desfazer");
        itmDesfazer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itmDesfazerActionPerformed(evt);
            }
        });
        menuEditar.add(itmDesfazer);

        jMenuItem7.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Y, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem7.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        jMenuItem7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/Refazer.png"))); // NOI18N
        jMenuItem7.setText("Refazer");
        menuEditar.add(jMenuItem7);
        menuEditar.add(jSeparator1);

        itmCortar.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.CTRL_MASK));
        itmCortar.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        itmCortar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/Cortar.png"))); // NOI18N
        itmCortar.setText("Cortar");
        itmCortar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itmCortarActionPerformed(evt);
            }
        });
        menuEditar.add(itmCortar);

        itmCopiar.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_MASK));
        itmCopiar.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        itmCopiar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/Copiar.png"))); // NOI18N
        itmCopiar.setText("Copiar");
        itmCopiar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itmCopiarActionPerformed(evt);
            }
        });
        menuEditar.add(itmCopiar);

        itmColar.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, java.awt.event.InputEvent.CTRL_MASK));
        itmColar.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        itmColar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/Colar.png"))); // NOI18N
        itmColar.setText("Colar");
        itmColar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                itmColarActionPerformed(evt);
            }
        });
        menuEditar.add(itmColar);

        jMenuItem11.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DELETE, 0));
        jMenuItem11.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        jMenuItem11.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/Deletar.png"))); // NOI18N
        jMenuItem11.setText("Delete");
        jMenuItem11.setToolTipText("");
        menuEditar.add(jMenuItem11);

        jMenuItem12.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem12.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        jMenuItem12.setText("Selecionar Tudo");
        menuEditar.add(jMenuItem12);
        menuEditar.add(jSeparator2);

        jMenuItem13.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem13.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        jMenuItem13.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/Procurar.png"))); // NOI18N
        jMenuItem13.setText("Procurar e Substituir");
        menuEditar.add(jMenuItem13);

        jMenuBar2.add(menuEditar);

        jMenu5.setText("Compilar");
        jMenu5.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        jMenuItemIniciar.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F6, 0));
        jMenuItemIniciar.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        jMenuItemIniciar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/Iniciar.png"))); // NOI18N
        jMenuItemIniciar.setText("Iniciar");
        jMenuItemIniciar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                IniciarPorMenu(evt);
            }
        });
        jMenu5.add(jMenuItemIniciar);

        jMenuItem15.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F6, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem15.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        jMenuItem15.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/Passo a Passo.png"))); // NOI18N
        jMenuItem15.setText("Passo a Passo");
        jMenu5.add(jMenuItem15);

        jMenuItem16.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F4, 0));
        jMenuItem16.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        jMenuItem16.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/Parar.png"))); // NOI18N
        jMenuItem16.setText("Parar");
        jMenu5.add(jMenuItem16);

        jMenuItem17.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F5, 0));
        jMenuItem17.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        jMenuItem17.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/Recomeçar.png"))); // NOI18N
        jMenuItem17.setText("Recomeçar");
        jMenu5.add(jMenuItem17);

        jMenuBar2.add(jMenu5);

        jMenu6.setText("Ajuda");
        jMenu6.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        jMenuItem18.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        jMenuItem18.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/Ajuda.png"))); // NOI18N
        jMenuItem18.setText("Suporte");
        jMenu6.add(jMenuItem18);

        jMenuBar2.add(jMenu6);

        setJMenuBar(jMenuBar2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

        private void Executar(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_Executar
            new Thread(new Runnable() {
                public void run() {
                    Compilar();
                }
            }).start();

			String path = pastaProjeto.getAbsolutePath().toString();

        }//GEN-LAST:event_Executar

    private void jMenuItemLimparOutputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemLimparOutputActionPerformed
//        txtOutput.setText("");
		consolePane.clearText();
    }//GEN-LAST:event_jMenuItemLimparOutputActionPerformed

    private void IniciarPorMenu(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_IniciarPorMenu
        new Thread(new Runnable() {
            public void run() {
                Compilar();
            }
        }).start();
    }//GEN-LAST:event_IniciarPorMenu

    private void jMenuSairActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuSairActionPerformed
        this.dispose();
    }//GEN-LAST:event_jMenuSairActionPerformed

    private void btnAbrirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAbrirActionPerformed
        JOptionPane.showMessageDialog(this, "Não tem ninguem");
    }//GEN-LAST:event_btnAbrirActionPerformed

    private void MenuAbrirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MenuAbrirActionPerformed
        JOptionPane.showMessageDialog(this, "Não tem ninguem");
    }//GEN-LAST:event_MenuAbrirActionPerformed

    private void jMenuSalvarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuSalvarActionPerformed
        for (int i = 0; i < abasTexto.getComponentCount(); i++) {
            if (abasTexto.getSelectedIndex() == i) {
                try {
                    salvar(i);
                } catch (IOException ex) {
                    Logger.getLogger(TelaPrincipal.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }//GEN-LAST:event_jMenuSalvarActionPerformed

    private void itmSalvarTudoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itmSalvarTudoActionPerformed
        // TODO add your handling code here:
        for (int i = 0; i < abasTexto.getComponentCount(); i++) {
            try {
                salvar(i);
            } catch (IOException ex) {
                Logger.getLogger(TelaPrincipal.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_itmSalvarTudoActionPerformed

    private void itmCopiarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itmCopiarActionPerformed
        // TODO add your handling code here:
        funcoesTexto.copy();
    }//GEN-LAST:event_itmCopiarActionPerformed

    private void itmCortarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itmCortarActionPerformed
        // TODO add your handling code here:
        funcoesTexto.cut();
    }//GEN-LAST:event_itmCortarActionPerformed

    private void itmColarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itmColarActionPerformed
        // TODO add your handling code here:
        funcoesTexto.paste();
    }//GEN-LAST:event_itmColarActionPerformed

    private void itmDesfazerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itmDesfazerActionPerformed
        // TODO add your handling code here:

    }//GEN-LAST:event_itmDesfazerActionPerformed

    private void btnNovoArquivoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNovoArquivoActionPerformed
        try {
            // TODO add your handling code here:
            novoArquivo();
        } catch (IOException ex) {
            Logger.getLogger(TelaPrincipal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnNovoArquivoActionPerformed

    private void itmNovoArquivoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itmNovoArquivoActionPerformed
        try {
            // TODO add your handling code here:
            novoArquivo();
        } catch (IOException ex) {
            Logger.getLogger(TelaPrincipal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_itmNovoArquivoActionPerformed

    private void btnSalvarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSalvarActionPerformed
        for (int i = 0; i < abasTexto.getComponentCount(); i++) {
            if (abasTexto.getSelectedIndex() == i) {
                try {
                    salvar(i);
                } catch (IOException ex) {
                    Logger.getLogger(TelaPrincipal.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }//GEN-LAST:event_btnSalvarActionPerformed

    private void itmRecarregarProjActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_itmRecarregarProjActionPerformed
        try {
            // TODO add your handling code here:
            carregarArquivos();
        } catch (IOException ex) {
            Logger.getLogger(TelaPrincipal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_itmRecarregarProjActionPerformed

    private void jButton14ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton14ActionPerformed
		OutputTextUpdate(CaixaDeTexto.Gerar("executando nslookup"));
		consolePane.executar("nslookup");
    }//GEN-LAST:event_jButton14ActionPerformed

    /**
     * @param args the command line arguments
     */
    public void salvar(int i) throws IOException {
        painelGenerico = new PainelEdicao();
        painelGenerico = conteudoAbas.get(i);
        if (painelGenerico.getArquivo() == null) {
            abasTexto.setSelectedIndex(i);
            JFileChooser fc = new JFileChooser();
            fc.setCurrentDirectory(pastaProjeto);
            fc.showSaveDialog(jPanel1);
            File arq = fc.getSelectedFile();
            arq.createNewFile();
            painelGenerico.setArquivo(arq);
            painelGenerico.saveFile();
            abasTexto.remove(abasTexto.getSelectedIndex());
            abasTexto.add(painelGenerico.montarPainel());
            abasTexto.setTitleAt(abasTexto.getSelectedIndex(), painelGenerico.getArq());
        } else {
            try {
                painelGenerico.saveFile();
            } catch (IOException ex) {
                Logger.getLogger(TelaPrincipal.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public void novoArquivo() throws IOException {
        painelGenerico = new PainelEdicao();
        abasTexto.addTab("Novo Arquivo", painelGenerico.montarPainel());
        conteudoAbas.add(painelGenerico);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem MenuAbrir;
    private javax.swing.JInternalFrame OutputInternalFrame;
    private javax.swing.JTabbedPane abasTexto;
    private javax.swing.JButton bntExecutar;
    private javax.swing.JButton btnAbrir;
    private javax.swing.JButton btnNovoArquivo;
    private javax.swing.JButton btnSalvar;
    private javax.swing.JMenuItem itmColar;
    private javax.swing.JMenuItem itmCopiar;
    private javax.swing.JMenuItem itmCortar;
    private javax.swing.JMenuItem itmDesfazer;
    private javax.swing.JMenuItem itmNovoArquivo;
    private javax.swing.JMenuItem itmRecarregarProj;
    private javax.swing.JMenuItem itmSalvarTudo;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton13;
    private javax.swing.JButton jButton14;
    private javax.swing.JButton jButton15;
    private javax.swing.JButton jButton16;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu5;
    private javax.swing.JMenu jMenu6;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuBar jMenuBar2;
    private javax.swing.JMenuItem jMenuItem11;
    private javax.swing.JMenuItem jMenuItem12;
    private javax.swing.JMenuItem jMenuItem13;
    private javax.swing.JMenuItem jMenuItem15;
    private javax.swing.JMenuItem jMenuItem16;
    private javax.swing.JMenuItem jMenuItem17;
    private javax.swing.JMenuItem jMenuItem18;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem7;
    private javax.swing.JMenuItem jMenuItemIniciar;
    private javax.swing.JMenuItem jMenuItemLimparOutput;
    private javax.swing.JMenuItem jMenuSair;
    private javax.swing.JMenuItem jMenuSalvar;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPopupMenu jPopupMenuOutput;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JPopupMenu.Separator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JSeparator jSeparator7;
    private javax.swing.JSeparator jSeparator8;
    private javax.swing.JSeparator jSeparator9;
    private javax.swing.JMenu menuArquivo;
    private javax.swing.JMenu menuEditar;
    // End of variables declaration//GEN-END:variables
}
