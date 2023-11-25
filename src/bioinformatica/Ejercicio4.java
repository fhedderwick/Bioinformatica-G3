package bioinformatica;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.HttpsURLConnection;

public class Ejercicio4 {

    private final File _inputFile;
    
    Ejercicio4(final File inputFile) {
        _inputFile = inputFile;
    }

    public boolean run(){
        // El archivo input es el output del segundo ejercicio (el BLAST).
        BufferedReader br = null;
        try {
            System.out.println("Leyendo los resultados del archivo Blast. Por favor espere.");
            br = new BufferedReader(new FileReader(_inputFile));
            String line;
            final StringBuilder sb = new StringBuilder();
            while((line = br.readLine()) != null){
                if(sb.length() != 0 || "ALIGNMENTS".equals(line)){
                    sb.append(line);
                }
            }

            final String[] matches = sb.toString().split(">");

            br.close();
            br = new BufferedReader(new InputStreamReader(System.in));
            String patron;

            while (true) {
                System.out.println("Ingrese un patrón para buscar, o exit para salir: ");

                int resultados = 0;
                try {
                    patron = br.readLine();
                    if("exit".equalsIgnoreCase(patron)) {
                        break;
                    }
                    System.out.println("Buscando, espere por favor.");
                    final List<String> matchingMatches = new ArrayList<>();
                    for (final String match : matches) {
                        if (match.toUpperCase().contains(patron.toUpperCase())) {
                            System.out.println(match);
                            System.out.flush();
                            matchingMatches.add(match);
                            resultados++;
                        }
                    }
                    if (resultados == 0) {
                        System.out.println("No se encontraron matches con el patrón buscado.");
                    } else {
                        try{
                            final File outputFile = new File(_inputFile.getParent(), patron + "_" + _inputFile.getName());
                            final FileWriter fw = new FileWriter(outputFile);
                            for(final String outputLine : matchingMatches){
                                fw.write(outputLine + System.lineSeparator());
                            }
                            fw.close();
                            System.out.println(resultados + " matches encontrados y almacenados en el archivo " + outputFile);
                            return promptFastaDownload(matchingMatches);
                        } catch(final IOException e){
                            System.out.println("Error escribiendo el archivo de salida. Se coloca debajo el trace del error.");
                            e.printStackTrace();
                            return false;
                        }
                    }
                } catch (final IOException e) {
                        System.out.println("Error leyendo la terminal de usuario. Se coloca debajo el trace del error.");
                        e.printStackTrace();
                        return false;
                }
            }
        } catch (final Exception e) {
            System.err.println("Ha ocurrido un error general. Se coloca debajo el trace del error.");
            e.printStackTrace();
            return false;
        } finally {
            try {
                if(br != null){
                    br.close();
                }
            } catch (IOException ex) {
                System.err.println("Ha ocurrido un error salvable al cerrar el flujo de input.");
                ex.printStackTrace();
            }
        }
        return true;
    }
    
    private boolean promptFastaDownload(final List<String> matches){
        try(final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));){
            while (true) {
                System.out.println("Descargar archivos FASTA para los accesion matcheados? (S/N)");
                final String input;
                input = br.readLine();
                if("N".equalsIgnoreCase(input)) {
                    return true;
                } else if("S".equalsIgnoreCase(input)) {
                    break;
                }
                System.out.println("Respuesta inválida!");
            }
        } catch (final IOException ex) {
            System.out.println("Error leyendo la terminal de usuario. Se coloca debajo el trace del error.");
            ex.printStackTrace();
            return false;
        }
        
        try{
            for(final String match : matches){
                final String accesionNumber = match.substring(0,match.indexOf(' '));
                System.out.println("Consultando para " + accesionNumber);
                final URL url = new URL("https://www.ncbi.nlm.nih.gov/search/api/download-sequence/?db=protein&id=" + accesionNumber);
                HttpURLConnection connection = null;
                connection = (HttpsURLConnection)url.openConnection();
                connection.setRequestProperty("Content-Type", "text/plain; charset=\"utf8\"");
                connection.setRequestMethod("GET");
                int returnCode = connection.getResponseCode();
                final InputStream connectionIn;
                if (returnCode==200) {
                    connectionIn = connection.getInputStream();
                } else {
                    connectionIn = connection.getErrorStream();
                }
                final BufferedReader buffer = new BufferedReader(new InputStreamReader(connectionIn));
                String inputLine;
                final File outputFile = new File(_inputFile.getParent(),accesionNumber + ".fas");
                final FileWriter fw = new FileWriter(outputFile);
                while ((inputLine = buffer.readLine()) != null){
                    fw.write(inputLine + System.lineSeparator());
                }
                fw.close();
                buffer.close();
                System.out.println(outputFile.getName() + " generado exitosamente.");
            }
        }catch(final IOException e){
            System.out.println("Error obteniendo los FASTA y/o volcandolo a archivo. Se coloca debajo el trace del error.");
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
}
