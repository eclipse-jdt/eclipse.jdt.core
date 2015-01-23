/**
 * Mensagens SMTP tem o seguinte formato:
 * 
 * <pre>
 * resposta de uma linha só:
 *  nnn [SP] lalalal [CR] [LF]
 * resposta de várias linhas:
 *  nnn [-] lalalalal [CR] [LF]
 *  nnn [-] lalalalal [CR] [LF]
 *  ...
 *  nnn [SP] lalalalal [CR] [LF]
 * </pre>
 */