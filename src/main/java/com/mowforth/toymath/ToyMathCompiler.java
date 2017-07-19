package com.mowforth.toymath;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.AbstractParseTreeVisitor;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import java.nio.file.Files;
import java.nio.file.Paths;


/**
 * Created by chris on 18/07/2017.
 */
public class ToyMathCompiler {

    private static final class CodeGen extends ToymathBaseListener {

        private final ClassWriter cw = new ClassWriter(0);

        private MethodVisitor main;
        private byte[] classBytes;
        private int depth, maxDepth;

        @Override
        public void enterMain(ToymathParser.MainContext ctx) {
            cw.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER, "ToymathExample", null, "java/lang/Object", null);
            cw.visitSource("ToymathExample.java", null);

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

        @Override public void enterAddValues(ToymathParser.AddValuesContext ctx) {
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
            main.visitMaxs(maxDepth + 3, 1);
            main.visitEnd();

            cw.visitEnd();

            classBytes = cw.toByteArray();
        }
    }

    public static void main(String[] args) throws Exception {
        String input = "main { 1 + 2 + 3 + 4 + 5 }";
        ANTLRInputStream inputStream = new ANTLRInputStream(input);
        ToymathLexer lexer = new ToymathLexer(inputStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        ToymathParser parser = new ToymathParser(tokenStream);

        CodeGen cg = new CodeGen();

        ParseTreeWalker.DEFAULT.walk(cg, parser.main());

        Files.write(Paths.get("ToymathExample.class"), cg.classBytes);
    }
}
