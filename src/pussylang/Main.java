package pussylang;

import pussylang.ast.Stmt;
import pussylang.compiler.Chunk;
import pussylang.compiler.Compiler;
import pussylang.compiler.Disassembler;
import pussylang.interpreter.Interpreter;
import pussylang.lexer.Lexer;
import pussylang.lexer.LexerException;
import pussylang.lexer.Token;
import pussylang.parser.ParseException;
import pussylang.parser.Parser;
import pussylang.vm.VM;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Main {

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            runVM(DEMO, false);
            return;
        }
        switch (args[0]) {
            case "--interpret" -> runInterpreter(src(args));
            case "--vm"        -> runVM(src(args), false);
            case "--dis"       -> runVM(src(args), true);
            default            -> runVM(Files.readString(Path.of(args[0])), false);
        }
    }



    private static void runInterpreter(String source) {
        List<Stmt> ast = frontend(source);
        if (ast == null) return;
        System.out.println(" tree-walk interpreter ");
        new Interpreter().interpret(ast);
    }

    private static void runVM(String source, boolean dis) {
        List<Stmt> ast = frontend(source);
        if (ast == null) return;

        Chunk chunk = new Compiler().compileScript(ast);

        if (dis) {
            Disassembler.disassemble(chunk);
            System.out.println();
        }

        System.out.println(" bytecode VM ");
        new VM().run(chunk);
    }



    private static List<Stmt> frontend(String source) {
        try {
            List<Token> tokens = new Lexer(source).tokenize();
            return new Parser(tokens).parse();
        } catch (LexerException | ParseException e) {
            System.err.println(e.getMessage());
            return null;
        }
    }

    private static String src(String[] args) throws IOException {
        return args.length > 1 ? Files.readString(Path.of(args[1])) : DEMO;
    }



    private static final String DEMO = """
        func factorial(n) {
            if (n <= 1) { return 1; }
            return n * factorial(n - 1);
        }

        print factorial(6);

        var sc = b"\\x90\\x90\\xCC";
        print len(sc);
        print hex(0xDEAD);

        var buf = alloc(0x1000);
        write(buf, sc, 3);
        exec(buf);
        free(buf);
    """;
}