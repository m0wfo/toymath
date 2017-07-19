package com.mowforth.toymath;

import com.lexicalscope.jewel.cli.Option;
import com.lexicalscope.jewel.cli.Unparsed;

/**
 * Command line options.
 */
public interface CLIOptions {

    @Option(shortName = "i")
    boolean getInteractive();

    @Unparsed
    String getSourceFile();
}
