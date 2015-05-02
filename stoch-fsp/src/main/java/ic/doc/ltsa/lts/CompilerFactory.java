package ic.doc.ltsa.lts;

import ic.doc.ltsa.common.iface.ICompilerFactory;
import ic.doc.ltsa.common.iface.ILTSCompiler;
import ic.doc.ltsa.common.iface.ILex;
import ic.doc.ltsa.common.iface.LTSInput;
import ic.doc.ltsa.common.iface.LTSOutput;

public class CompilerFactory implements ICompilerFactory {

    public ILTSCompiler createCompiler(LTSInput pInput, LTSOutput pOutput, String pCurrentDirectory) {

        return new LTSCompiler( pInput , pOutput , pCurrentDirectory );
    }

    public ILex createLex( LTSInput pInput , boolean b ) {

        return new Lex( pInput , b );
    }
}
