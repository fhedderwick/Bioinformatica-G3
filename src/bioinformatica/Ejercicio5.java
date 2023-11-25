package bioinformatica;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

public class Ejercicio5 {

    private final File _inputFile;
    
    Ejercicio5(final File inputFile) {
        _inputFile = inputFile;
    }
    
    public boolean run(){
        try {
//            Primero se descarga la base de datos (PROSITE.DAT y PROSITE.DOC), luego se la instala con “sudo prosextract”.
                final File getorfTempFolder = new File("getorf-temp/");
                Files.createDirectories(getorfTempFolder.toPath());
                System.out.println("Se crea el directorio temporal " + getorfTempFolder.getAbsolutePath() + ".");
                System.out.println("Creando los archivos ORF temporales. Espere por favor.");
		final String command = "getorf -sequence "
				+ _inputFile.getAbsolutePath()
				+ " -table 0 -minsize 30 -maxsize 1000000 -find 0 -methionine -nocircular -reverse -flanking 100 -ossingle2 -osdirectory2 "
				+ getorfTempFolder.toString() + " -auto";

                try {
                    Runtime.getRuntime().exec(command).waitFor();
                } catch (final InterruptedException ex) {
                    System.err.println("Ha ocurrido un error ejecutando el comando getorf. Se coloca debajo el trace del error.");
                    ex.printStackTrace();
                    return false;
                }
		System.out.println("Se crearon exitosamente " + getorfTempFolder.listFiles().length + " archivos con secuencias de aminoácidos en la carpeta " + getorfTempFolder.getName() + ".");
                
                final File patmatmotifsTempFolder = new File("patmatmotifs-temp/");
                Files.createDirectories(patmatmotifsTempFolder.toPath());
                System.out.println("Se crea el directorio temporal " + patmatmotifsTempFolder.getAbsolutePath() + ".");
                
                final File[] firstFolderFiles = getorfTempFolder.listFiles();
		for (final File file : firstFolderFiles) {
			if(!file.isFile()){
                            continue;
                        }
                        System.out.println("Se hace un análisis de dominios para el archivo " + file.getName() + ".");
			final String patmatmotifsCommand = "patmatmotifs -sequence "
					+ file.toString() + " -outfile "
					+ patmatmotifsTempFolder.toString() + "/" + file.getName()
					+ ".patmatmotifs -full -rformat dbmotif -auto";
                    try {
                        Runtime.getRuntime().exec(patmatmotifsCommand).waitFor();
                    } catch (InterruptedException ex) {
                        System.err.println("Ha ocurrido un error ejecutando el comando patmatmotifs. Se coloca debajo el trace del error.");
                        ex.printStackTrace();
                        return false;
                    }
                    file.delete();
		}
		System.out.println("Se crearon exitosamente " + patmatmotifsTempFolder.listFiles().length + " archivos en la carpeta " + patmatmotifsTempFolder.getName() + ".");
//
		System.out.println("Mergeando los archivos en una única salida. Espere por favor.");
                final File outputFile = new File(_inputFile + ".EMBASS.out");
                final FileWriter fw = new FileWriter(outputFile);
                final File[] secondFolderFiles = patmatmotifsTempFolder.listFiles();
		for (final File file : secondFolderFiles){
                    if(!file.isFile()){
                        continue;
                    }
                    final BufferedReader br = new BufferedReader(new FileReader(file));
                    String line;
                    while((line = br.readLine()) != null){
                        fw.write(line);
                        fw.write(System.lineSeparator());
                    }
                    fw.write(System.lineSeparator());
                    br.close();
                    file.delete();
                }
                fw.close();
                System.out.println("Se generó el archivo de salida " + outputFile.getName() + ".");

		System.out.println("Se eliminan las carpetas temporales.");
                Files.delete(getorfTempFolder.toPath());
                Files.delete(patmatmotifsTempFolder.toPath());
        } catch (final IOException ex) {
            System.err.println("Ha ocurrido un error general. Se coloca debajo el trace del error.");
            ex.printStackTrace();
            return false;
        }
        return true;
    }
    
}
