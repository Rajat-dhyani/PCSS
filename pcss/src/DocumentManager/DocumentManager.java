/*
 * Copyright (c) 2017, Rajat Dhyani (http://www.rajatdhyani.xyz)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package DocumentManager;

import java.io.File;
import java.io.*;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Stack;
import java.util.StringTokenizer;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

/**
 *
 * @author rajat_000
 */
public class DocumentManager {
    private HashMap<String,String> mapVariable;
    private HashMap<String,String> mapFunction;
    private BufferedWriter pcssBufferedWriter;
    private BufferedWriter cssBufferedWriter;
    private Scanner pcssText;
    private Scanner cssText;
    
    public DocumentManager(){
        mapVariable = new HashMap<String,String>();
        mapFunction = new HashMap<String,String>();
    }
    
    
    public boolean doUpdate(String pcssFilePath,JTextArea jTPcssFile,String cssFilePath,JTextArea jTCssFile){
        try{
            // TODO : Use Fast Scanner Class for input 
            
            pcssText = new Scanner(jTPcssFile.getText());
            System.out.println(pcssFilePath);
            
            FileWriter pcssFileWriter = new FileWriter(pcssFilePath);
            pcssBufferedWriter = new BufferedWriter(pcssFileWriter);  
            
            cssText = new Scanner(jTCssFile.getText());
            System.out.println(cssFilePath);
            
            FileWriter cssFileWriter = new FileWriter(cssFilePath);
            cssBufferedWriter = new BufferedWriter(cssFileWriter);  
            
            
            
            while(pcssText.hasNext()){
                String line = pcssText.nextLine();
                pcssBufferedWriter.write(line);
                pcssBufferedWriter.newLine();
            
                if (line.startsWith("$")){
                    addVariableToMap(line);
                }else if (line.startsWith("@import")){
                    writeImportToCSS(cssFilePath.substring(0, cssFilePath.lastIndexOf("\\")) + "\\"+line.split(" ")[1].trim());
                }else if (line.startsWith("@function")){
                    addFunctionsToMap(line);
                }else if (line.contains("@include")){
                    boolean flag = writeFunctionToCSS(line);
                    if (!flag){
                        return flag;
                    }   
                }
                else if (line.contains("$")){
                    boolean flag = writeVariableToCSS(line);
                    if (!flag){
                        return flag;
                    }
                }
                else{
                    cssBufferedWriter.write(line);
                    cssBufferedWriter.newLine();
                }   
                System.out.println(line);
            }
            
            pcssBufferedWriter.close();  
            cssBufferedWriter.close();  
            return true;
        } catch(Exception ex) {
            JOptionPane.showMessageDialog(null, ex,"Alert",JOptionPane.ERROR_MESSAGE);
        }
        return false;
    }
    
    private void addVariableToMap(String line){
        String variable[] = line.split(":");
        variable[1] = variable[1].trim().substring(0, variable[1].trim().length()-1);
                    
        System.out.println("Variable :" + variable[0] + " value: "+ variable[1] );
        if (mapVariable.containsKey(variable[0].trim())){
            String value = mapVariable.get(variable[0]);
            if ( !value.equals(variable[1])){
                mapVariable.put(variable[0].trim(),variable[1].trim());
            }
         } else {
            mapVariable.put(variable[0].trim(),variable[1].trim());
         }
    }
    
    private boolean writeVariableToCSS(String line) throws IOException{
        String variable[] = line.split(":");
        System.out.println("Variable :" + variable[0] + " value: "+ variable[1]+" map :"+ mapVariable);
        if (containsOperator(line)){
           variable[1] = solveOperation(variable[1].toCharArray());
           System.out.println("Variable after operation : '"+ variable[1]+"'");
            
           if (variable[1].equals("false")){
               System.out.println("Operation error at line : '"+ line+"'");
               return false;
           }
        } else {
            variable[1] = variable[1].trim().substring(0, variable[1].lastIndexOf(";")-1);
            System.out.println("Variable :" + variable[0] + " value: "+ variable[1]+" map :"+ mapVariable);
                    
            if (mapVariable.containsKey(variable[1].trim())){
                variable[1] = mapVariable.get(variable[1]);
            } else {
                System.out.println("Variable Not Defined at line : '"+ line+"'");
                return false;
            }
        }
        cssBufferedWriter.write(variable[0]+" : "+ variable[1]+";");
        System.out.println("Variable :" + variable[0] + " value: "+ variable[1] );
        cssBufferedWriter.newLine();
        
        return true;
    }
    
    private boolean containsOperator(String line){
        return line.contains("+") || line.contains("-") || line.contains("*") || line.contains("/") || line.contains("%");
    }
    
    private boolean isOperator(char c){
        return c == '+'|| c == '-' || c == '*' || c == '/' || c == '%';
    }
    
    private boolean hasPrecedence(char c1, char c2){
        if ( c1 =='(' && c2 == ')')
            return false;
        if (c1 == '*' || c1 == '/' || c1 == '%'&& c2 =='+' || c2 == '-')
            return false;
        return true;
    }
    
    private int applyOperation(int b,int a,char operation){
        if (operation == '+')
            return a+b;
        else if (operation == '-')
            return a-b;
        else if(operation == '*')
            return a*b;
        else if (operation == '/'){
            if (b == 0){
                System.out.println("Devide by 0 error");
                return Integer.MIN_VALUE;
            }
            return a/b;
        } else if (operation == '%'){
            if (b == 0){
                System.out.println("Devide by 0 error");
                return Integer.MIN_VALUE;
            }
            return a%b;
        }
        System.out.println("Undefined operation");
        return Integer.MIN_VALUE;
    }
    
    private String solveOperation(char[] token){
        
        Stack<Integer> variableStack = new Stack<>();
        Stack<Character> operatorStack = new Stack<>();
        boolean isHex = false;
        String unit = "";
        for (int i=0; i<token.length; i++){
            if (token[i] == ' '){
                continue;
            }
            if (token[i] == '$') {
                StringBuilder variable = new StringBuilder();
                variable.append(token[i++]);
                System.out.println("Variable :" + variable+" "+token[i]+" "+ i+" "+ token.length);
                while (i<token.length && !(token[i] == ' ' || token[i] == ';' || isOperator(token[i]))){
                    variable.append(token[i++]);
                    System.out.println("Variable :" + variable+" "+token[i]+" "+ i+" "+ token.length);
                }
                String value = mapVariable.get(variable.toString());
                System.out.println("Variable :" + variable + " value: "+ value);
                if (value.contains("#")){
                    isHex = true;
                    value = value.substring(1,value.length());
                    System.out.println(" # Variable :" + variable + " value: "+ value+" integer value "+ Integer.parseInt(value, 16));
                    variableStack.push(Integer.parseInt(value, 16));
                }else {
                    unit = value.substring(value.length()-2,value.length());
                    value = value.substring(0,value.length()-2);
                    System.out.println("Unit"+unit+"Variable :" + variable + " value: "+ value);
                    variableStack.push(Integer.parseInt(value));
                }
            } else if (token[i] == '('){
                operatorStack.push(token[i]);
            } else if (token[i] == ')'){
                while (operatorStack.peek()!=')'){
                    int results = applyOperation(variableStack.pop(), variableStack.pop(), operatorStack.pop());
                    if (results != Integer.MIN_VALUE)
                        variableStack.push(results);
                    else 
                        return "false";
                }
            } else if (isOperator(token[i])){
                System.out.println("Operator :"+ token[i]);
                while (!operatorStack.empty() && hasPrecedence(token[i], operatorStack.peek())){
                    int results = applyOperation(variableStack.pop(), variableStack.pop(), operatorStack.pop());
                    System.out.println("Operator :"+ token[i]+"  Results: "+results);
                    if (results != Integer.MIN_VALUE)
                        variableStack.push(results);
                    else 
                        return "false";
                }
                operatorStack.push(token[i]);
            }
        }
        while (!operatorStack.empty()){
            int results = applyOperation(variableStack.pop(), variableStack.pop(), operatorStack.pop());
            System.out.println("Results: "+results);
            if (results != Integer.MIN_VALUE)
                variableStack.push(results);
            else 
                return "false";
        }
        System.out.println(isHex+" "+unit);
        if (isHex){
            String results=  "#"+Integer.toHexString(variableStack.pop());
            System.out.print(results);
            return results;
        }
        else{
            String results=  variableStack.pop()+unit;
            return results;
        }
            
    }
   
    
    private void addFunctionsToMap(String line) throws IOException{
        String functionName = line.split(" ")[1].trim();
        StringBuilder functionBody =  new StringBuilder();
        while (pcssText.hasNext()){
            String text = pcssText.nextLine();
            pcssBufferedWriter.write(text);
            pcssBufferedWriter.newLine();
            if (text.contains("}"))
                break;
            functionBody.append(text+"\n");
        }
        mapFunction.put(functionName, functionBody.toString());
    }
    
    private boolean writeFunctionToCSS(String line) throws IOException{
      
       String functionName = line.split(" ")[1].trim();
       
       if (mapFunction.containsKey(functionName)){
            cssBufferedWriter.write(mapFunction.get(functionName));
            System.out.println("function Name: "+functionName+" function : "+mapFunction.get(functionName) );
        } else {
            System.out.println("Function Not Defined at line : '"+ line+"'");
            return false;
        }
        return true;
    }
    
    private void writeImportToCSS(String filePath) throws FileNotFoundException, IOException{
        System.out.println(filePath);
        File importedFile = new File(filePath);
        Scanner importedFileText = new Scanner(importedFile);
                    
        while (importedFileText.hasNext()){
            cssBufferedWriter.write(importedFileText.nextLine());
            cssBufferedWriter.newLine();  
        }
    }
    
}
