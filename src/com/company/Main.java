package com.company;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Main extends JFrame {

    Main() {
        this.setTitle("Zipper");
        this.setSize(400, 200);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        this.setJMenuBar(pasekMenu);

        JMenu menuPlik = pasekMenu.add(new JMenu("Plik"));
        Action akcjaDodawania = new Akcja("Dodaj", "Dodaj nowy plik do archiwum", "ctrl D");
        Action akcjaUsuwania = new Akcja("Usuń", "Usuń wpisy z archiwum", "ctrl U");
        Action akcjaZipowania = new Akcja("Zip!", "Zipuj", "ctrl Z");


        bDodaj = new JButton(akcjaDodawania);
        bUsun = new JButton(akcjaUsuwania);
        bZip = new JButton(akcjaZipowania);
        JScrollPane scrollPane = new JScrollPane(lista);

        JMenuItem menuOtworz = menuPlik.add(akcjaDodawania);
        JMenuItem menuUsun = menuPlik.add(akcjaUsuwania);
        JMenuItem menuZipuj = menuPlik.add(akcjaZipowania);
        lista.setBorder(BorderFactory.createEtchedBorder());
        GroupLayout layout = new GroupLayout(this.getContentPane());
        this.getContentPane().setLayout(layout);
        layout.setAutoCreateContainerGaps(true);
        layout.setAutoCreateGaps(true);
        layout.setHorizontalGroup(
                layout.createSequentialGroup()
                        .addComponent(scrollPane, 100, 150, Short.MAX_VALUE)
                        .addContainerGap(0, Short.MAX_VALUE)
                        .addGroup(
                                layout.createParallelGroup().addComponent(bDodaj).addComponent(bUsun).addComponent(bZip)
                        )

        );
        layout.setVerticalGroup(
                layout.createParallelGroup()
                        .addComponent(scrollPane, layout.DEFAULT_SIZE, layout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createSequentialGroup().addComponent(bDodaj).addComponent(bUsun).addGap(10, 40, Short.MAX_VALUE).addComponent(bZip))

        );
        this.pack();
    }

    private DefaultListModel modelListy = new DefaultListModel() {
        public void addElement(Object element) {
            lista.add(element);
            super.addElement(((File) element).getName());
        }

        ArrayList lista = new ArrayList();

        public Object get(int index) {
            return lista.get(index);
        }

        public Object remove(int index) {
            lista.remove(index);
            return super.remove(index);
        }
    };
    private JList lista = new JList(modelListy);
    private JButton bDodaj = new JButton();
    private JButton bUsun = new JButton();
    private JButton bZip = new JButton();
    private JMenuBar pasekMenu = new JMenuBar();
    private JFileChooser wybieracz = new JFileChooser();


    private class Akcja extends AbstractAction {
        public Akcja(String nazwa, String opis, String klawiaturowySkrot) {
            this.putValue(Action.NAME, nazwa);
            this.putValue(Action.SHORT_DESCRIPTION, opis);
            this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(klawiaturowySkrot));
        }

        public Akcja(String nazwa, String opis, String klawiaturowySkrot, Icon icon) {
            this(nazwa, opis, klawiaturowySkrot);
            this.putValue(Action.SMALL_ICON, icon);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().equals("Dodaj")) {
                dodajWpisyDoArchiwum();
            } else if (e.getActionCommand().equals("Usuń")) {
                usunWpisyZListy();
            } else if (e.getActionCommand().equals("Zip!")) {
                stworzArchiwumZip();
            }

        }
    }

    private void dodajWpisyDoArchiwum() {
        wybieracz.setCurrentDirectory(new File(System.getProperty("user.dir")));
        wybieracz.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        wybieracz.setMultiSelectionEnabled(true);

        int odp = wybieracz.showDialog(rootPane, "Dodaj do archiwum");
        if (odp == JFileChooser.APPROVE_OPTION) {
            File[] sciezki = wybieracz.getSelectedFiles();
            for (int i = 0; i < sciezki.length; i++) {
                if (!powtorkaWpisu(sciezki[i].getPath()))
                    modelListy.addElement(sciezki[i]);
            }
        }
    }

    private boolean powtorkaWpisu(String Wpis) {

        for (int i = 0; i < modelListy.getSize(); i++) {
            if (((File) modelListy.get(i)).getPath().equals(Wpis)) {
                return true;
            }
        }
        return false;
    }

    private void usunWpisyZListy() {
        int[] tmp = lista.getSelectedIndices();
        for (int i = 0; i < tmp.length; i++) {
            modelListy.remove(tmp[i] - i);
        }
    }

    private void stworzArchiwumZip() {
        wybieracz.setCurrentDirectory(new File(System.getProperty("user.dir")));
        wybieracz.setSelectedFile(new File(System.getProperty("user.dir") + File.separator + "Mojanazwa.zip"));
        int tmp = wybieracz.showDialog(rootPane, "Kopmresuj");
        if (tmp == JFileChooser.APPROVE_OPTION) {
            byte tmpData[] = new byte[BUFFOR];
            try {
                ZipOutputStream zOutS = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(wybieracz.getSelectedFile()), BUFFOR));
                for (int i = 0; i < modelListy.getSize(); i++) {
                    if (!((File) modelListy.get(i)).isDirectory()) {
                        zipuj(zOutS, (File) modelListy.get(i), tmpData,  ((File) modelListy.get(i)).getPath());
                    } else {
                        wypiszSciezki((File) modelListy.get(i));

                        for (int j = 0; j < listaSciezek.size(); i++) {
                            zipuj(zOutS, (File) listaSciezek.get(j), tmpData, ((File) modelListy.get(j)).getPath());
                            listaSciezek.removeAll(listaSciezek);
                        }
                    }


                }
                zOutS.close();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }

        }
    }
    private void zipuj(ZipOutputStream zOutS, File sciezkaPliku, byte []tmpData, String sciezkaBazowa) throws IOException {
        BufferedInputStream inS = new BufferedInputStream(new FileInputStream(sciezkaPliku), BUFFOR);

        zOutS.putNextEntry(new ZipEntry(sciezkaPliku.getPath().substring(sciezkaBazowa.lastIndexOf(File.separator)+1)));

        int counter;
        while ((counter = inS.read(tmpData, 0, BUFFOR)) != -1) {
            zOutS.write(tmpData, 0, counter);
        }
        zOutS.closeEntry();
        inS.close();
    }

    private void wypiszSciezki(File nazwaSciezki)
    {
        String[] nazwyPlikowIKatalogow = nazwaSciezki.list();
        for(int i = 0; i<nazwyPlikowIKatalogow.length; i++)
        {
            File p = new File(nazwaSciezki.getPath(), nazwyPlikowIKatalogow[i]);
                    if(p.isFile())
                    {
listaSciezek.add(p);
                    }
                   if(p.isDirectory())
                    {
                        wypiszSciezki(new File(p.getPath()));
                    }
        }
    }
    ArrayList listaSciezek = new ArrayList();
    public static final int BUFFOR = 1024;

    public static void main(String[] args) {
        new Main().setVisible(true);
    }
}
