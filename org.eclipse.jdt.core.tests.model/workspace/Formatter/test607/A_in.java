package uol.proxy.parser;

import java.nio.ByteBuffer;

import uol.proxy.smtp.SmtpResponse;
/**
 * Mensagens SMTP tem o seguinte formato:
 * <pre>
 * resposta de uma linha só:
 *  nnn [SP] lalalal [CR] [LF]
 * resposta de várias linhas:
 *  nnn [-] lalalalal [CR] [LF]
 *  nnn [-] lalalalal [CR] [LF]
 *  ...
 *  nnn [SP] lalalalal [CR] [LF]
 * 
 * 
 * 
 * */
public class SmtpServerParser {
    private static final int DIGITS = 0;
    private static final int LINE_MODE_CHAR = 1;
    private static final int SKIP_TO_EOL_AND_FINISH = 2;
    private static final int SKIP_TO_EOL = 3;
    
    int state = DIGITS;
    int code;
    int pos;
    boolean wellFormed = true;
    
    
    public SmtpServerParser() {
    }
    
    //formato: xxx lalal
    public SmtpResponse parse(ByteBuffer buffer) {
        outer: while (buffer.remaining() > 0) {
            char c = (char) buffer.get();
            ++pos;
            switch (state) {
            case DIGITS: //get number
                if(Character.isDigit(c)) {
                    code = code * 10 + (c - '0');
                } else {
                    wellFormed = false;
                    state = SKIP_TO_EOL_AND_FINISH;
                }
                if(pos == 3)
                    state = LINE_MODE_CHAR;
                break;
            case LINE_MODE_CHAR:
                if(c == ' ')
                    state = SKIP_TO_EOL_AND_FINISH;
                else if(c == ' ')
                    state = SKIP_TO_EOL;
                else {
                    wellFormed = false;
                    state = SKIP_TO_EOL_AND_FINISH;
                }
                break;
            case SKIP_TO_EOL_AND_FINISH:
                
                
                break;
            }
        }
        return null;
    }

}
