package com.mowforth.toymath;

import com.lexicalscope.jewel.cli.CliFactory;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Deque;
import java.util.LinkedList;


/**
 * Compiler for the toy math language.
 */
public class ToyMathCompiler {

    private static final class CodeGen extends ToymathBaseListener {

        private final ClassWriter cw = new ClassWriter(0);
        private final Path sourcePath;
        private final Deque<Label> labels;

        private MethodVisitor main;
        private byte[] classBytes;
        private int depth, maxDepth;

        CodeGen(Path sourcePath) {
            this.sourcePath = sourcePath;
            this.labels = new LinkedList<>();
        }

        @Override
        public void enterMain(ToymathParser.MainContext ctx) {
            cw.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER, sourcePath.toString().split("\\.")[0], null, "java/lang/Object", null);
            cw.visitSource(sourcePath.toString(), null);

            // constructor
            MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
            mv.visitVarInsn(Opcodes.ALOAD, 0);
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL,
                    "java/lang/Object",
                    "<init>",
                    "()V");
            mv.visitInsn(Opcodes.RETURN);
            mv.visitMaxs(1, 1);
            mv.visitEnd();

            // start of main method
            main = cw.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, "main",
                    "([Ljava/lang/String;)V",
                    null,
                    null);

            main.visitFieldInsn(Opcodes.GETSTATIC,
                    "java/lang/System",
                    "out",
                    "Ljava/io/PrintStream;");
        }

        @Override
        public void enterAddValues(ToymathParser.AddValuesContext ctx) {
            main.visitLdcInsn(Integer.parseInt(ctx.INT(0).getText()));
            main.visitLdcInsn(Integer.parseInt(ctx.INT(1).getText()));
            main.visitInsn(Opcodes.IADD);
            for (int i = depth; i > 0; i--) {
                main.visitInsn(Opcodes.IADD);
                depth--;
            }
        }

        @Override
        public void enterAddExpr(ToymathParser.AddExprContext ctx) {
            main.visitLdcInsn(Integer.parseInt(ctx.INT().getText()));
            depth++;
            maxDepth++;
        }

        @Override
        public void exitMain(ToymathParser.MainContext ctx) {
            main.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                    "java/io/PrintStream",
                    "println",
                    "(I)V");
            main.visitInsn(Opcodes.RETURN);
            main.visitMaxs(maxDepth + 4, 1);
            main.visitEnd();

            cw.visitEnd();

            classBytes = cw.toByteArray();
        }

        @Override
        public void enterLoop(ToymathParser.LoopContext ctx) {
            Label label = new Label();
            labels.push(label);
            main.visitLabel(label);
        }

        @Override
        public void exitLoop(ToymathParser.LoopContext ctx) {
            main.visitInsn(Opcodes.POP);
            main.visitJumpInsn(Opcodes.GOTO, labels.pop());
        }

    }

    public static void main(String[] args) throws Exception {
        CLIOptions options = CliFactory.parseArguments(CLIOptions.class, args);

        Path sourcePath = Paths.get(options.getSourceFile());

        String input = new String(Files.readAllBytes(sourcePath), StandardCharsets.UTF_8);
        ANTLRInputStream inputStream = new ANTLRInputStream(input);
        ToymathLexer lexer = new ToymathLexer(inputStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        ToymathParser parser = new ToymathParser(tokenStream);

        CodeGen cg = new CodeGen(sourcePath);

        ParseTreeWalker.DEFAULT.walk(cg, parser.main());

        String[] parts = sourcePath.toString().split("\\.");

        Files.write(Paths.get(parts[0] + ".class"), cg.classBytes);
    }
}
