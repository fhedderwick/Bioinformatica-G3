package bioinformatica;

import java.io.File;
import java.io.IOException;

public class Bioinformatica {

    public static void main(String[] args) throws IOException {
        
        if(args.length == 0){
            System.out.println("TP GRUPO 3. Ejecutar con parámetros:");
            System.out.println("-n -input siendo n el ejercicio [1, 2, 4, 5] y siendo input el archivo de entrada");
            return;
        }
        
        if(args.length != 2){
            System.out.println("Debe indicarse el archivo de entrada y el script a ejecutar.");
            return;
        }
        
        final File inputFile = new File(args[1]);
        if(!inputFile.getCanonicalPath().startsWith(System.getProperty("user.dir"))){
            System.out.println("Por motivos de seguridad los archivos de entrada deben estar debajo del programa en la jerarquia de archivos.");
            return;
        }
        if(!inputFile.isFile()){
            System.out.println("No se encuentra el archivo de entrada especificado.");
            return;
        }
        
        switch(args[0]){
            case "-1": ejercicio1(inputFile); break;
            case "-2": ejercicio2(inputFile); break;
            case "-4": ejercicio4(inputFile); break;
            case "-5": ejercicio5(inputFile); break;
            default: System.out.println("Parámetro incorrecto. Debe ser -n con n valiendo 1, 2, 4 o 5");
        }
        
    }
    
    private static void ejercicio1(final File file){
        if(!file.getName().toUpperCase().endsWith(".GB")){
            System.out.println("El archivo de entrada debe ser tipo gb.");
            return;
        }
        if(new Ejercicio1(file).run()){
            System.out.println("El script se ejecutó exitosamente.");
        } else {
            System.out.println("Ha ocurrido un problema. Revise el log por favor.");
        }
    }
    
    private static void ejercicio2(final File file){
        if(!file.getName().toUpperCase().endsWith(".FAS")){
            System.out.println("El archivo de entrada debe ser tipo fas.");
            return;
        }
        if(new Ejercicio2(file).run()){
            System.out.println("El script se ejecutó exitosamente.");
        } else {
            System.out.println("Ha ocurrido un problema. Revise el log por favor.");
        }
    }
    
    private static void ejercicio4(final File file){
        if(!file.getName().toUpperCase().endsWith(".OUT")){
            System.out.println("El archivo de entrada debe ser tipo out.");
            return;
        }
        if(new Ejercicio4(file).run()){
            System.out.println("El script se ejecutó exitosamente.");
        } else {
            System.out.println("Ha ocurrido un problema. Revise el log por favor.");
        }
    }
    
    private static void ejercicio5(final File file){
        if(!file.getName().toUpperCase().endsWith(".FAS")){
            System.out.println("El archivo de entrada debe ser tipo fas.");
            return;
        }
        if(new Ejercicio5(file).run()){
            System.out.println("El script se ejecutó exitosamente.");
        } else {
            System.out.println("Ha ocurrido un problema. Revise el log por favor.");
        }
    }
    

}
