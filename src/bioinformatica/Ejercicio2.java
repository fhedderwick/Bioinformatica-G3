package bioinformatica;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.biojava3.core.sequence.ProteinSequence;
import org.biojava3.core.sequence.io.FastaReaderHelper;
import org.biojava3.core.sequence.io.util.IOUtils;
import org.biojava3.ws.alignment.qblast.BlastOutputFormatEnum;
import org.biojava3.ws.alignment.qblast.BlastProgramEnum;
import org.biojava3.ws.alignment.qblast.NCBIQBlastAlignmentProperties;
import org.biojava3.ws.alignment.qblast.NCBIQBlastOutputProperties;
import org.biojava3.ws.alignment.qblast.NCBIQBlastService;
//import org.biojava3.ws.alignment.qblast.NCBIQBlastService;

//https://alextblog.blogspot.com/2012/05/ncbi-blast-jaxb-biojava-blasting-like.html fuente

public class Ejercicio2 {

    private final File _inputFile;
    
    Ejercicio2(final File inputFile) {
        _inputFile = inputFile;
    }

    public boolean run(){
        try {
            // El archivo input es el output del primer ejercicio. Se buscaran secuencias similares en otras especies.
            final Map<String, ProteinSequence> secuenciasDeAminoacidos;

            System.out.println("Leyendo secuencias de aminoácidos escritas en formato FASTA. Por favor espere.");

            try {
                // Obtenemos las seis secuencias desde el archivo FASTA
                secuenciasDeAminoacidos = FastaReaderHelper.readFastaProteinSequence(_inputFile);
            } catch (Exception e) {
                System.out.println("Error leyendo el archivo de entrada " + _inputFile.getName() + ". Se coloca debajo el trace del error.");
                e.printStackTrace();
                return false;
            }

            final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            int value = -1;
            while (true) {
                System.out.println("Se leyeron " + secuenciasDeAminoacidos.size() + " secuencias:");
                int i=1;
                System.out.println("\t0) Salir sin procesar");
                final List<String> keys = new ArrayList<>();
                final List<ProteinSequence> proteinSequences = new ArrayList<>();
                for(final Map.Entry<String, ProteinSequence> entry : secuenciasDeAminoacidos.entrySet()){
                    System.out.println("\t" + i++ + ") " + entry.getKey());
                    keys.add(entry.getKey());
                    proteinSequences.add(entry.getValue());
                }
                System.out.print(System.lineSeparator() + "Elija cual de estas secuencias quiere procesar con BLAST: ");
                try {
                    final String userInput = br.readLine();
                    try{
                        value = Integer.parseInt(userInput);
                    }catch(final NumberFormatException nfe){
                        value = -1;
                    }
                    if(value == 0){
                        System.out.println("Se cancela el proceso a petición del usuario.");
                        return true;
                    }
                    if(value >= 0 && value <= secuenciasDeAminoacidos.size()){
                        final String tagSecuencia = keys.get(value-1);
                        System.out.println("Se enviará a BLAST la secuencia " + tagSecuencia);
                        final List<List<String>> lines = blast(tagSecuencia,secuenciasDeAminoacidos.get(tagSecuencia));
                        if(lines != null){
                            final List<String> results = prepareResults(lines);
                            System.out.println("Se obtuvieron los siguientes resultados:");
                            for(int j=results.size(); j!=0; j--){
                                System.out.println(results.get(j-1));
                            }
                            return true;
                        }
                        return false;
                    }
                    System.out.println("Número inválido! Elija otro");
                } catch (IOException e) {
                    System.out.println("Error leyendo la terminal de usuario. Se coloca debajo el trace del error.");
                    e.printStackTrace();
                    return false;
                }
            }
        } catch (final Exception e) {
            System.err.println("Ha ocurrido un error general. Se coloca debajo el trace del error.");
            e.printStackTrace();
            return false;
        }
    }
    
    private List<List<String>> blast(final String tagSecuencia, final ProteinSequence secuenciaElegida){
        final NCBIQBlastService service = new NCBIQBlastService();
        System.out.println("Inicializando la conexión con el servidor BLAST del NCBI. Por favor espere.");
        final NCBIQBlastAlignmentProperties props = new NCBIQBlastAlignmentProperties();
        props.setBlastProgram(BlastProgramEnum.blastp); //BLAST para Proteinas
        props.setBlastDatabase("swissprot");

        // Configuración de la salida
        final NCBIQBlastOutputProperties outputProps = new NCBIQBlastOutputProperties();
        outputProps.setOutputFormat(BlastOutputFormatEnum.Text);

        String requestId = null;
        FileWriter outputWriter = null;
        BufferedReader serverResponseReader = null;
        try {
            System.out.println("Enviando la consulta al servidor.");
                requestId = service.sendAlignmentRequest(secuenciaElegida.getSequenceAsString(), props);
                System.out.println("Se ha enviado la consulta al servidor. Por favor espere.");
                int loopCounter = 0;
                while (!service.isReady(requestId)) {
                    loopCounter++;
                    System.out.println("Los resultados aún no están disponibles, espere por favor.");
                    Thread.sleep(5000);
                    if(loopCounter%12 == 0){
                        final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                        while (true) {
                            System.out.println("Los resultados están demorando. (S)eguir esperando o (c)ancelar el proceso?");
                            final String userInput = br.readLine();
                            if("S".equalsIgnoreCase(userInput)){
                                break;
                            } else if("C".equalsIgnoreCase(userInput)){
                                if(!service.isReady(requestId)){
                                    System.out.println("Se cancela el proceso");
                                    return null;
                                }
                                System.out.println("Se eligió cancelar el proceso por la demora, sin embargo los resultados ya están listos por lo que se continuará el proceso.");
                                break;
                            }
                        }
                    }
                }

            System.out.println("El servidor informa que los resultados ya están listos.");
            final InputStream in = service.getAlignmentResults(requestId, outputProps);
            serverResponseReader = new BufferedReader(new InputStreamReader(in));
            final List<List<String>> lines = new ArrayList<>();
            List<String> tempList = new ArrayList<>();
            final File outputFile = new File(_inputFile.getParent(),_inputFile.getName() + "_blast_" + tagSecuencia + ".out");
            outputWriter = new FileWriter(outputFile);
            String line;
            boolean flag = false;
            while ((line = serverResponseReader.readLine()) != null) {
                outputWriter.write(line + System.lineSeparator());
                if(flag){
                    if(line.startsWith(">")){
                        tempList = new ArrayList<>();
                        lines.add(tempList);
                    }
                    tempList.add(line);
                }
                flag |= "ALIGNMENTS".equals(line);
            }
            System.out.println("El proceso ha finalizado, se escribe el resultado en archivo de salida " + outputFile.getName());
            return lines;
        } catch (Exception e) {
            System.err.println("Ha ocurrido un error general. Se coloca debajo el trace del error.");
            e.printStackTrace();
            return null;
        } finally {
            IOUtils.close(outputWriter);
            IOUtils.close(serverResponseReader);
            service.sendDeleteRequest(requestId); //Request para informar al servidor que puede eliminar los resultados
        }
    }

    private List<String> prepareResults(final List<List<String>> lines) {
        final List<String> results = new ArrayList<>();
        for(final List<String> block : lines){
            List<String> especies = new ArrayList<>();
            String score = "";
            for(final String line : block){
                int startIndex = line.indexOf('[');
                int endIndex = line.indexOf(']');
                if(startIndex != -1 && endIndex != -1){
                    especies.add(line.substring(startIndex+1,endIndex));
                } else if(startIndex != -1){
                    especies.add(line.substring(startIndex+1));
                } else if(endIndex != -1){
                    final String especie = especies.remove(especies.size()-1);
                    especies.add(especie + line.substring(0,endIndex));
                }
                
                startIndex = line.indexOf(" Score = ");
                endIndex = line.indexOf(" (");
                if(startIndex != -1 && endIndex != -1){
                    score = line.substring(startIndex+9,endIndex);
                }
                
            }
            
            for(final String especie : especies){
                results.add(especie + " con un score de " + score);
            }
        }
        return results;
    }
    
}
