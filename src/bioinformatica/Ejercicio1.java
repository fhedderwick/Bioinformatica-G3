package bioinformatica;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import org.biojava3.core.sequence.DNASequence;
import org.biojava3.core.sequence.ProteinSequence;
import org.biojava3.core.sequence.RNASequence;
import org.biojava3.core.sequence.io.FastaWriterHelper;
import org.biojava3.core.sequence.io.GenbankReaderHelper;
import org.biojava3.core.sequence.transcription.Frame;

public class Ejercicio1 {

    private final File _inputFile;
    
    Ejercicio1(final File inputFile) {
        _inputFile = inputFile;
    }

    public boolean run(){
        try {
            final LinkedHashMap<String, DNASequence> dnaSequences = GenbankReaderHelper.readGenbankDNASequence(_inputFile);
            final List<ProteinSequence> secuenciasDeAminoacidos = new LinkedList<>();
            
            for (final DNASequence secuenciaDeADN : dnaSequences.values()) {
                System.out.println("Traduciendo a ARN en cada uno de los marcos de lectura (ORF). Por favor espere.");
                for(final Frame frame : Frame.getAllFrames()){
                    final RNASequence rnaSequence = secuenciaDeADN.getRNASequence(frame);       //se obtiene la secuencia de RNA por cada ORF
                    final ProteinSequence proteinSequence = rnaSequence.getProteinSequence();   //Se transcribe ese RNA a una secuencia de aminoacidos por cada ORF
                    proteinSequence.setOriginalHeader(frame.name());                            //Se setea como encabezado el nombre del ORF utilizado
                    secuenciasDeAminoacidos.add(proteinSequence);
                }

                final File outputFile = new File(_inputFile.getParent(),_inputFile.getName() + ".fas");
                try {
                    outputFile.createNewFile();
                } catch (final IOException e) {
                    System.out.println("Error creando el archivo de salida " + outputFile.getName() + ". Se coloca debajo el trace del error.");
                    e.printStackTrace();
                    return false;
                }

                System.out.println("El proceso ha finalizado, se escribe el resultado en archivo de salida " + outputFile.getName());

                try {
                    FastaWriterHelper.writeProteinSequence(outputFile,secuenciasDeAminoacidos);
                } catch (final Exception e) {
                    System.out.println("Error escribiendo el archivo de salida " + outputFile.getName() + ". Se coloca debajo el trace del error.");
                    e.printStackTrace();
                    return false;
                }
            }
        } catch (final Exception e) {
            System.err.println("Ha ocurrido un error general. Se coloca debajo el trace del error.");
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
}
