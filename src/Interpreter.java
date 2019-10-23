import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

import org.apache.commons.text.StringEscapeUtils;

import static java.lang.System.out;

public class Interpreter {
    private static final List<String> statements = Arrays.asList(
            "clear",
            "incr",
            "decr",
            "while",
            "if",
            "func",
            "print"
    );
    private static final List<String> reservedWords = Arrays.asList(
            "not",
            "do",
            "end",
            "of"
    );
    private static final List<String> comparators = Arrays.asList(
            "=",
            ">"
    );

    private HashMap<String, Integer> status;
    private HashMap<String, ArrayList<String>> functions;
    private ArrayList<String> code;
    private int line;

    private Interpreter(ArrayList<String> code) throws EOFException {
        this.status = new HashMap<>();
        this.code = code;
        for(this.line = 1; this.line < code.size(); this.line++) {
            this.interpretExpression(this.code.get(this.line-1));
        }
    }

    private Interpreter(ArrayList<String> code, HashMap<String, Integer> status) throws EOFException {
        this.status = status; // Pass outer scope
        this.code = code;
        for(this.line = 1; this.line < code.size()+1; this.line++) {
            this.interpretExpression(this.code.get(this.line-1));
        }
    }

    private void interpretExpression(String expression) throws SyntaxError, EOFException {
        out.println(this.status);
        String trimmed = expression.trim();
        out.print(this.line); out.print(" - "); out.println(trimmed);
        String[] tokens = trimmed.split("[\\s]+"); // any multiple of any type of whitespace
        if(tokens.length != 0) {
            String statement = tokens[0];
            if(statements.contains(statement)) {
                if(tokens.length == 1) {
                    throw new SyntaxError(this.line);
                }
                String operand = tokens[1];
                if(reservedWords.contains(operand) || statements.contains(operand)) throw new ReservedTokenException(this.line, operand);
                // clear
                if(statement.equals(statements.get(0))) {
                    if(tokens.length > 2) throw new UnexpectedTokenException(this.line, operand);
                    this.clear(operand);
                }
                // incr
                if(statement.equals(statements.get(1))) {
                    if(tokens.length > 2) throw new UnexpectedTokenException(this.line, operand);
                    this.increment(operand);
                }
                // decr
                if(statement.equals(statements.get(2))) {
                    if(tokens.length > 2) throw new UnexpectedTokenException(this.line, operand);
                    this.decrement(operand);
                }
                // while
                if(statement.equals(statements.get(3))) {
                    if(!status.containsKey(operand)) throw new UndefinedException(this.line, operand);
                    if(tokens.length < 5) throw new SyntaxError(this.line);
                    if(tokens.length > 5) throw new UnexpectedTokenException(this.line, tokens[5]);
                    String notToken = tokens[2];
                    if(!notToken.equals(reservedWords.get(0))) throw new UnexpectedTokenException(this.line, notToken); // not
                    String zeroToken = tokens[3];
                    if(!zeroToken.equals(String.valueOf(0))) throw new UnexpectedTokenException(this.line, zeroToken); // 0
                    String doToken = tokens[4];
                    if(!doToken.equals(reservedWords.get(1))) throw new UnexpectedTokenException(this.line, doToken); // do
                    this.whileDo(operand, this.line + 1);
                }
                // if
                if(statement.equals(statements.get(4))) {}
                // func
                if(statement.equals(statements.get(5))) {}
                // print
                if(statement.equals(statements.get(6))) {
                    if(tokens.length > 2) throw new UnexpectedTokenException(this.line, operand);
                    this.print(operand);
                }
            } else {
                throw new UnexpectedTokenException(this.line, statement);
            }
        }
    }

    private void clear(String operand) {
        if(this.status.containsKey(operand)) {
            this.status.replace(operand, 0);
        } else {
            this.status.put(operand, 0);
        }
    }
    private void increment(String operand) {
        if(this.status.containsKey(operand)) {
            this.status.replace(operand, this.status.get(operand) + 1);
        } else throw new UndefinedException(this.line, operand);
    }
    private void decrement(String operand) {
        if(this.status.containsKey(operand)) {
            this.status.replace(operand, this.status.get(operand) - 1);
        } else throw new UndefinedException(this.line, operand);
    }
    private void whileDo(String operand, int line) throws EOFException {
        ArrayList<String> remaining = new ArrayList<>(this.code.subList(line-1, this.code.size()));
        Iterator<String> iterator = remaining.iterator();
        int nestLevel = 1;
        int endLine = 0;
        while(iterator.hasNext()) {
            String next = iterator.next();
            for(String token : new String[]{statements.get(3), statements.get(4), statements.get(5)}) {
                if(next.contains(token)) {
                    nestLevel++;
                }
            }
            if(next.contains(reservedWords.get(2))) nestLevel--;
            if(nestLevel == 0) break;
            endLine++;
            if(!iterator.hasNext()) throw new EOFException("Reached end of file before while loop finished");
        }
        ArrayList<String> block = new ArrayList<>(remaining.subList(0, endLine));
        while(this.status.get(operand) != 0) {
            Interpreter i = new Interpreter(block, this.status);
            this.status.putAll(i.status);
        }
        this.line = line + endLine;
    }
    private void print(String operand) {
        if(this.status.containsKey(operand)) {
            out.println(operand);
        } else throw new UndefinedException(this.line, operand);
    }

    public static void main(String[] args) throws FileNotFoundException, EOFException {
        String pathToProgram;
        try {
            pathToProgram = StringEscapeUtils.escapeJava(args[0]);
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalArgumentException("No file given to interpret");
        }
        Scanner sc = new Scanner(new File(pathToProgram));
        sc.useDelimiter(";");
        ArrayList<String> code = new ArrayList<>();
        while(sc.hasNext()) code.add(sc.next());
        Interpreter i = new Interpreter(code);
        out.println("Finishing status:");
        out.print(i.status);
    }

    private static int countOccurrences(String subString, String superString) {
        int index = superString.indexOf(subString);
        if(index == -1) return 0;
        else return countOccurrences(subString, superString.substring(index + subString.length())) + 1;
    }

    private static class SyntaxError extends RuntimeException {
        SyntaxError(int line) {
            super("Syntax error on line: " + line);
        }
    }
    private static class UnexpectedTokenException extends RuntimeException {
        UnexpectedTokenException(int line, String token) {
            super("Unexpected token on line " + line + ": " + token);
        }
    }
    private static class ReservedTokenException extends RuntimeException {
        ReservedTokenException(int line, String token) {
            super("Reserved token used as variable name on line " + line + ": " + token);
        }
    }
    private static class UndefinedException extends RuntimeException {
        UndefinedException(int line, String token) {
            super("Undefined variable on line " + line + ": " + token);
        }
    }
}
